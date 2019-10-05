

package com.aurora.store.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.store.GlideApp;
import com.aurora.store.R;
import com.aurora.store.activity.FullscreenImageActivity;
import com.aurora.store.utility.ViewUtil;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SmallScreenshotsAdapter extends RecyclerView.Adapter<SmallScreenshotsAdapter.ViewHolder> {

    private List<String> URLs;
    private Context context;

    public SmallScreenshotsAdapter(List<String> URLs, Context context) {
        this.URLs = URLs;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_screenshots_small, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        GlideApp.with(context)
                .load(URLs.get(position)).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .transition(new DrawableTransitionOptions().crossFade())
                .transforms(new CenterCrop(), new RoundedCorners(25))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition<? super Drawable> transition) {
                        holder.imageView.getLayoutParams().width = drawable.getIntrinsicWidth();
                        holder.imageView.getLayoutParams().height = drawable.getIntrinsicHeight();
                        holder.imageView.setImageDrawable(drawable);
                    }
                });

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullscreenImageActivity.class);
            intent.putExtra(FullscreenImageActivity.INTENT_SCREENSHOT_NUMBER, position);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return URLs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.screenshot_img)
        ImageView imageView;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}

