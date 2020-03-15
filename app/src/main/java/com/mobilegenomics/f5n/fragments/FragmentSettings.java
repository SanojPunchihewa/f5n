package com.mobilegenomics.f5n.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

public class FragmentSettings extends PreferenceFragmentCompat {

    private final String MOBILE_GENOMICS_FOLDER_PATH = Environment.getExternalStorageDirectory() + "/" + "mobile-genomics/";

    private String folderPath;
    private Preference storagePreference;
    private Preference versionPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        storagePreference = findPreference(getResources().getString(R.string.key_storage_preference));
        versionPreference = findPreference(getResources().getString(R.string.key_version_preference));
        storagePreference.setDefaultValue(MOBILE_GENOMICS_FOLDER_PATH);
        storagePreference.setSummary(MOBILE_GENOMICS_FOLDER_PATH);
        storagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openFileManager();
                return true;
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
    }

    private void openFileManager() {
        new ChooserDialog(getActivity())
                .withFilter(true, false)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        folderPath = path;
                        storagePreference.setDefaultValue(folderPath);
                        storagePreference.setSummary(folderPath);
                        PreferenceUtil.setSharedPreferenceString(R.string.key_storage_preference, folderPath);
                    }
                })
                .build()
                .show();
    }
}
