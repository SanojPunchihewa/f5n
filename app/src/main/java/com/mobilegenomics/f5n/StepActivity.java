package com.mobilegenomics.f5n;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.obsez.android.lib.filechooser.ChooserDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StepActivity extends AppCompatActivity {

    public static int FOLDERPICKER_CODE = 002;

    ArrayList<Argument> arguments;

    private int argument_id = 0;

    private String folderPath;

    private EditText editTextFolderPath;

    private Button btnCopyPath;

    private Button btnOpenFolder;

    private Button btnPastePath;

    private TextView txtStepName;

    ArrayAdapter<String> adapter;

    private List<String> fileNames;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_step);

        fileNames = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, fileNames);

//        adapter = new CustomerAdapter(this, android.R.layout.simple_dropdown_item_1line, fileNames);

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
            }
        });

        if (getIntent().getExtras() != null) {
            String path = getIntent().getExtras().getString("FOLDER_PATH");
            if (path != null && !TextUtils.isEmpty(path)) {
                editTextFolderPath.setText(path);
                getFileNameList(path);
            }
        }

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
                        getFileNameList(folderPath);
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(StepActivity.this, "No file path was copied", Toast.LENGTH_SHORT).show();
                }
            }
        });
        txtStepName = findViewById(R.id.txt_step_name);

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        // TODO Handle orientation change
        final Step step = GUIConfiguration.getNextStep();
        arguments = step.getArguments();

        txtStepName.setText(step.getStep().getCommand());

        for (final Argument argument : arguments) {

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
            // checkBox_LayoutParams.weight = 1;
            linearLayout.addView(checkBox);
            checkBox.setLayoutParams(checkBox_LayoutParams);

            if (!argument.isFlagOnly()) {
                final EditText editText;
                if (argument.isFile()) {
                    editText = new AutoCompleteTextView(this);
                    ((AutoCompleteTextView) editText).setAdapter(adapter);
                } else {
                    editText = new EditText(this);
                }
                editText.setId(argument_id + 1000);
                if (argument.isFile()) {
                    editText.setText(GUIConfiguration.getLinkedFileArgument(argument.getIsDependentOn()));
                } else {
                    editText.setText(argument.getArgValue());
                }
                linearLayout.addView(editText);
                LinearLayout.LayoutParams editText_LayoutParams =
                        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
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
                                String argValue = editText.getText().toString();
                                if (argument.isFile()) {
                                    GUIConfiguration
                                            .configureLikedFileArgument(argument.getArgID(), argValue);
                                    if (folderPath != null && !TextUtils.isEmpty(folderPath)) {
                                        argValue = folderPath + "/" + argValue;
                                    }
                                }
                                argument.setArgValue(argValue);
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
                    Log.d("ARGS", argument.getArgName() + " = " + argument.getArgValue());
                }
                step.buildCommandString();
                if (haveSetAllRequiredArgs) {
                    if (GUIConfiguration.isFinalStep()) {
                        startActivity(new Intent(StepActivity.this, ConfirmationActivity.class));
                    } else {
                        Intent intent = new Intent(StepActivity.this, StepActivity.class);
                        intent.putExtra("FOLDER_PATH", folderPath);
                        startActivity(intent);
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

        new ChooserDialog(StepActivity.this)
                .withFilter(true, false)
                // to handle the result(s)
                .withChosenListener((path, pathFile) -> {
                    folderPath = path;
                    editTextFolderPath.setText(folderPath);
                    getFileNameList(folderPath);
                })
                .build()
                .show();

    }

    private void getFileNameList(String folderPath) {

        adapter.clear();
        adapter.notifyDataSetChanged();
        File[] files = new File(folderPath).listFiles();
        try {
            for (File file : files) {
                adapter.add(file.getName());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid File path", Toast.LENGTH_SHORT).show();
            Log.e("STEP_ACTIVITY", "File Exception : " + e);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GUIConfiguration.reduceStepCount();
    }
}
