

package com.aurora.store.fragment.details;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.store.R;
import com.aurora.store.adapter.SmallScreenshotsAdapter;
import com.aurora.store.fragment.DetailsFragment;
import com.aurora.store.model.App;

import butterknife.BindView;

public class Screenshot extends AbstractHelper {

    @BindView(R.id.screenshots_gallery)
    RecyclerView recyclerView;

    public Screenshot(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {
        if (app.getScreenshotUrls().size() > 0) {
            drawGallery();
        }
    }

    private void drawGallery() {
        recyclerView.setAdapter(new SmallScreenshotsAdapter(app.getScreenshotUrls(), context));
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    }
}