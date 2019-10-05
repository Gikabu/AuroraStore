

package com.aurora.store.task;

import android.content.Context;

import com.aurora.store.Constants;
import com.aurora.store.exception.NotPurchasedException;
import com.aurora.store.model.App;
import com.aurora.store.utility.Accountant;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.PrefUtil;
import com.dragons.aurora.playstoreapiv2.AndroidAppDeliveryData;
import com.dragons.aurora.playstoreapiv2.BuyResponse;
import com.dragons.aurora.playstoreapiv2.DeliveryResponse;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;

import java.io.IOException;

import io.reactivex.Observable;

public class ObservableDeliveryData extends BaseTask {

    public AndroidAppDeliveryData deliveryData;
    private String downloadToken;

    public ObservableDeliveryData(Context context) {
        super(context);
    }

    public Observable<DeliveryDataBundle> getDeliveryData(App app) {
        return Observable.create(emitter -> {
            GooglePlayAPI api = getApi();
            purchase(api, app);
            delivery(api, app);
            DeliveryDataBundle deliveryDataBundle = new DeliveryDataBundle(app, deliveryData);
            emitter.onNext(deliveryDataBundle);
            emitter.onComplete();
        });
    }

    public void purchase(GooglePlayAPI api, App app) {
        try {
            BuyResponse buyResponse = api.purchase(app.getPackageName(), app.getVersionCode(), app.getOfferType());
            if (buyResponse.hasPurchaseStatusResponse()
                    && buyResponse.getPurchaseStatusResponse().hasAppDeliveryData()
                    && buyResponse.getPurchaseStatusResponse().getAppDeliveryData().hasDownloadUrl()) {
                deliveryData = buyResponse.getPurchaseStatusResponse().getAppDeliveryData();
            }
            if (buyResponse.hasDownloadToken()) {
                downloadToken = buyResponse.getDownloadToken();
            }
        } catch (IOException e) {
            Log.d("Failed to purchase %s", app.getDisplayName());
        }
    }

    void delivery(GooglePlayAPI api, App app) throws IOException {
        DeliveryResponse deliveryResponse = api.delivery(
                app.getPackageName(),
                shouldDownloadDelta(app) ? app.getInstalledVersionCode() : 0,
                app.getVersionCode(),
                app.getOfferType(),
                downloadToken
        );
        if (deliveryResponse.hasAppDeliveryData()
                && deliveryResponse.getAppDeliveryData().hasDownloadUrl()) {
            deliveryData = deliveryResponse.getAppDeliveryData();
        } else if (!app.isFree() && Accountant.isDummy(context)) {
            throw new NotPurchasedException();
        } else if (deliveryData == null) {
            Log.d("No download link returned");
        }
    }

    private boolean shouldDownloadDelta(App app) {
        return PrefUtil.getBoolean(context, Constants.PREFERENCE_DOWNLOAD_DELTAS)
                && app.getInstalledVersionCode() < app.getVersionCode();
    }

    public class DeliveryDataBundle {
        private App app;
        private AndroidAppDeliveryData androidAppDeliveryData;

        public DeliveryDataBundle(App app, AndroidAppDeliveryData androidAppDeliveryData) {
            this.app = app;
            this.androidAppDeliveryData = androidAppDeliveryData;
        }

        public App getApp() {
            return app;
        }

        public AndroidAppDeliveryData getAndroidAppDeliveryData() {
            return androidAppDeliveryData;
        }
    }
}
