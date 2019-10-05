

package com.aurora.store.download;

import android.content.Context;

import com.aurora.store.model.App;
import com.aurora.store.utility.PathUtil;
import com.aurora.store.utility.TextUtil;
import com.aurora.store.utility.Util;
import com.dragons.aurora.playstoreapiv2.AndroidAppDeliveryData;
import com.dragons.aurora.playstoreapiv2.AppFileMetadata;
import com.dragons.aurora.playstoreapiv2.Split;
import com.tonyodev.fetch2.EnqueueAction;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;

import java.util.ArrayList;
import java.util.List;

public class RequestBuilder {

    /*
     *
     * Build Simple App Download Request from URL
     * @param Context - Application Context
     * @param App -  App object
     * @param Url -  APK Url
     * @return Request
     *
     */

    public static Request buildRequest(Context context, App app, String Url) {
        Request request;
        request = new Request(Url, PathUtil.getLocalApkPath(context, app));
        request.setPriority(Priority.HIGH);
        request.setEnqueueAction(EnqueueAction.UPDATE_ACCORDINGLY);
        request.setGroupId(app.getPackageName().hashCode());
        if (Util.isDownloadWifiOnly(context))
            request.setNetworkType(NetworkType.WIFI_ONLY);
        else
            request.setNetworkType(NetworkType.ALL);
        request.setTag(app.getPackageName());
        return request;
    }

    /*
     *
     * Build Bundled App Download Request from URL
     * @param Context - Application Context
     * @param App -  App object
     * @param Url -  APK Url
     * @return Request
     *
     */

    public static Request buildSplitRequest(Context context, App app, Split split) {
        Request request = new Request(split.getDownloadUrl(),
                PathUtil.getLocalSplitPath(context, app, split.getName()));
        request.setPriority(Priority.HIGH);
        request.setEnqueueAction(EnqueueAction.UPDATE_ACCORDINGLY);
        request.setGroupId(app.getPackageName().hashCode());
        if (Util.isDownloadWifiOnly(context))
            request.setNetworkType(NetworkType.WIFI_ONLY);
        else
            request.setNetworkType(NetworkType.ALL);
        request.setTag(app.getPackageName());
        return request;
    }

    /*
     *
     * Build Bundled App Download RequestList from SplitList
     * @param Context - Application Context
     * @param App -  App object
     * @param AndroidAppDeliveryData -  DeliveryData Objects
     * @return RequestList
     *
     */

    public static List<Request> buildSplitRequestList(Context context, App app,
                                                      AndroidAppDeliveryData deliveryData) {
        List<Split> splitList = deliveryData.getSplitList();
        List<Request> requestList = new ArrayList<>();
        for (Split split : splitList) {
            final Request request = buildSplitRequest(context, app, split);
            requestList.add(request);
        }
        return requestList;
    }

    /*
     *
     * Build Obb Download Request from URL
     * @param Context - Application Context
     * @param App -  App object
     * @param Url -  APK Url
     * @param isMain - boolean to determine obb type
     * @return Request
     *
     */

    public static Request buildObbRequest(Context context, App app, String Url, boolean isMain, boolean isGZipped) {
        Request request;
        request = new Request(Url, PathUtil.getObbPath(app, isMain, isGZipped));
        request.setEnqueueAction(EnqueueAction.UPDATE_ACCORDINGLY);
        request.setPriority(Priority.HIGH);
        request.setGroupId(app.getPackageName().hashCode());
        if (Util.isDownloadWifiOnly(context))
            request.setNetworkType(NetworkType.WIFI_ONLY);
        else
            request.setNetworkType(NetworkType.ALL);
        request.setTag(app.getPackageName());
        return request;
    }


    /*
     *
     * Build Obb App Download RequestList from DeliveryDataList and GroupId
     * @param Context - Application Context
     * @param AndroidAppDeliveryData -  App object
     * @param groupId - Request GroupId
     * @return RequestList
     *
     */

    public static List<Request> buildObbRequestList(Context context, App app, AndroidAppDeliveryData appDeliveryData) {
        List<Request> requestList = new ArrayList<>();
        List<AppFileMetadata> appFileMetadataList = appDeliveryData.getAdditionalFileList();
        if (appFileMetadataList.size() == 1) {
            AppFileMetadata obbFileMetadata = appDeliveryData.getAdditionalFile(0);
            if (TextUtil.isEmpty(obbFileMetadata.getDownloadUrlGzipped()))
                requestList.add(buildObbRequest(context, app, obbFileMetadata.getDownloadUrl(), true, false));
            else
                requestList.add(buildObbRequest(context, app, obbFileMetadata.getDownloadUrlGzipped(), true, true));
        }
        if (appFileMetadataList.size() == 2) {
            AppFileMetadata obbFileMetadata = appDeliveryData.getAdditionalFile(1);
            if (TextUtil.isEmpty(obbFileMetadata.getDownloadUrlGzipped()))
                requestList.add(buildObbRequest(context, app, obbFileMetadata.getDownloadUrl(), false, false));
            else
                requestList.add(buildObbRequest(context, app, obbFileMetadata.getDownloadUrlGzipped(), false, true));
        }
        return requestList;
    }
}

