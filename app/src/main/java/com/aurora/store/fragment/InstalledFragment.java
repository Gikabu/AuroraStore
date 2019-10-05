

package com.aurora.store.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.store.Constants;
import com.aurora.store.ErrorType;
import com.aurora.store.ListType;
import com.aurora.store.R;
import com.aurora.store.adapter.InstalledAppsAdapter;
import com.aurora.store.model.App;
import com.aurora.store.task.InstalledAppsTask;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.PrefUtil;
import com.aurora.store.view.CustomSwipeToRefresh;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class InstalledFragment extends BaseFragment {

    @BindView(R.id.swipe_layout)
    CustomSwipeToRefresh customSwipeToRefresh;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.switch_system)
    SwitchMaterial switchSystem;

    private Context context;
    private View view;
    private InstalledAppsAdapter adapter;
    private InstalledAppsTask installedAppTask;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_installed, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setErrorView(ErrorType.UNKNOWN);

        switchSystem.setChecked(PrefUtil.getBoolean(context, Constants.PREFERENCE_INCLUDE_SYSTEM));
        switchSystem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                PrefUtil.putBoolean(context, Constants.PREFERENCE_INCLUDE_SYSTEM, true);
            else
                PrefUtil.putBoolean(context, Constants.PREFERENCE_INCLUDE_SYSTEM, false);
            fetchData();
        });
        customSwipeToRefresh.setOnRefreshListener(() -> fetchData());
        setupRecycler();
    }

    @Override
    public void onPause() {
        super.onPause();
        customSwipeToRefresh.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter.isDataEmpty())
            fetchAppsFromCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        installedAppTask = null;
        adapter = null;
    }

    @Override
    protected void fetchData() {
        installedAppTask = new InstalledAppsTask(context);
        disposable.add(Observable.fromCallable(() -> installedAppTask
                .getInstalledApps(switchSystem.isChecked()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(subscription -> customSwipeToRefresh.setRefreshing(true))
                .doOnTerminate(() -> customSwipeToRefresh.setRefreshing(false))
                .subscribe((appList) -> {
                    if (view != null) {
                        if (appList.isEmpty()) {
                            setErrorView(ErrorType.NO_APPS);
                            switchViews(true);
                        } else {
                            switchViews(false);
                            if (adapter != null)
                                adapter.addData(appList);
                            saveToCache(appList);
                        }
                    }
                }, err -> {
                    Log.d(err.getMessage());
                    processException(err);
                }));
    }

    private void saveToCache(List<App> appList) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(appList);
        PrefUtil.putString(context, Constants.PREFERENCE_INSTALLED_APPS, jsonString);
    }

    private void fetchAppsFromCache() {
        Gson gson = new Gson();
        Type type = new TypeToken<List<App>>() {
        }.getType();
        String jsonString = PrefUtil.getString(context, Constants.PREFERENCE_INSTALLED_APPS);
        List<App> appList = gson.fromJson(jsonString, type);
        if (appList == null || appList.isEmpty())
            fetchData();
        else {
            adapter.addData(appList);
            switchViews(false);
        }
    }

    private void setupRecycler() {
        adapter = new InstalledAppsAdapter(context, ListType.INSTALLED);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.anim_falldown));
    }

    @Override
    protected View.OnClickListener errRetry() {
        return v -> {
            fetchData();
            ((Button) v).setText(getString(R.string.action_retry_ing));
            ((Button) v).setEnabled(false);
        };
    }
}
