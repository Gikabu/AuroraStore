

package com.aurora.store.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aurora.store.R;
import com.aurora.store.fragment.DetailsFragment;
import com.aurora.store.manager.BlacklistManager;
import com.aurora.store.manager.FavouriteListManager;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.ThemeUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    public static final String INTENT_PACKAGE_NAME = "INTENT_PACKAGE_NAME";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private String packageName;
    private DetailsFragment detailsFragment;
    private ThemeUtil themeUtil = new ThemeUtil();
    private FavouriteListManager favouriteListManager;

    static public Intent getDetailsIntent(Context context, String packageName) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(DetailsActivity.INTENT_PACKAGE_NAME, packageName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeUtil.onCreate(this);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setupActionBar();
        favouriteListManager = new FavouriteListManager(this);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        packageName = getIntentPackageName(intent);
        if (TextUtils.isEmpty(packageName)) {
            Log.e("No package name provided");
            finish();
            return;
        }
        Log.i("Getting info about %s", packageName);
        grabDetails(packageName);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_details, menu);
        menu.findItem(R.id.action_favourite).setIcon(favouriteListManager.contains(packageName)
                ? R.drawable.ic_favourite_red
                : R.drawable.ic_favourite_remove);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_favourite:
                if (favouriteListManager.contains(packageName)) {
                    favouriteListManager.remove(packageName);
                    menuItem.setIcon(R.drawable.ic_favourite_remove);
                } else {
                    favouriteListManager.add(packageName);
                    menuItem.setIcon(R.drawable.ic_favourite_red);
                }
                return true;
            case R.id.action_manual:
                startActivity(new Intent(this, ManualDownloadActivity.class));
                return true;
            case R.id.action_downloads:
                startActivity(new Intent(this, DownloadsActivity.class));
                return true;
            case R.id.action_blacklist:
                new BlacklistManager(this).add(packageName);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeUtil.onResume(this);
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.get(0) instanceof DetailsFragment) {
            FragmentManager fm = fragments.get(0).getChildFragmentManager();
            if (!fm.getFragments().isEmpty())
                fm.popBackStack();
            else
                super.onBackPressed();
        } else
            super.onBackPressed();
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private String getIntentPackageName(Intent intent) {
        if (intent.hasExtra(INTENT_PACKAGE_NAME)) {
            return intent.getStringExtra(INTENT_PACKAGE_NAME);
        } else if (intent.getScheme() != null
                && (intent.getScheme().equals("market")
                || intent.getScheme().equals("http")
                || intent.getScheme().equals("https"))) {
            return intent.getData().getQueryParameter("id");
        } else if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            return bundle.getString(INTENT_PACKAGE_NAME);
        }
        return null;
    }

    public void grabDetails(String packageName) {
        detailsFragment = new DetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putString("PackageName", packageName);
        detailsFragment.setArguments(arguments);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, detailsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

    public void redrawButtons() {
        if (detailsFragment != null)
            detailsFragment.drawButtons();
    }

}
