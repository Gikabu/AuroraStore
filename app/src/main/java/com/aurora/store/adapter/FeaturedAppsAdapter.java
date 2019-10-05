

package com.aurora.store.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.store.CardType;
import com.aurora.store.GlideApp;
import com.aurora.store.R;
import com.aurora.store.activity.DetailsActivity;
import com.aurora.store.model.App;
import com.aurora.store.utility.PackageUtil;
import com.aurora.store.utility.Util;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeaturedAppsAdapter extends RecyclerView.Adapter<FeaturedAppsAdapter.ViewHolder> {

    private Context context;
    private CardType cardType;
    private List<App> appList = new ArrayList<>();

    public FeaturedAppsAdapter(Context context) {
        this.context = context;
        this.cardType = Util.isLegacyCardEnabled(context) ? CardType.LEGACY : CardType.MODERN;
    }

    public void addData(List<App> appList) {
        this.appList.clear();
        this.appList = appList;
        notifyDataSetChanged();
    }

    public boolean isDataEmpty() {
        return appList.isEmpty();
    }

    @NonNull
    @Override
    public FeaturedAppsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(cardType == CardType.MODERN
                ? R.layout.item_featured
                : R.layout.item_cluster, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        final App app = appList.get(position);
        viewHolder.txtName.setText(app.getDisplayName());
        if (app.getPageBackgroundImage() != null)
            drawBackground(app, viewHolder);

        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("INTENT_PACKAGE_NAME", app.getPackageName());
            context.startActivity(intent);
        });

        viewHolder.txtIndicator.setVisibility(PackageUtil.isInstalled(context, app)
                ? View.VISIBLE
                : View.GONE);

        if (viewHolder.txtSize != null)
            viewHolder.txtSize.setText(Util.humanReadableByteValue(app.getSize(), true));
    }

    private void drawBackground(App app, ViewHolder holder) {
        GlideApp
                .with(context)
                .asBitmap()
                .load(cardType == CardType.MODERN
                        ? app.getPageBackgroundImage().getUrl()
                        : app.getIconInfo().getUrl())
                .placeholder(R.color.colorTransparent)
                .transition(new BitmapTransitionOptions().crossFade())
                .transforms(new CenterCrop(), new RoundedCorners(50))
                .into(holder.imgIcon);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.app_icon)
        ImageView imgIcon;
        @BindView(R.id.app_name)
        TextView txtName;
        @Nullable
        @BindView(R.id.app_size)
        TextView txtSize;
        @BindView(R.id.txt_indicator)
        TextView txtIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            int orientation = Resources.getSystem().getConfiguration().orientation;
            if (cardType == CardType.MODERN) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    itemView.getLayoutParams().width = (Resources.getSystem()
                            .getDisplayMetrics().widthPixels) - 64/*Padding & Margins*/;
                    imgIcon.getLayoutParams().height = (Resources.getSystem()
                            .getDisplayMetrics().heightPixels) / 4;
                } else {
                    itemView.getLayoutParams().width = ((Resources.getSystem()
                            .getDisplayMetrics().widthPixels) / 2) - 64 /*Padding & Margins*/;
                    imgIcon.getLayoutParams().height = (Resources.getSystem()
                            .getDisplayMetrics().heightPixels) / 2;
                }
            }
        }
    }
}