

package com.aurora.store.fragment.preference;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aurora.store.R;
import com.aurora.store.view.LinkView;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AboutFragment extends Fragment {

    @BindView(R.id.app_version)
    TextView txtVersion;
    @BindView(R.id.linkContainer)
    LinearLayout linkContainer;

    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        drawVersion();
        drawLinks();
    }

    private void drawVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            txtVersion.setText(new StringBuilder()
                    .append("v")
                    .append(packageInfo.versionName));
        } catch (Exception ignored) {
        }
    }

    private void drawLinks() {
        String[] linkURLS = getResources().getStringArray(R.array.linkURLS);
        String[] linkTitles = getResources().getStringArray(R.array.linkTitles);
        String[] linkSummary = getResources().getStringArray(R.array.linkSummary);
        int[] linkIcons = {
                R.drawable.ic_paypal,
                R.drawable.ic_gitlab,
                R.drawable.ic_xda,
                R.drawable.ic_telegram,
                R.drawable.ic_fdroid
        };
        for (int i = 0; i < linkURLS.length; i++)
            linkContainer.addView(new LinkView(getContext(),
                    linkURLS[i],
                    linkTitles[i],
                    linkSummary[i],
                    linkIcons[i]));
    }
}
