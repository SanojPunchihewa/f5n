package com.mobilegenomics.f5n;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import java.io.File;
import java.util.ArrayList;

public class StepActivity extends AppCompatActivity {

    ArrayList<Argument> arguments;

    private int argument_id = 0;

    private String folderPath;

    private EditText editTextFolderPath;

    private ImageButton btnCopyPath;

    private TextView txtStepName;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_step);

        editTextFolderPath = findViewById(R.id.edit_text_folder_path);
        editTextFolderPath.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                openFileManager();
                return false;
            }
        });
        btnCopyPath = findViewById(R.id.btn_copy_path);
        btnCopyPath.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("folderpath", folderPath);
                clipboard.setPrimaryClip(clip);
            }
        });
        txtStepName = findViewById(R.id.txt_step_name);

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        // TODO Handle orientation change
        final Step step = GUIConfiguration.getNextStep();
        arguments = step.getArguments();

        txtStepName.setText(step.getStep().getCommand());

        for (final Argument argument : arguments) {
            LinearLayout linearLayoutHorizontal = new LinearLayout(this);
            LinearLayout.LayoutParams linearLayout_LayoutParams =
                    new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout_LayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            linearLayoutHorizontal.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.addView(linearLayoutHorizontal);
            linearLayoutHorizontal.setLayoutParams(linearLayout_LayoutParams);

            CheckBox checkBox = new CheckBox(this);

            String text = argument.isHasFlag() ? argument.getArgName() + "( " + argument.getFlag() + " )"
                    : argument.getArgName();
            checkBox.setText(text);
            checkBox.setChecked(argument.isRequired());
            checkBox.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    return argument.isRequired();
                }
            });
            checkBox.setId(argument_id);
            LinearLayout.LayoutParams checkBox_LayoutParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            checkBox_LayoutParams.weight = 1;
            linearLayoutHorizontal.addView(checkBox);
            checkBox.setLayoutParams(checkBox_LayoutParams);

            if (!argument.isFlagOnly()) {
                EditText editText = new EditText(this);
                editText.setId(argument_id + 1000);
                LinearLayout.LayoutParams editText_LayoutParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                editText_LayoutParams.weight = 3;
                editText.setText(argument.getArgValue());
                linearLayoutHorizontal.addView(editText);
                editText.setLayoutParams(editText_LayoutParams);
            }
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
                        argument.setSetByUser(true);
                        if (!argument.isFlagOnly()) {
                            EditText editText = findViewById(arg_id + 1000);
                            if (editText.getText() != null && !TextUtils.isEmpty(editText.getText().toString())) {
                                argument.setArgValue(editText.getText().toString());
                            } else {
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

    private void openFileManager() {

        DialogProperties properties = new DialogProperties();

        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        FilePickerDialog dialog = new FilePickerDialog(StepActivity.this, properties);
        dialog.setTitle("Select a Folder");

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                folderPath = files[0];
                editTextFolderPath.setText(folderPath);
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GUIConfiguration.reduceStepCount();
    }
}
