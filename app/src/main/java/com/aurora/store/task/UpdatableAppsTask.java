

package com.aurora.store.task;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.aurora.store.exception.MalformedRequestException;
import com.aurora.store.manager.BlacklistManager;
import com.aurora.store.model.App;
import com.aurora.store.model.AppBuilder;
import com.aurora.store.utility.Accountant;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.PackageUtil;
import com.dragons.aurora.playstoreapiv2.BulkDetailsEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdatableAppsTask extends AllAppsTask {

    public UpdatableAppsTask(Context context) {
        super(context);
    }

    public List<App> getUpdatableApps() throws Exception {
        List<App> appList = new ArrayList<>();
        List<String> packageList = getInstalledApps();
        packageList = filterBlacklistedApps(packageList);
        for (App app : getAppsFromPlayStore(packageList)) {
            final String packageName = app.getPackageName();
            if (TextUtils.isEmpty(packageName) || !packageList.contains(packageName)) {
                continue;
            }

            final App installedApp = getInstalledApp(packageName);
            app = addInstalledAppInfo(app, installedApp);

            if (installedApp != null && installedApp.getVersionCode() < app.getVersionCode()) {
                appList.add(app);
            }
        }
        return appList;
    }

    public List<App> getAppsFromPlayStore(List<String> packageNames) throws Exception {
        final List<App> appsFromPlayStore = new ArrayList<>();
        boolean builtInAccount = Accountant.isDummy(context);
        for (App app : getRemoteAppList(packageNames)) {
            if (!builtInAccount || app.isFree()) {
                appsFromPlayStore.add(app);
            }
        }
        return appsFromPlayStore;
    }

    private List<App> getRemoteAppList(List<String> packageNames) throws Exception {
        final List<App> appList = new ArrayList<>();
        try {
            final List<BulkDetailsEntry> bulkDetailsEntries = getApi().bulkDetails(packageNames).getEntryList();
            for (BulkDetailsEntry bulkDetailsEntry : bulkDetailsEntries) {
                if (!bulkDetailsEntry.hasDoc()) {
                    continue;
                }
                appList.add(AppBuilder.build(bulkDetailsEntry.getDoc()));
            }
        } catch (Exception e) {
            if (e instanceof MalformedRequestException) {
                Log.e("Malformed Request : %s", e.getMessage());
            } else
                throw e;
        }
        return appList;
    }

    public App addInstalledAppInfo(App appFromMarket, App installedApp) {
        if (installedApp != null) {
            appFromMarket.setPackageName(installedApp.getPackageName());
            appFromMarket.setVersionName(installedApp.getVersionName());
            appFromMarket.setDisplayName(installedApp.getDisplayName());
            appFromMarket.setSystem(installedApp.isSystem());
        }
        return appFromMarket;
    }

    public App getInstalledApp(String packageName) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            return PackageUtil.getInstalledApp(getPackageManager(), packageInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public List<String> filterBlacklistedApps(List<String> packageList) {
        packageList.removeAll(new BlacklistManager(context).get());
        return packageList;
    }
}