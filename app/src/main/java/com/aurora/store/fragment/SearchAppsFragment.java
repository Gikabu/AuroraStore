

package com.aurora.store.fragment;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.store.EndlessScrollListener;
import com.aurora.store.ErrorType;
import com.aurora.store.Filter;
import com.aurora.store.R;
import com.aurora.store.activity.AuroraActivity;
import com.aurora.store.adapter.EndlessAppsAdapter;
import com.aurora.store.model.App;
import com.aurora.store.sheet.FilterBottomSheet;
import com.aurora.store.task.SearchTask;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.NetworkUtil;
import com.aurora.store.utility.Util;
import com.aurora.store.utility.ViewUtil;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchAppsFragment extends BaseFragment {

    @BindView(R.id.search_apps)
    SearchView searchView;
    @BindView(R.id.search_apps_list)
    RecyclerView recyclerView;
    @BindView(R.id.filter_fab)
    ExtendedFloatingActionButton filterFab;
    @BindView(R.id.related_chip_group)
    ChipGroup relatedChipGroup;

    private Context context;
    private View view;
    private String query;
    private List<String> relatedTags = new ArrayList<>();
    private EndlessAppsAdapter endlessAppsAdapter;
    private SearchTask searchTask;

    private String getQuery() {
        return query;
    }

    private void setQuery(String query) {
        this.query = query;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        searchTask = new SearchTask(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search_applist, container, false);
        ButterKnife.bind(this, view);
        Bundle arguments = getArguments();
        if (arguments != null) {
            setQuery(arguments.getString("SearchQuery"));
            searchView.setQuery(getQuery(), false);
        } else
            Log.e("No category id provided");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setErrorView(ErrorType.UNKNOWN);
        setupRecycler();
        setupSearch();
        filterFab.show();
        filterFab.setOnClickListener(v -> getFilterDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (endlessAppsAdapter == null || endlessAppsAdapter.isDataEmpty())
            fetchSearchAppsList(false);
    }

    @Override
    public void onDestroy() {
        Glide.with(this).pauseAllRequests();
        disposable.dispose();
        if (Util.filterSearchNonPersistent(context))
            new Filter(context).resetFilterPreferences();
        super.onDestroy();
    }

    private void setupSearch() {
        SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = getActivity().getComponentName();

        if (null != searchManager && componentName != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        }

        if (!StringUtils.isEmpty(AuroraActivity.externalQuery))
            setQuery(AuroraActivity.externalQuery);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String newQuery) {
                searchView.clearFocus();
                query = newQuery;
                fetchSearchAppsList(false);
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = searchView.getSuggestionsAdapter().getCursor();
                cursor.moveToPosition(position);
                if (position == 0) {
                    searchView.setQuery(cursor.getString(2), true);
                    searchView.setQuery(cursor.getString(1), false);
                } else
                    searchView.setQuery(cursor.getString(1), true);
                return true;
            }
        });
    }

    private void getFilterDialog() {
        FilterBottomSheet filterSheet = new FilterBottomSheet();
        filterSheet.setCancelable(true);
        filterSheet.setOnApplyListener(v -> {
            filterSheet.dismiss();
            recyclerView.removeAllViewsInLayout();
            fetchSearchAppsList(false);
        });
        filterSheet.show(getChildFragmentManager(), "FILTER");
    }

    private void getIterator() {
        try {
            iterator = null;
            iterator = getIterator(getQuery());
            iterator.setEnableFilter(true);
            iterator.setFilter(new Filter(getContext()).getFilterPreferences());
            relatedTags = iterator.getRelatedTags();
        } catch (Exception e) {
            processException(e);
        }
    }

    private void fetchSearchAppsList(boolean shouldIterate) {
        if (!shouldIterate)
            getIterator();
        disposable.add(Observable.fromCallable(() -> searchTask.getSearchResults(iterator))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appList -> {
                    if (view != null) {
                        if (shouldIterate) {
                            addApps(appList);
                        } else if (appList.isEmpty() && endlessAppsAdapter.isDataEmpty()) {
                            setErrorView(ErrorType.NO_SEARCH);
                            switchViews(true);
                        } else {
                            switchViews(false);
                            if (endlessAppsAdapter != null)
                                endlessAppsAdapter.addData(appList);
                            if (!relatedTags.isEmpty())
                                setupTags();
                        }
                    }
                }, err -> {
                    Log.e(err.getMessage());
                    processException(err);
                }));
    }

    private void addApps(List<App> appsToAdd) {
        if (!appsToAdd.isEmpty()) {
            for (App app : appsToAdd)
                endlessAppsAdapter.add(app);
            endlessAppsAdapter.notifyItemInserted(endlessAppsAdapter.getItemCount() - 1);
        }

        /*
         * Search results are scarce if filter are too strict, in this case endless scroll events
         * fail to fetch next batch of apps, so manually fetch at least 10 apps until available.
         */
        disposable.add(Observable.interval(1000, 2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (iterator.hasNext() && endlessAppsAdapter.getItemCount() < 10) {
                        iterator.next();
                    }
                }, e -> Log.e(e.getMessage())));
    }

    private void setupRecycler() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        endlessAppsAdapter = new EndlessAppsAdapter(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this.getActivity(), R.anim.anim_falldown));
        recyclerView.setAdapter(endlessAppsAdapter);
        EndlessScrollListener endlessScrollListener = new EndlessScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                fetchSearchAppsList(true);
            }
        };
        recyclerView.addOnScrollListener(endlessScrollListener);
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (velocityY < 0) {
                    filterFab.show();
                } else if (velocityY > 0) {
                    filterFab.hide();
                }
                return false;
            }
        });
    }

    private void setupTags() {
        relatedChipGroup.removeAllViews();
        int i = 0;
        for (String tag : relatedTags) {
            final int color = ViewUtil.getSolidColors(i++);
            Chip chip = new Chip(context);
            chip.setText(tag);
            chip.setChipIconSize(ViewUtil.dpToPx(context, 24));
            chip.setChipStrokeWidth(ViewUtil.dpToPx(context, 1));
            chip.setChipStrokeColor(ColorStateList.valueOf(color));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 100)));
            chip.setRippleColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 200)));
            chip.setCheckedIcon(context.getDrawable(R.drawable.ic_checked));
            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    query = query + " " + tag;
                    fetchData();
                    searchView.setQuery(query, false);
                } else {
                    query = query.replace(tag, "");
                    fetchData();
                    searchView.setQuery(query, false);
                }
            });
            relatedChipGroup.addView(chip);
        }
    }

    @Override
    protected void fetchData() {
        fetchSearchAppsList(false);
    }

    @Override
    protected View.OnClickListener errRetry() {
        return v -> {
            if (NetworkUtil.isConnected(context)) {
                fetchData();
            } else {
                setErrorView(ErrorType.NO_NETWORK);
            }
            ((Button) v).setText(getString(R.string.action_retry_ing));
            ((Button) v).setEnabled(false);
        };
    }
}