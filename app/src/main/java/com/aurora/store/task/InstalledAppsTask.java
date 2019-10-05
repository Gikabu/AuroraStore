

package com.aurora.store.task;

import android.content.Context;
import android.text.TextUtils;

import com.aurora.store.model.App;
import com.aurora.store.utility.PackageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstalledAppsTask extends UpdatableAppsTask {

    public InstalledAppsTask(Context context) {
        super(context);
    }

    public List<App> getInstalledApps(boolean removeSystemApps) throws Exception {
        List<App> appList = new ArrayList<>();
        List<String> packageList = getInstalledApps();
        if (removeSystemApps)
            packageList = filterSystemApps(packageList);
        packageList = filterBlacklistedApps(packageList);
        for (App app : getAppsFromPlayStore(packageList)) {
            final String packageName = app.getPackageName();
            if (TextUtils.isEmpty(packageName) || !packageList.contains(packageName)) {
                continue;
            }

            final App installedApp = getInstalledApp(packageName);
            app = addInstalledAppInfo(app, installedApp);
            appList.add(app);
        }
        return appList;
    }

    public List<App> getAllApps() throws Exception {
        List<App> appList = new ArrayList<>();
        List<String> packageList = getInstalledApps();
        for (App app : getAppsFromPlayStore(packageList)) {
            final String packageName = app.getPackageName();
            if (TextUtils.isEmpty(packageName) || !packageList.contains(packageName)) {
                continue;
            }

            final App installedApp = getInstalledApp(packageName);
            app = addInstalledAppInfo(app, installedApp);
            appList.add(app);
        }
        return appList;
    }

    private List<String> filterSystemApps(List<String> packageList) {
        List<String> newPackageList = new ArrayList<>();
        for (String packageName : packageList) {
            if (!PackageUtil.isSystemApp(getPackageManager(), packageName)) {
                newPackageList.add(packageName);
            }
        }
        return newPackageList;
    }
}