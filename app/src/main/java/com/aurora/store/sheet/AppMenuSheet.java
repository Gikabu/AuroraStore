

package com.aurora.store.sheet;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.store.AuroraApplication;
import com.aurora.store.R;
import com.aurora.store.activity.ManualDownloadActivity;
import com.aurora.store.adapter.InstalledAppsAdapter;
import com.aurora.store.adapter.UpdatableAppsAdapter;
import com.aurora.store.fragment.DetailsFragment;
import com.aurora.store.manager.BlacklistManager;
import com.aurora.store.manager.FavouriteListManager;
import com.aurora.store.model.App;
import com.aurora.store.utility.ApkCopier;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.PackageUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AppMenuSheet extends BottomSheetDialogFragment {

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    private App app;
    private Context context;
    private RecyclerView.Adapter adapter;
    private CompositeDisposable disposable = new CompositeDisposable();

    public AppMenuSheet() {
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_app_menu, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final FavouriteListManager favouriteListManager = new FavouriteListManager(context);
        boolean isFav = favouriteListManager.contains(app.getPackageName());
        MenuItem favMenu = navigationView.getMenu().findItem(R.id.action_fav);
        favMenu.setTitle(isFav ? R.string.details_favourite_remove : R.string.details_favourite_add);

        final BlacklistManager blacklistManager = new BlacklistManager(context);
        boolean isBlacklisted = blacklistManager.contains(app.getPackageName());
        MenuItem blackListMenu = navigationView.getMenu().findItem(R.id.action_blacklist);
        blackListMenu.setTitle(isBlacklisted ? R.string.action_whitelist : R.string.action_blacklist);

        boolean installed = PackageUtil.isInstalled(context, app);
        navigationView.getMenu().findItem(R.id.action_uninstall).setVisible(installed);
        navigationView.getMenu().findItem(R.id.action_local).setVisible(installed);
        navigationView.getMenu().findItem(R.id.action_info).setVisible(installed);

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_fav:
                    if (isFav) {
                        favouriteListManager.remove(app.getPackageName());
                    } else {
                        favouriteListManager.add(app.getPackageName());
                    }
                    break;
                case R.id.action_blacklist:
                    if (isBlacklisted) {
                        blacklistManager.remove(app.getPackageName());
                        Toast.makeText(context, context.getString(R.string.toast_apk_whitelisted),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        blacklistManager.add(app.getPackageName());
                        Toast.makeText(context, context.getString(R.string.toast_apk_blacklisted),
                                Toast.LENGTH_SHORT).show();
                        if (adapter instanceof InstalledAppsAdapter)
                            ((InstalledAppsAdapter) adapter).remove(app);
                        if (adapter instanceof UpdatableAppsAdapter)
                            ((UpdatableAppsAdapter) adapter).remove(app);
                    }
                    break;
                case R.id.action_local:
                    disposable.add(Observable.fromCallable(() -> new ApkCopier(context, app)
                            .copy())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(success -> {
                                Toast.makeText(context, success
                                        ? context.getString(R.string.toast_apk_copy_success)
                                        : context.getString(R.string.toast_apk_copy_failure), Toast.LENGTH_SHORT)
                                        .show();
                            }));
                    break;
                case R.id.action_manual:
                    DetailsFragment.app = app;
                    context.startActivity(new Intent(context, ManualDownloadActivity.class));
                    break;
                case R.id.action_uninstall:
                    AuroraApplication.getUninstaller().uninstall(app);
                    break;
                case R.id.action_info:
                    try {
                        context.startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",
                                Uri.parse("package:" + app.getPackageName())));
                    } catch (ActivityNotFoundException e) {
                        Log.e("Could not find system app activity");
                    }
                    break;
            }
            dismissAllowingStateLoss();
            return false;
        });
    }
}
