package com.mobilegenomics.f5n;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class TerminalActivity extends AppCompatActivity {

    private LinearLayout linearLayout;

    private int stepId = 0;

    private List<Step> steps;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        linearLayout = findViewById(R.id.vertical_linear_layout);

        steps = GUIConfiguration.getSteps();

        stepId = 0;

        for (Step step : steps) {
            EditText editText = new EditText(this);
            editText.setText(step.getCommandString());
            editText.setPadding(10, 10, 10, 0);
            editText.setId(stepId + 125);
            linearLayout.addView(editText);
            stepId++;
        }

        Button button = new Button(this);
        linearLayout.addView(button);
        button.setText("Next");
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                stepId = 0;
                for (Step step : steps) {
                    EditText editText = findViewById(stepId + 125);
                    if (editText.getText() != null && !TextUtils.isEmpty(editText.getText().toString())) {
                        step.setgetCommandString(editText.getText().toString());
                    }
                    stepId++;
                }

                GUIConfiguration.createPipeline();
                startActivity(new Intent(TerminalActivity.this, ConfirmationActivity.class));
            }
        });

    }

}
