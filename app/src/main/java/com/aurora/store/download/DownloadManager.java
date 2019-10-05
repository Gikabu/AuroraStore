

package com.aurora.store.download;

import android.content.Context;

import com.aurora.store.Constants;
import com.aurora.store.utility.Util;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import okhttp3.OkHttpClient;

public class DownloadManager {

    private static volatile DownloadManager instance;
    private static Fetch fetch;

    public DownloadManager() {
        if (instance != null) {
            throw new RuntimeException("Use get() method to get the single instance of RxBus");
        }
    }

    public static Fetch getFetchInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                    fetch = getFetch(context);
                }
            }
        }
        return fetch;
    }

    private static Fetch getFetch(Context context) {
        FetchConfiguration.Builder fetchConfiguration = new FetchConfiguration.Builder(context)
                .setDownloadConcurrentLimit(Util.getActiveDownloadCount(context))
                .setHttpDownloader(new OkHttpDownloader(getOkHttpClient(context), Util.getDownloadStrategy(context)))
                .setNamespace(Constants.TAG)
                .enableLogging(Util.isFetchDebugEnabled(context))
                .enableHashCheck(true)
                .enableFileExistChecks(true)
                .enableRetryOnNetworkGain(true)
                .enableAutoStart(true)
                .setAutoRetryMaxAttempts(3)
                .setProgressReportingInterval(3000);
        return Fetch.Impl.getInstance(fetchConfiguration.build());
    }

    private static OkHttpClient getOkHttpClient(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (Util.isNetworkProxyEnabled(context))
            builder.proxy(Util.getNetworkProxy(context));
        return builder.build();
    }
}

