

package com.aurora.store;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.IntentFilter;

import com.aurora.store.installer.Installer;
import com.aurora.store.installer.InstallerService;
import com.aurora.store.installer.Uninstaller;
import com.aurora.store.model.App;
import com.aurora.store.utility.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.plugins.RxJavaPlugins;

public class AuroraApplication extends Application {

    public static boolean updating = false;
    public static List<App> ongoingUpdateList = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    public static Installer installer;
    @SuppressLint("StaticFieldLeak")
    public static Uninstaller uninstaller;

    public static boolean getOnGoingUpdate() {
        return updating;
    }

    public static void setOnGoingUpdate(boolean updating) {
        AuroraApplication.updating = updating;
    }

    public static List<App> getOngoingUpdateList() {
        return ongoingUpdateList;
    }

    public static void setOngoingUpdateList(List<App> ongoingUpdateList) {
        AuroraApplication.ongoingUpdateList = ongoingUpdateList;
    }

    public static void removeFromOngoingUpdateList(String packageName) {
        Iterator<App> iterator = ongoingUpdateList.iterator();
        while (iterator.hasNext()) {
            if (packageName.equals(iterator.next().getPackageName()))
                iterator.remove();
        }
        if (ongoingUpdateList.isEmpty())
            setOnGoingUpdate(false);
    }

    public static Installer getInstaller() {
        return installer;
    }

    public static Uninstaller getUninstaller() {
        return uninstaller;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        installer = new Installer(this);
        uninstaller = new Uninstaller(this);
        Util.clearOldInstallationSessions(this);
        registerReceiver(installer.getPackageInstaller().getBroadcastReceiver(),
                new IntentFilter(InstallerService.ACTION_INSTALLATION_STATUS_NOTIFICATION));
        RxJavaPlugins.setErrorHandler(err -> {
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            unregisterReceiver(installer.getPackageInstaller().getBroadcastReceiver());
        } catch (Exception ignored) {
        }
    }
}
