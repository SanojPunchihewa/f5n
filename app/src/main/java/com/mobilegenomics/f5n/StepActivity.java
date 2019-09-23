package com.mobilegenomics.f5n;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class StepActivity extends AppCompatActivity {

    ArrayList<Argument> arguments;

    private int argument_id = 0;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        // TODO Handle orientation change
        final Step step = GUIConfiguration.getNextStep();
        arguments = step.getArguments();

        TextView txtCurrentStep = new TextView(this);
        txtCurrentStep.setText(GUIConfiguration.getCurrentStepCount() + "");
        linearLayout.addView(txtCurrentStep);

        for (Argument argument : arguments) {
            LinearLayout linearLayoutHorizontal = new LinearLayout(this);
            LinearLayout.LayoutParams linearLayout_LayoutParams =
                    new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout_LayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            linearLayoutHorizontal.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.addView(linearLayoutHorizontal);
            linearLayoutHorizontal.setLayoutParams(linearLayout_LayoutParams);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(argument.getArgName());
            checkBox.setChecked(argument.isRequired());
            checkBox.setId(argument_id);
            LinearLayout.LayoutParams checkBox_LayoutParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            checkBox_LayoutParams.weight = 1;
            linearLayoutHorizontal.addView(checkBox);
            checkBox.setLayoutParams(checkBox_LayoutParams);

            EditText editText = new EditText(this);
            editText.setId(argument_id + 1000);
            LinearLayout.LayoutParams editText_LayoutParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            editText_LayoutParams.weight = 3;
            linearLayoutHorizontal.addView(editText);
            editText.setLayoutParams(editText_LayoutParams);
            argument_id++;
        }

        Button btnNext = new Button(this);
        btnNext.setText("Next");
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                int arg_id = 0;
                boolean haveSetAllRequiredArgs = true;
                for (Argument argument : arguments) {
                    CheckBox checkBox = findViewById(arg_id);
                    if (checkBox.isChecked()) {
                        EditText editText = findViewById(arg_id + 1000);
                        if (editText.getText() != null && !TextUtils.isEmpty(editText.getText().toString())) {
                            argument.setArgValue(editText.getText().toString());
                            argument.setSetByUser(true);
                        } else {
                            if (argument.isRequired()) {
                                haveSetAllRequiredArgs = false;
                                editText.setError("This field is required!");
                            }
                        }
                    } else { // not needed as UI keeps a saved instance
                        argument.setSetByUser(false);
                    }
                    arg_id++;
                }
                for (Argument argument : arguments) {
                    Log.e("ARGS", argument.getArgName() + " = " + argument.getArgValue());
                }
                step.buildCommandString();
                if (haveSetAllRequiredArgs) {
                    if (GUIConfiguration.isFinalStep()) {
                        startActivity(new Intent(StepActivity.this, ConfirmationActivity.class));
                    } else {
                        startActivity(new Intent(StepActivity.this, StepActivity.class));
                    }
                }
            }
        });
        linearLayout.addView(btnNext);

        Button btnBack = new Button(this);
        btnBack.setText("Back");
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                onBackPressed();
            }
        });
        linearLayout.addView(btnBack);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GUIConfiguration.reduceStepCount();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        // TODO Find a better solution
        // super.onRestoreInstanceState(savedInstanceState);
    }
}
