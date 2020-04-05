package com.mobilegenomics.f5n.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.activity.MainActivity;
import com.mobilegenomics.f5n.support.FileUtil;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

public class FragmentSettings extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_DOCUMENT_TREE = 148;

    private static final int REQUEST_PERMISSION_STORAGE = 158;

    private final String MOBILE_GENOMICS_FOLDER_PATH = Environment.getExternalStorageDirectory() + "/"
            + "mobile-genomics/";

    private final int DEFAULT_TIME_INTERVAL = 3;

    private String folderPath;

    private String[] timeUnits = {"Seconds", "Minutes"};

    private boolean dimScreen;

    private Preference logFileDirectoryPreference;

    private Preference storagePreference;

    private Preference referenceGnomePreference;

    private Preference timePreference;

    private Preference versionPreference;

    private Preference feedbackPreference;

    private SwitchPreference permissionStoragePreference;

    private SwitchPreference permissionSDCardWritePreference;

    private SwitchPreference permissionSystemSettingsWritePreference;

    private String storagePermission;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        storagePermission = (Manifest.permission.WRITE_EXTERNAL_STORAGE);

        logFileDirectoryPreference = findPreference(getResources().getString(R.string.key_log_file_preference));
        storagePreference = findPreference(getResources().getString(R.string.key_storage_preference));
        referenceGnomePreference = findPreference(getResources().getString(R.string.key_reference_gnome));
        timePreference = findPreference(getResources().getString(R.string.key_time_preference));
        versionPreference = findPreference(getResources().getString(R.string.key_version_preference));
        feedbackPreference = findPreference(getResources().getString(R.string.key_feedback_preference));
        permissionSDCardWritePreference = findPreference(
                getResources().getString(R.string.key_sdcard_storage_permission));
        permissionSystemSettingsWritePreference = findPreference(
                getResources().getString(R.string.key_write_settings_permission));
        permissionStoragePreference = findPreference(getResources().getString(R.string.key_storage_permission));

        if (checkPermission(storagePermission)) {
            permissionStoragePreference.setChecked(true);
        } else {
            permissionStoragePreference.setChecked(false);
        }

        permissionStoragePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                if (permissionStoragePreference.isChecked()) {
                    requestPermissions(new String[]{storagePermission}, REQUEST_PERMISSION_STORAGE);
                    // TODO Implement permission to Pre Marshmallow devices
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Uri uri = Uri.fromParts("package", getPackageName(), null);
//                    intent.setData(uri);
//                    startActivity(intent);
                } else {
                    permissionStoragePreference.setChecked(true);
                }
                return false;
            }
        });

        if (PreferenceUtil.getSharedPreferenceUri(R.string.sdcard_uri) != null) {
            permissionSDCardWritePreference.setChecked(true);
        }

        permissionSDCardWritePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                if (permissionSDCardWritePreference.isChecked()) {
                    AskSDCardPermission();
                } else {
                    PreferenceUtil.setSharedPreferenceUri(R.string.sdcard_uri, null);
                }
                return false;
            }
        });

        permissionSystemSettingsWritePreference
                .setChecked(PreferenceUtil.getSharedPreferenceBool(R.string.id_dim_screen));

        permissionSystemSettingsWritePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                if (permissionSystemSettingsWritePreference.isChecked()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        boolean settingsCanWrite = Settings.System.canWrite(getContext());
                        if (!settingsCanWrite) {
                            AskSystemSettingsWritePermission();
                        } else {
                            dimScreen = true;
                        }
                    } else {
                        dimScreen = true;
                    }
                } else {
                    dimScreen = false;
                }
                PreferenceUtil.setSharedPreferenceBool(R.string.id_dim_screen, dimScreen);
                return false;
            }
        });

        String storagePath = PreferenceUtil
                .getSharedPreferenceString(R.string.key_storage_preference, MOBILE_GENOMICS_FOLDER_PATH);
        storagePreference.setDefaultValue(storagePath);
        storagePreference.setSummary(storagePath);
        storagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setDataStorage();
                return true;
            }
        });

        String referenceGnomePath = PreferenceUtil
                .getSharedPreferenceString(R.string.key_reference_gnome, MOBILE_GENOMICS_FOLDER_PATH);
        referenceGnomePreference.setDefaultValue(referenceGnomePath);
        referenceGnomePreference.setSummary(referenceGnomePath);
        referenceGnomePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setReferenceGnome();
                return true;
            }
        });

        int timeInterval = PreferenceUtil
                .getSharedPreferenceInt(R.string.key_time_preference, DEFAULT_TIME_INTERVAL);
        timePreference.setDefaultValue(timeInterval);
        timePreference.setSummary(timeInterval + " " + timeUnits[0]);
        timePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setTimeInterval();
                return false;
            }
        });

        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            versionPreference.setSummary(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        feedbackPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sendFeedback();
                return true;
            }
        });
    }

    private void setDataStorage() {
        new ChooserDialog(getActivity())
                .withFilter(true, false)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        folderPath = path + "/";
                        storagePreference.setDefaultValue(folderPath);
                        storagePreference.setSummary(folderPath);
                        PreferenceUtil.setSharedPreferenceString(R.string.key_storage_preference, folderPath);
                    }
                })
                .build()
                .show();
    }

    private void setReferenceGnome() {
        new ChooserDialog(getActivity())
                .withFilter(true, false)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        folderPath = path + "/";
                        referenceGnomePreference.setDefaultValue(folderPath);
                        referenceGnomePreference.setSummary(folderPath);
                        PreferenceUtil.setSharedPreferenceString(R.string.key_reference_gnome, folderPath);
                    }
                })
                .build()
                .show();
    }

    private void setTimeInterval() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.time_preference, null, false);
        NumberPicker valuePicker = view.findViewById(R.id.picker_value);
        NumberPicker unitPicker = view.findViewById(R.id.picker_unit);
        valuePicker.setMaxValue(120);
        valuePicker.setMinValue(0);
        unitPicker.setDisplayedValues(timeUnits);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(1);
        new AlertDialog.Builder(getActivity())
                .setTitle("Set Time Interval")
                .setView(view)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        int value = valuePicker.getValue();
                        int unit = unitPicker.getValue();
                        timePreference.setSummary(value + " " + timeUnits[unit]);
                        if (unit == 1) {
                            value = value * 60;
                        }
                        timePreference.setDefaultValue(value);
                        PreferenceUtil.setSharedPreferenceInt(R.string.key_time_preference, value);
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.setData(Uri.parse("mailto:"));
        String[] to = {"hiruna72@gmail.com"};
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        intent.putExtra(Intent.EXTRA_SUBJECT, "f5n mobile-genomics");
        intent.putExtra(Intent.EXTRA_TEXT, "--Write your feedback here--");
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Send Feedback via Email"));
    }

    private void AskSDCardPermission() {
        if (FileUtil.isExternalSDCardAvailable(getContext())) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_CODE_DOCUMENT_TREE);
        } else {
            permissionSDCardWritePreference.setEnabled(false);
            permissionSDCardWritePreference.setSwitchTextOn("No SD Card found");
        }
    }

    private void AskSystemSettingsWritePermission() {

        Handler handler = new Handler();

        Runnable checkSettings = new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return;
                }
                boolean settingsCanWrite = Settings.System.canWrite(getContext());
                if (settingsCanWrite) {
                    PreferenceUtil.setSharedPreferenceBool(R.string.id_dim_screen, true);
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }
                handler.postDelayed(this, 1000);
            }
        };

        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + getContext().getPackageName()));
        startActivity(intent);
        handler.postDelayed(checkSettings, 1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DOCUMENT_TREE && resultCode == Activity.RESULT_OK && data != null) {
            // Get Uri from Storage Access Framework.
            Uri treeUri = data.getData();

            // Persist access permissions.
            getActivity().getContentResolver()
                    .takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            PreferenceUtil.setSharedPreferenceUri(R.string.sdcard_uri, treeUri);
        }
    }

    public boolean checkPermission(String permission) {

        return ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionStoragePreference.setChecked(true);
        } else {
            // We were not granted permission this time, so don't try to show the contact picker
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
