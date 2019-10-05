

package com.aurora.store.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.store.R;
import com.aurora.store.SharedPreferencesTranslator;
import com.aurora.store.fragment.CategoryAppsFragment;
import com.aurora.store.utility.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoriesListAdapter extends RecyclerView.Adapter<CategoriesListAdapter.ViewHolder> {

    private Context context;
    private Fragment fragment;
    private Map<String, String> categories = new HashMap<>();

    private Integer[] categoriesImg = {
            R.drawable.ic_android_wear,
            R.drawable.ic_art_design,
            R.drawable.ic_auto_vehicles,
            R.drawable.ic_beauty,
            R.drawable.ic_books_reference,
            R.drawable.ic_business,
            R.drawable.ic_comics,
            R.drawable.ic_communication,
            R.drawable.ic_dating,
            R.drawable.ic_education,
            R.drawable.ic_entertainment,
            R.drawable.ic_events,
            R.drawable.ic_family,
            R.drawable.ic_finance,
            R.drawable.ic_food_drink,
            R.drawable.ic_games,
            R.drawable.ic_health_fitness,
            R.drawable.ic_house_home,
            R.drawable.ic_libraries_demo,
            R.drawable.ic_lifestyle,
            R.drawable.ic_maps_navigation,
            R.drawable.ic_medical,
            R.drawable.ic_music__audio,
            R.drawable.ic_news_magazines,
            R.drawable.ic_parenting,
            R.drawable.ic_personalization,
            R.drawable.ic_photography,
            R.drawable.ic_productivity,
            R.drawable.ic_shopping,
            R.drawable.ic_social,
            R.drawable.ic_sports,
            R.drawable.ic_tools,
            R.drawable.ic_travel_local,
            R.drawable.ic_video_editors,
            R.drawable.ic_weather,
    };

    private SharedPreferencesTranslator translator;

    public CategoriesListAdapter(Fragment fragment) {
        this.fragment = fragment;
        this.context = fragment.getContext();
        translator = new SharedPreferencesTranslator(Util.getPrefs(context));
    }

    public void addData(Map<String, String> categories) {
        this.categories.clear();
        this.categories = categories;
        notifyDataSetChanged();
    }

    public boolean isDataEmpty() {
        return categories.isEmpty();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.topLabel.setText(translator.getString(new ArrayList<>(categories.keySet()).get(position)));
        holder.topImage.setImageDrawable(context.getResources().getDrawable(categoriesImg[position]));
        holder.itemView.setOnClickListener(v -> {
            CategoryAppsFragment categoryAppsFragment = new CategoryAppsFragment();
            Bundle arguments = new Bundle();
            arguments.putString("CategoryId", new ArrayList<>(categories.keySet()).get(position));
            arguments.putString("CategoryName", translator.getString(new ArrayList<>(categories.keySet()).get(position)));
            categoryAppsFragment.setArguments(arguments);
            FragmentManager fragmentManager = fragment.getFragmentManager();
            if (fragmentManager != null)
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.coordinator, categoryAppsFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.all_cat_img)
        ImageView topImage;
        @BindView(R.id.all_cat_name)
        TextView topLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
