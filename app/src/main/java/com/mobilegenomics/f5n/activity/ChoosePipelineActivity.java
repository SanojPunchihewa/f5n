package com.mobilegenomics.f5n.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.AppMode;

public class ChoosePipelineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pipeline);
    }

    public void startStandaloneMethylationMode(View view) {
        GUIConfiguration.setAppMode(AppMode.STANDALONE_METHYLATION);
        startActivity(new Intent(ChoosePipelineActivity.this, PipelineActivity.class));
    }

    public void startStandaloneVariantMode(View view) {
        GUIConfiguration.setAppMode(AppMode.STANDALONE_VARIANT);
        startActivity(new Intent(ChoosePipelineActivity.this, PipelineActivity.class));
    }
}
