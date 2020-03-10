package com.mobilegenomics.f5n.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.PipelineStep;
import com.mobilegenomics.f5n.support.PipelineState;
import com.mobilegenomics.f5n.support.PreferenceUtil;

import java.util.ArrayList;

public class PipelineActivity extends AppCompatActivity {

    private ArrayList<CheckBox> pipelineSteps = new ArrayList<>();

    private static final int MODE_GUI = 0;

    private static final int MODE_TERMINAL = 1;

    private static final int MODE_PREV_CONFIG = 2;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        for (PipelineStep step : PipelineStep.values()) {
            CheckBox checkBox = new CheckBox(PipelineActivity.this);
            checkBox.setId(step.getValue());
            checkBox.setText(step.toString());
            pipelineSteps.add(checkBox);
            linearLayout.addView(checkBox);
        }

        Button btnGUIMode = new Button(this);
        btnGUIMode.setText(getString(R.string.btn_use_gui_mode));
        btnGUIMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                initPipelineSteps(MODE_GUI);
            }
        });
        linearLayout.addView(btnGUIMode);

        Button btnTerminalMode = new Button(this);
        btnTerminalMode.setText(getString(R.string.btn_use_terminal_mode));
        btnTerminalMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                initPipelineSteps(MODE_TERMINAL);
            }
        });
        linearLayout.addView(btnTerminalMode);

        Button btnLoadPreviousConfig = new Button(this);
        btnLoadPreviousConfig.setText(getString(R.string.btn_load_prev_config));
        btnLoadPreviousConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                initPipelineSteps(MODE_PREV_CONFIG);
            }
        });
        linearLayout.addView(btnLoadPreviousConfig);
    }

    private void initPipelineSteps(int mode) {
        GUIConfiguration.eraseSelectedPipeline();
        GUIConfiguration.resetSteps();
        boolean clickedNone = true;
        for (PipelineStep step : PipelineStep.values()) {
            CheckBox checkBox = findViewById(step.getValue());
            if (checkBox.isChecked()) {
                GUIConfiguration.addPipelineStep(step);
                clickedNone = false;
            }
        }
        if (!clickedNone) {
            GUIConfiguration.setPipelineState(PipelineState.TO_BE_CONFIGURED);
            GUIConfiguration.printList();
            GUIConfiguration.configureSteps(PipelineActivity.this, null);
            if (mode == MODE_GUI) {
                startActivity(new Intent(PipelineActivity.this, StepActivity.class));
            } else if (mode == MODE_TERMINAL) {
                startActivity(new Intent(PipelineActivity.this, TerminalActivity.class));
            } else {
                loadPreviousConfig();
            }
        } else {
            loadPreviousConfig();
        }
    }

    private void loadPreviousConfig() {
        if ((PreferenceUtil.getSharedPreferenceString(R.string.id_folder_path) != null) &&
                (PreferenceUtil.getSharedPreferenceStepList(R.string.id_step_list) != null) &&
                !(PreferenceUtil.getSharedPreferenceStepList(R.string.id_step_list).isEmpty())) {
            GUIConfiguration.setPipelineState(PipelineState.PREV_CONFIG_LOAD);
            startActivity(new Intent(PipelineActivity.this, TerminalActivity.class));
        } else {
            Toast.makeText(PipelineActivity.this, "No previous configs to load", Toast.LENGTH_SHORT).show();
        }
    }
}
