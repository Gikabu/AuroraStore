

package com.aurora.store.task;

import android.content.Context;

import com.aurora.store.model.App;
import com.aurora.store.model.AppBuilder;
import com.aurora.store.model.Review;
import com.aurora.store.model.ReviewBuilder;
import com.aurora.store.utility.Accountant;
import com.aurora.store.utility.PackageUtil;
import com.dragons.aurora.playstoreapiv2.DetailsResponse;
import com.dragons.aurora.playstoreapiv2.ReviewResponse;

public class DetailsApp extends BaseTask {

    public DetailsApp(Context context) {
        super(context);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public App getInfo(String packageName) throws Exception {
        api = getApi();
        DetailsResponse response = api.details(packageName);
        App app = AppBuilder.build(response);
        if (app.getUserReview() != null && Accountant.isGoogle(context)) {
            ReviewResponse reviewResponse = api.getReview(app.getPackageName());
            if (reviewResponse.hasGetResponse() && reviewResponse.getGetResponse().getReviewCount() == 1) {
                Review review = ReviewBuilder.build(reviewResponse.getGetResponse().getReview(0));
                app.setUserReview(review);
            }
        }
        if (PackageUtil.isInstalled(context, app))
            app.setInstalled(true);
        return app;
    }
}