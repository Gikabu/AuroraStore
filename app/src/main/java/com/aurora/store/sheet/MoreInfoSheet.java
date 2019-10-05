

package com.aurora.store.sheet;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aurora.store.R;
import com.aurora.store.model.App;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoreInfoSheet extends BottomSheetDialogFragment {

    @BindView(R.id.content_readMore)
    TextView contentReadMore;

    private App app;

    public MoreInfoSheet() {
    }

    public void setApp(App app) {
        this.app = app;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_read_more, container, false);
        ButterKnife.bind(this, view);
        view.setFitsSystemWindows(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            contentReadMore.setText(Html.fromHtml(app.getDescription(), Html.FROM_HTML_MODE_LEGACY).toString());
        else
            contentReadMore.setText(Html.fromHtml(app.getDescription()).toString());
    }
}
