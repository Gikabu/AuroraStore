

package com.aurora.store.utility;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.aurora.store.model.App;

import java.util.Map;

public class PackageUtil {

    private static final String PSEUDO_PACKAGE_MAP = "PSEUDO_PACKAGE_MAP";
    private static final String PSEUDO_URL_MAP = "PSEUDO_URL_MAP";

    public static String getDisplayName(Context context, String packageName) {
        Map<String, String> pseudoMap = getPseudoPackageMap(context);
        return TextUtil.emptyIfNull(pseudoMap.get(packageName));
    }

    public static String getIconURL(Context context, String packageName) {
        Map<String, String> pseudoMap = getPseudoURLMap(context);
        return TextUtil.emptyIfNull(pseudoMap.get(packageName));
    }

    private static Map<String, String> getPseudoPackageMap(Context context) {
        return PrefUtil.getMap(context, PSEUDO_PACKAGE_MAP);
    }

    private static Map<String, String> getPseudoURLMap(Context context) {
        return PrefUtil.getMap(context, PSEUDO_URL_MAP);
    }

    public static void addToPseudoPackageMap(Context context, String packageName, String displayName) {
        Map<String, String> pseudoMap = getPseudoPackageMap(context);
        pseudoMap.put(packageName, displayName);
        PrefUtil.saveMap(context, pseudoMap, PSEUDO_PACKAGE_MAP);
    }

    public static void addToPseudoURLMap(Context context, String packageName, String iconURL) {
        Map<String, String> pseudoMap = getPseudoURLMap(context);
        pseudoMap.put(packageName, iconURL);
        PrefUtil.saveMap(context, pseudoMap, PSEUDO_URL_MAP);
    }

    public static App getInstalledApp(PackageManager packageManager, String packageName) {
        try {
            final App app = new App();
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS);
            app.setPackageName(packageName);
            app.setDisplayName(packageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
            app.setVersionName(packageInfo.versionName);
            app.setVersionCode(packageInfo.versionCode);
            return app;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isSystemApp(PackageManager packageManager, String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @NonNull
    public static String getAppLabel(Context c, String packageName) {
        try {
            PackageManager pm = c.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(appInfo).toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isInstalled(Context context, App app) {
        try {
            context.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
