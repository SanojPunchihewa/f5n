package com.mobilegenomics.f5n.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.Step;
import com.mobilegenomics.f5n.support.PipelineState;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import com.mobilegenomics.f5n.support.ScreenDimUtil;
import com.obsez.android.lib.filechooser.ChooserDialog;
import java.util.ArrayList;
import java.util.Objects;

public class TerminalActivity extends AppCompatActivity {

    private static final String DATA_SET_PATH = "\\$DATA_SET_PATH";

    private LinearLayout linearLayout;

    private int stepId = 0;

    private ArrayList<Step> steps;

    private String folderPath;

    private EditText editTextFolderPath;

    private Button btnCopyPath;

    private Button btnOpenFolder;

    private Button btnPastePath;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_step);

        linearLayout = findViewById(R.id.vertical_linear_layout);

        if (GUIConfiguration.getPipelineState() == null) {
            // TODO Find a better fix
            // The app has crashed !
            ScreenDimUtil.changeBrightness(getContentResolver(), getWindow(),
                    PreferenceUtil.getSharedPreferenceInt(R.string.id_screen_brightness));
            showCrashError();
        } else {

            editTextFolderPath = findViewById(R.id.edit_text_folder_path);
            editTextFolderPath.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(final CharSequence s, final int start, final int count,
                        final int after) {

                }

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

                }

                @Override
                public void afterTextChanged(final Editable s) {
                    folderPath = s.toString();
                    replaceDirectoryPath(folderPath);
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
            btnOpenFolder = findViewById(R.id.btn_open_folder);
            btnOpenFolder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    openFileManager();
                }
            });
            btnPastePath = findViewById(R.id.btn_paste_path);
            btnPastePath.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    try {
                        ClipData.Item item = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0);
                        folderPath = item.getText().toString();
                        if (!TextUtils.isEmpty(folderPath)) {
                            editTextFolderPath.setText(folderPath);
                        }
                    } catch (NullPointerException e) {
                        Toast.makeText(TerminalActivity.this, "No file path was copied", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            steps = GUIConfiguration.getSteps();

            // If resumed, set the folder path
            if (PreferenceUtil.getSharedPreferenceInt(R.string.id_app_mode) == PipelineState.MINIT_RUNNING
                    .ordinal()) {
                folderPath = PreferenceUtil.getSharedPreferenceString(R.string.id_folder_path);
            }

            // if previous config mode, load previous configuration
            if (GUIConfiguration.getPipelineState() == PipelineState.PREV_CONFIG_LOAD) {
                folderPath = PreferenceUtil.getSharedPreferenceString(R.string.id_folder_path);
                steps = PreferenceUtil.getSharedPreferenceStepList(R.string.id_step_list);
                GUIConfiguration.setSteps(steps);
            }

            stepId = 0;

            LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 30);

            for (Step step : steps) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setId(stepId + 1250);
                checkBox.setText(step.getStep().getName());
                checkBox.setChecked(step.isSelected());
                linearLayout.addView(checkBox);

                EditText editText = new EditText(this);
                editText.setText(step.getCommandString());
                editText.setBackgroundColor(0xFFE5E7E9);
                editText.setLayoutParams(params);
                editText.setId(stepId + 125);
                editText.requestFocus();
                linearLayout.addView(editText);

                View separator = new View(this);
                separator.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        3
                ));
                separator.setBackgroundColor(Color.parseColor("#949494"));
                linearLayout.addView(separator);

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
                            step.setCommandString(editText.getText().toString().replaceAll("\"", "")); //^"|"$
                        }
                        CheckBox checkBox = findViewById(stepId + 1250);
                        step.setSelected(checkBox.isChecked());
                        stepId++;
                    }
                    GUIConfiguration.setSteps(steps);

                    // TODO App mode state is always the same?, change to get it from the calling activity
                    PreferenceUtil
                            .setSharedPreferenceInt(R.string.id_app_mode, PipelineState.MINIT_RUNNING.ordinal());
                    PreferenceUtil.setSharedPreferenceStepList(R.string.id_step_list, steps);
                    PreferenceUtil.setSharedPreferenceString(R.string.id_folder_path, folderPath);
                    GUIConfiguration.setPipelineState(PipelineState.CONFIGURED);

                    Intent intent = new Intent(TerminalActivity.this, ConfirmationActivity.class);
                    intent.putExtra("FOLDER_PATH", folderPath);
                    startActivity(intent);
                }
            });
            TextView txtSdcardPermissionInfo = new TextView(this);
            txtSdcardPermissionInfo.setText(
                    "change the output file paths to a location in the internal storage if writing to the SD card failed");
            linearLayout.addView(txtSdcardPermissionInfo);
        }
    }

    private void openFileManager() {

        new ChooserDialog(TerminalActivity.this)
                .withFilter(true, false)
                // to handle the result(s)
                .withChosenListener((path, pathFile) -> {
                    folderPath = path;
                    editTextFolderPath.setText(folderPath);
                })
                .build()
                .show();
    }

    private void replaceDirectoryPath(String path) {
        for (int stepId = 0; stepId < steps.size(); stepId++) {
            EditText editText = findViewById(stepId + 125);
            if (editText.getText() != null && !TextUtils.isEmpty(editText.getText().toString())) {
                String cmd = editText.getText().toString();
                String replacedCmd = cmd.replaceAll(DATA_SET_PATH, path);
                editText.setText(replacedCmd);
            }
        }
    }

    private void showCrashError() {
        new AlertDialog.Builder(this)
                .setTitle("F5N Crashed")
                .setMessage(
                        "One of the Native libraries has encountered a problem, most probably an Out Of Memory. Refer tmp.log in main storage/mobile-genomics folder for more information")
                .setPositiveButton("Go to Start page", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(TerminalActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .setCancelable(false)
                .show();
    }

}
