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
import com.mobilegenomics.f5n.core.ArticPipelineStep;
import com.mobilegenomics.f5n.core.MethylationPipelineStep;
import com.mobilegenomics.f5n.core.PipelineStep;
import com.mobilegenomics.f5n.core.PipelineType;
import com.mobilegenomics.f5n.core.SingleToolPipelineStep;
import com.mobilegenomics.f5n.core.Step;
import com.mobilegenomics.f5n.core.VariantPipelineStep;
import com.mobilegenomics.f5n.support.PipelineState;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import java.util.ArrayList;

public class PipelineActivity extends AppCompatActivity {

    private ArrayList<CheckBox> pipelineSteps = new ArrayList<>();

    private static final int MODE_GUI = 0;

    private static final int MODE_TERMINAL = 1;

    private static final int MODE_PREV_CONFIG = 2;

    private PipelineStep pipelineStep;

    private int pipelineType;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        pipelineType = PreferenceUtil.getSharedPreferenceInt(R.string.key_pipeline_type_preference);

        if (pipelineType == PipelineType.PIPELINE_METHYLATION.ordinal()) {
            pipelineStep = new MethylationPipelineStep();
        } else if (pipelineType == PipelineType.PIPELINE_VARIANT.ordinal()) {
            pipelineStep = new VariantPipelineStep();
        } else if (pipelineType == PipelineType.PIPELINE_ARTIC.ordinal()) {
            pipelineStep = new ArticPipelineStep();
        } else if (pipelineType == PipelineType.SINGLE_TOOL.ordinal()) {
            pipelineStep = new SingleToolPipelineStep();
        }

        int i = 0;

        for (PipelineStep step : pipelineStep.values()) {
            CheckBox checkBox = new CheckBox(PipelineActivity.this);
            checkBox.setId(i++);
            checkBox.setText(step.getName());
            pipelineSteps.add(checkBox);
            linearLayout.addView(checkBox);
        }

        if (pipelineType != PipelineType.SINGLE_TOOL.ordinal()) {

            Button btnGUIMode = new Button(this);
            btnGUIMode.setText(getString(R.string.btn_use_gui_mode));
            btnGUIMode.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    initPipelineSteps(MODE_GUI);
                }
            });
            linearLayout.addView(btnGUIMode);

        }

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
        ArrayList<Step> singleTools = new ArrayList<>();
        boolean clickedNone = true;
        int i = 0;
        for (PipelineStep step : pipelineStep.values()) {
            CheckBox checkBox = findViewById(i++);
            if (checkBox.isChecked()) {
                GUIConfiguration.addPipelineStep(step);
                singleTools.add(new Step(step, step.getCommand()));
                clickedNone = false;
            }
        }
        if (!clickedNone) {
            GUIConfiguration.setPipelineState(PipelineState.TO_BE_CONFIGURED);
            GUIConfiguration.printList();
            if (pipelineType == PipelineType.SINGLE_TOOL.ordinal()) {
                GUIConfiguration.configureSteps(singleTools);
            } else {
                GUIConfiguration.configureSteps(PipelineActivity.this, null);
            }
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
