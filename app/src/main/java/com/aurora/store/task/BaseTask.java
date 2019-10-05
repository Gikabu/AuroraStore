

package com.aurora.store.task;

import android.content.Context;
import android.content.ContextWrapper;

import com.aurora.store.api.PlayStoreApiAuthenticator;
import com.aurora.store.exception.InvalidApiException;
import com.aurora.store.model.App;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseTask extends ContextWrapper {

    protected Context context;
    protected GooglePlayAPI api;

    public BaseTask(Context context) {
        super(context);
        this.context = context;
    }


    public GooglePlayAPI getApi() throws Exception {
        api = PlayStoreApiAuthenticator.getApi(context);
        if (api == null)
            throw new InvalidApiException("Failed to build api, probably logged out");
        else
            return api;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<App> filterGoogleApps(List<App> apps) {
        Set<String> shitSet = new HashSet<>();
        shitSet.add("com.chrome.beta");
        shitSet.add("com.chrome.canary");
        shitSet.add("com.chrome.dev");
        shitSet.add("com.android.chrome");
        shitSet.add("com.niksoftware.snapseed");
        shitSet.add("com.google.toontastic");

        List<App> mApps = new ArrayList<>();
        for (App app : apps) {
            if (!app.getPackageName().startsWith("com.google") && !shitSet.contains(app.getPackageName())) {
                mApps.add(app);
            }
        }
        return mApps;
    }

}