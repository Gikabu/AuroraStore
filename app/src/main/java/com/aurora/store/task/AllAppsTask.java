

package com.aurora.store.task;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.aurora.store.utility.CertUtil;
import com.aurora.store.utility.Util;

import java.util.ArrayList;
import java.util.List;

public class AllAppsTask extends BaseTask {

    public AllAppsTask(Context context) {
        super(context);
    }

    List<String> getInstalledApps() {
        List<String> packageList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        for (PackageInfo packageInfo : packageManager.getInstalledPackages(0)) {
            final String packageName = packageInfo.packageName;
            if (packageInfo.applicationInfo != null
                    && !packageInfo.applicationInfo.enabled
                    && !Util.isExtendedUpdatesEnabled(context))
                continue;
            if (Util.filterFDroidAppsEnabled(context) && CertUtil.isFDroidApp(context, packageName))
                continue;
            packageList.add(packageName);
        }
        return packageList;
    }
}
