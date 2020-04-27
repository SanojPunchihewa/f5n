package com.mobilegenomics.f5n.fragments;

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
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.activity.MainActivity;
import com.mobilegenomics.f5n.core.PipelineType;
import com.mobilegenomics.f5n.support.FileUtil;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import com.obsez.android.lib.filechooser.ChooserDialog;
import java.io.File;

public class FragmentSettings extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_DOCUMENT_TREE = 148;

    private final String MOBILE_GENOMICS_FOLDER_PATH = Environment.getExternalStorageDirectory() + "/"
            + "mobile-genomics/";

    private String folderPath;

    private boolean dimScreen;

    private Preference storagePreference;

    private Preference referenceGnomePreference;

    private Preference versionPreference;

    private Preference feedbackPreference;

    private SwitchPreference permissionSDCardWritePreference;

    private SwitchPreference permissionSystemSettingsWritePreference;

    private ListPreference pipelineTypePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        storagePreference = findPreference(getResources().getString(R.string.key_storage_preference));
        referenceGnomePreference = findPreference(getResources().getString(R.string.key_reference_gnome));
        versionPreference = findPreference(getResources().getString(R.string.key_version_preference));
        feedbackPreference = findPreference(getResources().getString(R.string.key_feedback_preference));
        permissionSDCardWritePreference = findPreference(
                getResources().getString(R.string.key_sdcard_storage_permission));
        permissionSystemSettingsWritePreference = findPreference(
                getResources().getString(R.string.key_write_settings_permission));
        pipelineTypePreference = findPreference(getResources().getString(R.string.key_pipeline_type));

        int pipelineType = PreferenceUtil.getSharedPreferenceInt(R.string.key_pipeline_type_preference);

        if (pipelineType == -1) {
            pipelineType = 0;
        }

        pipelineTypePreference.setValueIndex(pipelineType);

        if (pipelineType == PipelineType.PIPELINE_METHYLATION.ordinal()) {
            pipelineTypePreference.setSummary(getResources().getStringArray(R.array.pipelineListArray)[0]);
        } else if (pipelineType == PipelineType.PIPELINE_VARIANT.ordinal()) {
            pipelineTypePreference.setSummary(getResources().getStringArray(R.array.pipelineListArray)[1]);
        } else {
            pipelineTypePreference.setSummary(getResources().getStringArray(R.array.pipelineListArray)[2]);
        }

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

        pipelineTypePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                int value = Integer.valueOf(newValue.toString());
                showSettingsApplyDialog(value);
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

    private void showSettingsApplyDialog(int type) {

        String strType;

        if (type == PipelineType.PIPELINE_METHYLATION.ordinal()) {
            strType = "METHYLATION CALLING";
        } else if (type == PipelineType.PIPELINE_VARIANT.ordinal()) {
            strType = "VARIANT CALLING";
        } else {
            strType = "ARTIC";
        }

        String message = "Pipeline type will be changed to " + strType
                + " the next time you start F5N. To immediately change the pipeline type, please restart F5N manually";

        new AlertDialog.Builder(getActivity())
                .setTitle("Change Pipeline Type")
                .setMessage(message)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceUtil.setSharedPreferenceInt(R.string.key_pipeline_type_temp_preference, type);
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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

}
