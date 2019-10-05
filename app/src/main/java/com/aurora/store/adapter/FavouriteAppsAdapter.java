

package com.aurora.store.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.store.R;
import com.aurora.store.manager.FavouriteListManager;
import com.aurora.store.model.App;
import com.aurora.store.utility.PackageUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavouriteAppsAdapter extends RecyclerView.Adapter {

    private List<App> appList = new ArrayList<>();
    private List<App> selectedList = new ArrayList<>();
    private Context context;
    private FavouriteListManager manager;
    private SelectableViewHolder.ItemClickListener itemClickListener;

    public FavouriteAppsAdapter(Context context, SelectableViewHolder.ItemClickListener itemClickListener, List<App> appsToAdd) {
        this.itemClickListener = itemClickListener;
        this.context = context;
        manager = new FavouriteListManager(context);
        appList.addAll(appsToAdd);
        Collections.sort(appsToAdd, (App1, App2) ->
                App1.getDisplayName().compareToIgnoreCase(App2.getDisplayName()));
    }

    public void add(int position, App app) {
        appList.add(position, app);
        notifyItemInserted(position);
    }

    public void add(App app) {
        appList.add(app);
    }

    public void remove(int position) {
        manager.remove(appList.get(position).getPackageName());
        appList.remove(position);
        notifyItemRemoved(position);
    }

    public boolean isEmpty() {
        return appList.isEmpty();
    }

    @NonNull
    @Override
    public SelectableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_favorite, parent, false);
        return new SelectableViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        SelectableViewHolder holder = (SelectableViewHolder) viewHolder;
        App app = appList.get(position);

        holder.AppTitle.setText(app.getDisplayName());

        if (PackageUtil.isInstalled(context, app)) {
            holder.AppExtra.setText(context.getText(R.string.list_installed));
            holder.AppCheckbox.setEnabled(false);
        } else {
            holder.AppExtra.setText(context.getText(R.string.list_not_installd));
        }

        Glide
                .with(context)
                .load(app.getIconInfo().getUrl())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .transition(new DrawableTransitionOptions().crossFade())
                .transforms(new CenterCrop(), new RoundedCorners(25))
                .into(holder.AppIcon);

        holder.setChecked(isSelected(appList.get(position)));
    }

    public void toggleSelection(int position) {
        App app = appList.get(position);
        if (selectedList.contains(app)) {
            selectedList.remove(app);
        } else {
            selectedList.add(app);
        }
        notifyItemChanged(position);
    }

    private boolean isSelected(App app) {
        return selectedList.contains(app);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public List<App> getSelectedList() {
        return new ArrayList<>(selectedList);
    }
}
