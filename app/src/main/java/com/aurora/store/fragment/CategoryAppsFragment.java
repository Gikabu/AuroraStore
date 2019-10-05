

package com.aurora.store.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.aurora.store.Filter;
import com.aurora.store.R;
import com.aurora.store.activity.AuroraActivity;
import com.aurora.store.adapter.SubCategoryAdapter;
import com.aurora.store.sheet.FilterBottomSheet;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.Util;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryAppsFragment extends Fragment {

    public static String categoryId;

    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.category_tabs)
    TabLayout tabLayout;
    @BindView(R.id.filter_fab)
    ExtendedFloatingActionButton filterFab;

    private Context context;
    private ActionBar actionBar;

    public ExtendedFloatingActionButton getFilterFab() {
        return filterFab;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_category_container, container, false);
        ButterKnife.bind(this, view);
        Bundle arguments = getArguments();
        if (arguments != null) {
            categoryId = arguments.getString("CategoryId");
            if (getActivity() instanceof AuroraActivity) {
                actionBar = ((AuroraActivity) getActivity()).getSupportActionBar();
                actionBar.setTitle(arguments.getString("CategoryName"));
            }
        } else
            Log.e("No category id provided");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getContext(), getChildFragmentManager());
        viewPager.setAdapter(subCategoryAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
        filterFab.setOnClickListener(v -> {
            getFilterDialog();
        });
    }

    @Override
    public void onDestroy() {
        Glide.with(this).pauseAllRequests();
        if (actionBar != null)
            actionBar.setTitle(getString(R.string.app_name));
        if (Util.filterSearchNonPersistent(context))
            new Filter(context).resetFilterPreferences();
        super.onDestroy();
    }

    private void getFilterDialog() {
        FilterBottomSheet filterSheet = new FilterBottomSheet();
        filterSheet.setOnApplyListener(v -> {
            filterSheet.dismiss();
            viewPager.removeAllViews();
            viewPager.setAdapter(new SubCategoryAdapter(getContext(), getChildFragmentManager()));
        });
        filterSheet.show(getChildFragmentManager(), "FILTER");
    }
}