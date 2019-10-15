package com.mobilegenomics.f5n;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class PipelineActivity extends AppCompatActivity {

    private ArrayList<CheckBox> pipelineSteps = new ArrayList<>();

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

        Button button = new Button(this);
        button.setText("Next");
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
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
                    startActivity(new Intent(PipelineActivity.this, StepActivity.class));
                }
            }
        });
        linearLayout.addView(button);
    }
}
