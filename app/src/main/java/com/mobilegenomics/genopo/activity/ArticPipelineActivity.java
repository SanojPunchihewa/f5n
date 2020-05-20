package com.mobilegenomics.genopo.activity;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.genopo.GUIConfiguration;
import com.mobilegenomics.genopo.R;
import com.mobilegenomics.genopo.core.ArticPipelineArgument;
import com.mobilegenomics.genopo.core.PipelineType;
import com.mobilegenomics.genopo.core.Step;
import com.mobilegenomics.genopo.support.PipelineState;
import com.mobilegenomics.genopo.support.PreferenceUtil;
import com.obsez.android.lib.filechooser.ChooserDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ArticPipelineActivity extends AppCompatActivity {

    private HashMap<String, String> userFilledArgs = new HashMap<>();

    private ArrayList<Step> steps = new ArrayList<>();

    private String folderPath;

    private EditText editTextFolderPath;

    private Button btnCopyPath;

    private Button btnOpenFolder;

    private Button btnPastePath;

    ArrayAdapter<String> adapter;

    private List<String> fileNames;

    private PipelineType pipelineType;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_step);

        int _pipelineType = PreferenceUtil.getSharedPreferenceInt(R.string.key_pipeline_type_preference);

        if (_pipelineType == PipelineType.PIPELINE_ARTIC.ordinal()) {
            pipelineType = PipelineType.PIPELINE_ARTIC;
        } else if (_pipelineType == PipelineType.PIPELINE_CONSENSUS.ordinal()) {
            pipelineType = PipelineType.PIPELINE_CONSENSUS;
        }

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        fileNames = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, fileNames);

        ArrayList<ArticPipelineArgument> argsToBeFilledByUser = GUIConfiguration
                .getArticPipelineUserFilledArgs(this, pipelineType);

        userFilledArgs.put(argsToBeFilledByUser.get(0).getArgID(), "");

        editTextFolderPath = findViewById(R.id.edit_text_folder_path);
        editTextFolderPath.setHint(argsToBeFilledByUser.get(0).getArgDescription());
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
                    Toast.makeText(ArticPipelineActivity.this, "No file path was copied", Toast.LENGTH_SHORT).show();
                }
            }
        });

        argsToBeFilledByUser.remove(0);

        int id = 0;

        for (ArticPipelineArgument argument : argsToBeFilledByUser) {

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(argument.getArgDescription());
            checkBox.setChecked(true);
            checkBox.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    return true;
                }
            });
            checkBox.setId(id);

            linearLayout.addView(checkBox);

            if (!argument.isFlagOnly()) {
                final EditText editText;
                if (argument.isFile()) {
                    editText = new AutoCompleteTextView(this);
                    ((AutoCompleteTextView) editText).setAdapter(adapter);
                    ((AutoCompleteTextView) editText).setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                                final long id) {
                            editText.setText(folderPath + "/" + parent.getAdapter().getItem(position));
                        }
                    });
                } else {
                    editText = new EditText(this);
                }
                editText.setId(id + 1000);
                linearLayout.addView(editText);
                LinearLayout.LayoutParams editText_LayoutParams =
                        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                editText_LayoutParams.setMargins(15, 0, 15, 0);
                editText.setPadding(30, 20, 20, 20);
                editText.setBackgroundColor(0xFFE5E7E9);
                editText.setLayoutParams(editText_LayoutParams);

            }
            id++;
        }

        Button btnNext = new Button(this);
        btnNext.setText("Next");
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                int arg_id = 0;
                boolean haveSetAllRequiredArgs = true;

                if (editTextFolderPath.getText() != null && !TextUtils
                        .isEmpty(editTextFolderPath.getText().toString())) {
                    userFilledArgs.put("[WORKING_DIRECTORY]", editTextFolderPath.getText().toString());
                } else {
                    haveSetAllRequiredArgs = false;
                    editTextFolderPath.setError("This field is required!");
                }

                for (ArticPipelineArgument argument : argsToBeFilledByUser) {
                    CheckBox checkBox = findViewById(arg_id);
                    if (checkBox.isChecked()) {
                        if (!argument.isFlagOnly()) {
                            EditText editText = findViewById(arg_id + 1000);
                            if (editText.getText() != null && !TextUtils.isEmpty(editText.getText().toString())) {
                                String argValue = editText.getText().toString();
                                userFilledArgs.put(argument.getArgID(), argValue);
                            } else {
                                haveSetAllRequiredArgs = false;
                                editText.setError("This field is required!");
                            }
                        } else {

                        }
                    }
                    arg_id++;
                }

                if (haveSetAllRequiredArgs) {
                    steps = GUIConfiguration
                            .getArticPipelineAutoGeneratedSteps(ArticPipelineActivity.this, pipelineType,
                                    userFilledArgs);
                    GUIConfiguration.configureSteps(steps);
                    GUIConfiguration.setPipelineState(PipelineState.CONFIGURED);
                    startActivity(new Intent(ArticPipelineActivity.this, TerminalActivity.class));
                }
            }
        });
        linearLayout.addView(btnNext);

        Button btnLoadPreviousConfig = new Button(this);
        btnLoadPreviousConfig.setText(getString(R.string.btn_load_prev_config));
        btnLoadPreviousConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPreviousConfig();
            }
        });
        linearLayout.addView(btnLoadPreviousConfig);

    }

    private void loadPreviousConfig() {
        if ((PreferenceUtil.getSharedPreferenceString(R.string.id_folder_path) != null) &&
                (PreferenceUtil.getSharedPreferenceStepList(R.string.id_step_list) != null) &&
                !(PreferenceUtil.getSharedPreferenceStepList(R.string.id_step_list).isEmpty())) {
            GUIConfiguration.setPipelineState(PipelineState.PREV_CONFIG_LOAD);
            startActivity(new Intent(ArticPipelineActivity.this, TerminalActivity.class));
        } else {
            Toast.makeText(ArticPipelineActivity.this, "No previous configs to load", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFileManager() {

        new ChooserDialog(ArticPipelineActivity.this)
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

}
