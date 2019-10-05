

package com.aurora.store.manager;

import android.content.Context;

import com.aurora.store.Constants;
import com.aurora.store.utility.PrefUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BlacklistManager {

    private Context context;
    private ArrayList<String> blackList;

    public BlacklistManager(Context context) {
        this.context = context;
        blackList = PrefUtil.getListString(context, Constants.PREFERENCE_BLACKLIST_APPS_LIST);
    }

    public boolean add(String s) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(s);
        boolean result = addAll(arrayList);
        save();
        return result;
    }

    public boolean addAll(ArrayList<String> arrayList) {
        boolean result = blackList.addAll(arrayList);
        Set<String> mAppSet = new HashSet<>(blackList);
        blackList.clear();
        blackList.addAll(mAppSet);
        save();
        return result;
    }

    public ArrayList<String> get() {
        return blackList;
    }

    public boolean contains(String packageName) {
        return blackList.contains(packageName);
    }

    public boolean remove(String packageName) {
        boolean result = blackList.remove(packageName);
        save();
        return result;
    }

    public boolean removeAll(ArrayList<String> packageList) {
        boolean result = blackList.removeAll(packageList);
        save();
        return result;
    }

    private void save() {
        PrefUtil.putListString(context, Constants.PREFERENCE_BLACKLIST_APPS_LIST, blackList);
    }
}
