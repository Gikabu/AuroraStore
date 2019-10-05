

package com.aurora.store.fragment.details;

import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.aurora.store.R;
import com.aurora.store.fragment.DetailsFragment;
import com.aurora.store.model.App;
import com.aurora.store.model.ExodusReport;
import com.aurora.store.model.Report;
import com.aurora.store.sheet.ExodusBottomSheet;
import com.aurora.store.task.NetworkTask;
import com.aurora.store.utility.Log;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ExodusPrivacy extends AbstractHelper {

    public static final String EXODUS_PATH = "https://reports.exodus-privacy.eu.org/api/search/";

    @BindView(R.id.exodus_card)
    RelativeLayout exodus_card;
    @BindView(R.id.moreButton)
    Button moreButton;

    private Report report;
    private CompositeDisposable disposable = new CompositeDisposable();

    public ExodusPrivacy(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {
        ButterKnife.bind(this, view);
        get(EXODUS_PATH + app.getPackageName());
    }

    private void get(String url) {
        disposable.add(Observable.fromCallable(() -> new NetworkTask(context)
                .get(url))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject exodusObject = jsonObject.getJSONObject(app.getPackageName());
                    Gson gson = new Gson();
                    ExodusReport exodusReport = gson.fromJson(exodusObject.toString(), ExodusReport.class);
                    List<Report> reportList = exodusReport.getReports();
                    Collections.sort(reportList, (Report1, Report2) ->
                            Report2.getCreationDate().compareTo(Report1.getCreationDate()));
                    report = reportList.get(0);
                    drawExodus(report);
                }, throwable -> {
                    Log.i("Error occurred at generating report");
                }));
    }

    private void drawExodus(Report mReport) {
        if (context != null) {
            exodus_card.setVisibility(View.VISIBLE);
            if (mReport.getTrackers().size() > 0) {
                setText(view, R.id.exodus_description,
                        new StringBuilder()
                                .append(context.getString(R.string.exodus_hasTracker))
                                .append(StringUtils.SPACE)
                                .append(mReport.getTrackers().size()).toString());
            } else {
                setText(view, R.id.exodus_description, R.string.exodus_noTracker);
            }

            if (mReport.getTrackers().isEmpty())
                moreButton.setVisibility(View.GONE);
            else
                moreButton.setOnClickListener(v -> showBottomDialog());
        }
    }

    private void showBottomDialog() {
        ExodusBottomSheet bottomSheet = new ExodusBottomSheet(report);
        bottomSheet.show(fragment.getChildFragmentManager(), "EXODUS");
    }
}
