

package com.aurora.store.fragment.preference;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aurora.store.Constants;
import com.aurora.store.R;
import com.aurora.store.activity.AccountsActivity;
import com.aurora.store.activity.DeviceInfoActivity;
import com.aurora.store.api.PlayStoreApiAuthenticator;
import com.aurora.store.manager.CategoryManager;
import com.aurora.store.manager.SpoofManager;
import com.aurora.store.notification.QuickNotification;
import com.aurora.store.task.DeviceInfoBuilder;
import com.aurora.store.task.GeoSpoofTask;
import com.aurora.store.utility.Accountant;
import com.aurora.store.utility.ContextUtil;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.PrefUtil;
import com.aurora.store.utility.Util;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SpoofFragment extends Fragment {

    @BindView(R.id.device_avatar)
    ImageView imgDeviceAvatar;
    @BindView(R.id.device_model)
    TextView txtDeviceModel;
    @BindView(R.id.device_info)
    TextView txtDeviceInfo;
    @BindView(R.id.spoof_device)
    Spinner mSpinnerDevice;
    @BindView(R.id.spoof_language)
    Spinner mSpinnerLanguage;
    @BindView(R.id.spoof_location)
    Spinner mSpinnerLocation;
    @BindView(R.id.export_fab)
    ExtendedFloatingActionButton exportFab;

    private Context context;
    private CompositeDisposable disposable = new CompositeDisposable();
    private String deviceName;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_spoof, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isSpoofed())
            drawSpoofedDevice();
        else
            drawDevice();

        setupDevice();
        setupLanguage();
        setupLocations();
        setupDeviceConfigExport();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSpoofed())
            drawSpoofedDevice();
        else
            drawDevice();
    }

    private boolean isSpoofed() {
        deviceName = PrefUtil.getString(context, Constants.PREFERENCE_DEVICE_TO_PRETEND_TO_BE);
        return (deviceName.contains("device-"));
    }

    private void drawDevice() {
        txtDeviceModel.setText(new StringBuilder()
                .append(Build.MODEL)
                .append(" | ")
                .append(Build.DEVICE));

        txtDeviceInfo.setText(new StringBuilder()
                .append(Build.MANUFACTURER)
                .append(" | ")
                .append(Build.BOARD));
    }

    private void drawSpoofedDevice() {
        Properties properties = new SpoofManager(this.context).getProperties(deviceName);
        String Model = properties.getProperty("UserReadableName");
        txtDeviceModel.setText(new StringBuilder()
                .append(Model.substring(0, Model.indexOf('(')))
                .append(" | ")
                .append(properties.getProperty(Constants.BUILD_DEVICE)));

        txtDeviceInfo.setText(new StringBuilder()
                .append(properties.getProperty(Constants.BUILD_MANUFACTURER))
                .append(" | ")
                .append(properties.getProperty(Constants.BUILD_HARDWARE)));
    }

    private void setupDevice() {
        Map<String, String> devices = getDeviceKeyValueMap();

        String[] deviceList = devices.values().toArray(new String[0]);
        String[] deviceKeys = devices.keySet().toArray(new String[0]);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                deviceList
        );

        adapter.setDropDownViewResource(R.layout.item_spinner);
        mSpinnerDevice.setAdapter(adapter);
        mSpinnerDevice.setSelection(PrefUtil.getInteger(context, Constants.PREFERENCE_DEVICE_TO_PRETEND_TO_BE_INDEX), true);
        mSpinnerDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Intent i = new Intent(context, DeviceInfoActivity.class);
                    i.putExtra(Constants.INTENT_DEVICE_NAME, deviceKeys[position]);
                    i.putExtra(Constants.INTENT_DEVICE_INDEX, position);
                    context.startActivity(i);
                }
                if (position == 0) {
                    showConfirmationDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupLanguage() {
        Map<String, String> locales = getLanguageKeyValueMap();
        String[] localeList = locales.values().toArray(new String[0]);
        String[] localeKeys = locales.keySet().toArray(new String[0]);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                localeList
        );

        adapter.setDropDownViewResource(R.layout.item_spinner);
        mSpinnerLanguage.setAdapter(adapter);
        mSpinnerLanguage.setSelection(PrefUtil.getInteger(context,
                Constants.PREFERENCE_REQUESTED_LANGUAGE_INDEX), true);
        mSpinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    PlayStoreApiAuthenticator.getApi(context)
                            .setLocale(new Locale(localeKeys[position]));
                    PrefUtil.putString(context, Constants.PREFERENCE_REQUESTED_LANGUAGE,
                            localeKeys[position]);
                    PrefUtil.putInteger(context, Constants.PREFERENCE_REQUESTED_LANGUAGE_INDEX,
                            position);
                }

                if (position == 0) {
                    PrefUtil.putString(context, Constants.PREFERENCE_REQUESTED_LANGUAGE, "");
                    PrefUtil.putInteger(context, Constants.PREFERENCE_REQUESTED_LANGUAGE_INDEX,
                            0);
                }
                new CategoryManager(context).clearAll();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupLocations() {
        String[] geoLocations = context.getResources().getStringArray(R.array.geoLocation);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                geoLocations);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        mSpinnerLocation.setAdapter(adapter);
        mSpinnerLocation.setSelection(PrefUtil.getInteger(context,
                Constants.PREFERENCE_REQUESTED_LOCATION_INDEX), true);
        mSpinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String mLocation = geoLocations[position];
                GeoSpoofTask mGeoSpoofTask = new GeoSpoofTask(context, mLocation, position);
                disposable.add(Observable.fromCallable(mGeoSpoofTask::spoof)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((success) -> {
                            QuickNotification.show(
                                    context,
                                    "Aurora Location Spoof",
                                    "Current Location : " + mLocation,
                                    null);
                            Util.clearCache(context);
                        }, err -> Log.e(err.getMessage())));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private Map<String, String> getDeviceKeyValueMap() {
        Map<String, String> devices = new SpoofManager(context).getDevices();
        devices = Util.sort(devices);
        Util.addToStart((LinkedHashMap<String, String>) devices,
                "",
                context.getString(R.string.pref_device_to_pretend_to_be_default));
        return devices;
    }

    private Map<String, String> getLanguageKeyValueMap() {
        Map<String, String> languages = new HashMap<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            languages.put(locale.toString(), locale.getDisplayName());
        }
        languages = Util.sort(languages);
        Util.addToStart((LinkedHashMap<String, String>) languages, "",
                context.getString(R.string.pref_requested_language_default));
        return languages;
    }

    private void showConfirmationDialog() {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.dialog_title_logout))
                .setMessage(getString(R.string.pref_device_to_pretend_to_be_toast))
                .setPositiveButton(getString(R.string.action_logout), (dialog, which) -> {
                    PrefUtil.putString(context,
                            Constants.PREFERENCE_DEVICE_TO_PRETEND_TO_BE, "");
                    PrefUtil.putInteger(context,
                            Constants.PREFERENCE_DEVICE_TO_PRETEND_TO_BE_INDEX, 0);
                    Accountant.completeCheckout(context);
                    Util.clearCache(context);
                    startActivity(new Intent(context, AccountsActivity.class));
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> {

                });
        mBuilder.create();
        mBuilder.show();
    }

    private void setupDeviceConfigExport() {
        exportFab.setOnClickListener(v -> {
            disposable.add(Observable.fromCallable(() -> new DeviceInfoBuilder(context)
                    .build())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success -> {
                        ContextUtil.toast(context, success
                                ? R.string.action_export_info
                                : R.string.action_export_info_failed);
                    }));
        });
    }
}
