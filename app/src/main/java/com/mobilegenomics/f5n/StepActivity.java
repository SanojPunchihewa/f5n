package com.mobilegenomics.f5n;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class StepActivity extends AppCompatActivity {

    ArrayList<Argument> arguments;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        // TODO Handle orientation change
        final Step step = GUIConfiguration.getNextStep();
        arguments = step.getArguments();

        int argument_id = 0;

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

        Button button = new Button(this);
        button.setText("Next");
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                int arg_id = 0;
                for (Argument argument : arguments) {
                    CheckBox checkBox = findViewById(arg_id);
                    if (checkBox.isChecked()) {
                        EditText editText = findViewById(arg_id + 1000);
                        if (editText.getText() != null && !TextUtils.isEmpty(editText.getText().toString())) {
                            argument.setArgValue(editText.getText().toString());
                            argument.setSetByUser(true);
                        }
                    } else {
                        argument.setSetByUser(false);
                    }
                    arg_id++;
                }
                for (Argument argument : arguments) {
                    Log.e("ARGS", argument.getArgName() + " = " + argument.getArgValue());
                }
                recreate();
            }
        });
        linearLayout.addView(button);

    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        // TODO Find a better solution
        // super.onRestoreInstanceState(savedInstanceState);
    }
}
