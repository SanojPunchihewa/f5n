package com.mobilegenomics.f5n.fragments;

import android.os.Bundle;
import android.os.Environment;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.mobilegenomics.f5n.R;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

public class FragmentSettings extends PreferenceFragmentCompat {

    final String MOBILE_GENOMICS_FOLDER_PATH = Environment.getExternalStorageDirectory() + "/" + "mobile-genomics/";

    private String folderPath;
    private Preference storagePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        storagePreference = findPreference(getResources().getString(R.string.key_default_storage));
        storagePreference.setDefaultValue(MOBILE_GENOMICS_FOLDER_PATH);
        storagePreference.setSummary(MOBILE_GENOMICS_FOLDER_PATH);
        storagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openFileManager();
                return true;
            }
        });
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
                    }
                })
                .build()
                .show();
    }
}
