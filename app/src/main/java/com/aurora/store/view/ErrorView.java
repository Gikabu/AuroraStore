

package com.aurora.store.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.store.ErrorType;
import com.aurora.store.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ErrorView extends RelativeLayout {

    @BindView(R.id.img_err)
    ImageView imgError;
    @BindView(R.id.txt_err)
    TextView txtError;
    @BindView(R.id.btn_err)
    Button btnError;

    private ErrorType errorType;
    private View.OnClickListener errorListener;

    public ErrorView(Context context, ErrorType errorType,
                     OnClickListener errorListener) {
        super(context);
        this.errorType = errorType;
        this.errorListener = errorListener;
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context) {
        View view = inflate(context, R.layout.view_error, this);
        ButterKnife.bind(this, view);
        btnError.setOnClickListener(errorListener);
        switch (errorType) {
            case NO_NETWORK:
                imgError.setImageDrawable(context.getDrawable(R.drawable.ic_no_network));
                txtError.setText(R.string.error_no_network);
                break;
            case NO_APPS:
                imgError.setImageDrawable(context.getDrawable(R.drawable.ic_empty_box));
                txtError.setText(R.string.error_app_not_found);
                break;
            case NO_UPDATES:
                btnError.setText(context.getString(R.string.action_recheck));
                imgError.setImageDrawable(context.getDrawable(R.drawable.ic_no_updates));
                txtError.setText(R.string.list_empty_updates);
                break;
            case NO_SEARCH:
                imgError.setImageDrawable(context.getDrawable(R.drawable.ic_search_alt));
                txtError.setText(R.string.error_search_not_found);
                break;
            case UNKNOWN:
                imgError.setImageDrawable(context.getDrawable(R.drawable.ic_unknown));
                txtError.setText(R.string.error_unknown);
                break;
            case APP_NOT_FOUND:
                imgError.setImageDrawable(context.getDrawable(R.drawable.ic_unknown));
                txtError.setText(R.string.error_app_not_found);
                btnError.setText(R.string.action_close);
                break;
            case LOGOUT_ERR:
                imgError.setImageDrawable(context.getDrawable(R.drawable.ic_login));
                txtError.setText(R.string.error_logged_out);
                btnError.setText(R.string.action_login);
                break;
            case NO_DOWNLOADS:
                imgError.setImageDrawable(context.getDrawable(R.drawable.ic_download_alt));
                txtError.setText(R.string.download_none);
                btnError.setVisibility(GONE);
                break;
            case IMPORT:
                btnError.setText(context.getString(R.string.action_import));
                imgError.setImageDrawable(context.getDrawable(R.drawable.ic_empty_box));
                txtError.setText(R.string.list_empty_fav);
                break;
        }
    }
}
