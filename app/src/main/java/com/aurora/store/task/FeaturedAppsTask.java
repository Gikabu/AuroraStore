

package com.aurora.store.task;

import android.content.Context;

import com.aurora.store.api.CategoryAppsIterator2;
import com.aurora.store.iterator.CustomAppListIterator;
import com.aurora.store.model.App;
import com.aurora.store.utility.Util;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeaturedAppsTask extends BaseTask {

    public FeaturedAppsTask(Context context) {
        super(context);
    }

    public List<App> getApps(String categoryId, GooglePlayAPI.SUBCATEGORY subCategory) throws Exception {
        final GooglePlayAPI api = getApi();
        final CustomAppListIterator iterator = new CustomAppListIterator(new CategoryAppsIterator2(api, categoryId, subCategory));
        List<App> apps = new ArrayList<>();
        while (iterator.hasNext() && apps.isEmpty()) {
            apps.addAll(iterator.next());
        }
        if (Util.filterGoogleAppsEnabled(context))
            return filterGoogleApps(apps);
        else
            return apps;
    }
}
