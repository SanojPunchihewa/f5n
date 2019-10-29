package com.mobilegenomics.f5n.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.Step;
import com.obsez.android.lib.filechooser.ChooserDialog;
import java.util.List;
import java.util.Objects;

public class TerminalActivity extends AppCompatActivity {

    private static final String DATA_SET_PATH = "\\$DATA_SET_PATH";

    private LinearLayout linearLayout;

    private int stepId = 0;

    private List<Step> steps;

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

        editTextFolderPath = findViewById(R.id.edit_text_folder_path);
        editTextFolderPath.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

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

        stepId = 0;

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 10);

        for (Step step : steps) {
            EditText editText = new EditText(this);
            editText.setText(step.getCommandString());
            editText.setBackgroundColor(0xFFE5E7E9);
            editText.setLayoutParams(params);
            editText.setId(stepId + 125);
            editText.requestFocus();
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
                        step.setCommandString(editText.getText().toString());
                    }
                    stepId++;
                }

                GUIConfiguration.createPipeline();
                startActivity(new Intent(TerminalActivity.this, ConfirmationActivity.class));
            }
        });

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

}
