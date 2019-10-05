

package com.aurora.store.adapter;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.aurora.store.R;
import com.aurora.store.fragment.SubCategoryFragment;

import org.jetbrains.annotations.NotNull;

public class SubCategoryAdapter extends FragmentStatePagerAdapter {

    private Context context;

    public SubCategoryAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        final Bundle bundle = new Bundle();
        final Fragment subCategoryFragment = new SubCategoryFragment();
        switch (position) {
            case 0:
                bundle.putString("SUBCATEGORY", "TOP_FREE");
                break;
            case 1:
                bundle.putString("SUBCATEGORY", "MOVERS_SHAKERS");
                break;
            default:
                bundle.putString("SUBCATEGORY", "TOP_GROSSING");
                break;
        }
        subCategoryFragment.setArguments(bundle);
        return subCategoryFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.category_topFree);
            case 1:
                return context.getString(R.string.category_trending);
            default:
                return context.getString(R.string.category_topGrossing);
        }
    }
}