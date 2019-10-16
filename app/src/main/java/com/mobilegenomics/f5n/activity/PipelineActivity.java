package com.mobilegenomics.f5n.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.PipelineStep;

import java.util.ArrayList;

public class PipelineActivity extends AppCompatActivity {

    private ArrayList<CheckBox> pipelineSteps = new ArrayList<>();

    private static final int MODE_GUI = 0;

    private static final int MODE_TERMINAL = 1;

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
        btnGUIMode.setText("Use GUI Mode");
        btnGUIMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                initPipelineSteps(MODE_GUI);
            }
        });
        linearLayout.addView(btnGUIMode);

        Button btnTerminalMode = new Button(this);
        btnTerminalMode.setText("Use Terminal Mode");
        btnTerminalMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                initPipelineSteps(MODE_TERMINAL);
            }
        });
        linearLayout.addView(btnTerminalMode);
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
            GUIConfiguration.printList();
            GUIConfiguration.configureSteps(PipelineActivity.this);
            if (mode == MODE_GUI) {
                startActivity(new Intent(PipelineActivity.this, StepActivity.class));
            } else {
                startActivity(new Intent(PipelineActivity.this, TerminalActivity.class));
            }

        }
    }
}
