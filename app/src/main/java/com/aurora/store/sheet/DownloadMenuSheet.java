

package com.aurora.store.sheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aurora.store.R;
import com.aurora.store.download.DownloadManager;
import com.aurora.store.utility.Util;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.Status;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadMenuSheet extends BottomSheetDialogFragment {

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    private Context context;
    private Fetch fetch;
    private Download download;

    public DownloadMenuSheet() {
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_download_menu, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetch = DownloadManager.getFetchInstance(context);

        if (download.getStatus() == Status.PAUSED
                || download.getStatus() == Status.COMPLETED
                || download.getStatus() == Status.CANCELLED) {
            navigationView.getMenu().findItem(R.id.action_pause).setVisible(false);
        }

        if (download.getStatus() == Status.DOWNLOADING
                || download.getStatus() == Status.COMPLETED
                || download.getStatus() == Status.QUEUED) {
            navigationView.getMenu().findItem(R.id.action_resume).setVisible(false);
        }

        if (download.getStatus() == Status.COMPLETED
                || download.getStatus() == Status.CANCELLED) {
            navigationView.getMenu().findItem(R.id.action_cancel).setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_copy:
                    Util.copyToClipBoard(context, download.getUrl());
                    Toast.makeText(context, context.getString(R.string.action_copied), Toast.LENGTH_LONG).show();
                    break;
                case R.id.action_pause:
                    fetch.pause(download.getId());
                    break;
                case R.id.action_resume:
                    if (download.getStatus() == Status.FAILED
                            || download.getStatus() == Status.CANCELLED)
                        fetch.retry(download.getId());
                    else
                        fetch.resume(download.getId());
                    break;
                case R.id.action_cancel:
                    fetch.cancel(download.getId());
                    break;
                case R.id.action_clear:
                    fetch.delete(download.getId());
                    break;
            }
            dismissAllowingStateLoss();
            return false;
        });
    }
}
