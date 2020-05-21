package com.mobilegenomics.genopo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.mobilegenomics.genopo.R;
import com.mobilegenomics.genopo.fragments.FragmentSettings;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preference_container, new FragmentSettings())
                .commit();
    }
}
