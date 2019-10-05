

package com.aurora.store.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.aurora.store.manager.BlacklistManager;

import java.util.ArrayList;

abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected ArrayList<String> mSelections;
    protected Context context;
    protected BlacklistManager mBlacklistManager;

    SelectableAdapter(Context context) {
        this.context = context;
        mBlacklistManager = new BlacklistManager(context);
        ArrayList<String> blacklistedApps = mBlacklistManager.get();
        mSelections = new ArrayList<>();
        if (blacklistedApps != null && !blacklistedApps.isEmpty()) {
            mSelections.addAll(blacklistedApps);
        }
    }

    boolean isSelected(String packageName) {
        return mSelections.contains(packageName);
    }

    void toggleSelection(int position) {
    }

    public void addSelectionsToBlackList() {
        mBlacklistManager.addAll(mSelections);
    }

    public void removeSelectionsFromBlackList() {
        mBlacklistManager.removeAll(mSelections);
        mSelections = new ArrayList<>();
    }
}
