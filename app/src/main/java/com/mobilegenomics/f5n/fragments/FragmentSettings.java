package com.mobilegenomics.f5n.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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
    private Preference referenceGnomePreference;
    private Preference versionPreference;
    private Preference feedbackPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        storagePreference = findPreference(getResources().getString(R.string.key_storage_preference));
        referenceGnomePreference = findPreference(getResources().getString(R.string.key_reference_gnome));
        versionPreference = findPreference(getResources().getString(R.string.key_version_preference));
        feedbackPreference = findPreference(getResources().getString(R.string.key_feedback_preference));

        String storagePath = PreferenceUtil.getSharedPreferenceString(R.string.key_storage_preference, MOBILE_GENOMICS_FOLDER_PATH);
        storagePreference.setDefaultValue(storagePath);
        storagePreference.setSummary(storagePath);
        storagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setDataStorage();
                return true;
            }
        });

        String referenceGnomePath = PreferenceUtil.getSharedPreferenceString(R.string.key_reference_gnome, MOBILE_GENOMICS_FOLDER_PATH);
        referenceGnomePreference.setDefaultValue(referenceGnomePath);
        referenceGnomePreference.setSummary(referenceGnomePath);
        referenceGnomePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setReferenceGnome();
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
}
