

package com.aurora.store.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.aurora.store.R;

public class LinkView extends RelativeLayout {

    Context context;
    CardView link_card;
    ImageView card_icon;
    TextView card_title;
    TextView card_summary;

    private String title;
    private String summary;
    private String linkURL;
    private int cardIconID;

    public LinkView(Context context, String linkURL, String title, String summary, int cardIconID) {
        super(context);
        this.context = context;
        this.linkURL = linkURL;
        this.title = title;
        this.summary = summary;
        this.cardIconID = cardIconID;
        init(context);
    }

    public LinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View view = inflate(context, R.layout.item_link, this);
        card_icon = view.findViewById(R.id.link_icon);
        card_title = view.findViewById(R.id.link_title);
        card_summary = view.findViewById(R.id.link_summary);
        card_title.setText(title);
        card_summary.setText(summary);
        card_icon.setImageResource(cardIconID);

        view.setOnClickListener(click -> {
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(linkURL));
            context.startActivity(browserIntent);
        });
    }
}
