package com.mobilegenomics.f5n.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.fragments.FragmentSettings;

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
