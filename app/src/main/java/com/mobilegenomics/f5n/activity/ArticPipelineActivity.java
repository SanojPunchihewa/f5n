package com.mobilegenomics.f5n.activity;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.ArticPipelineArgument;
import com.mobilegenomics.f5n.core.Step;
import com.mobilegenomics.f5n.support.PipelineState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticPipelineActivity extends AppCompatActivity {

    private HashMap<String, String> userFilledArgs = new HashMap<>();

    private HashMap<String, String> autoFilledArgs = new HashMap<>();

    private ArrayList<Step> steps = new ArrayList<>();

    private String folderPath;

    private EditText editTextFolderPath;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_step);

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        ArrayList<ArticPipelineArgument> argsToBeFilledByUser = GUIConfiguration
                .getArticPipelineUserFilledArgs(this);

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
//                getFileNameList(path);
            }
        }

//        btnCopyPath = findViewById(R.id.btn_copy_path);
//        btnCopyPath.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("folderpath", folderPath);
//                clipboard.setPrimaryClip(clip);
//            }
//        });
//        btnOpenFolder = findViewById(R.id.btn_open_folder);
//        btnOpenFolder.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                openFileManager();
//            }
//        });
//        btnPastePath = findViewById(R.id.btn_paste_path);
//        btnPastePath.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                try {
//
//                    ClipData.Item item = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0);
//                    folderPath = item.getText().toString();
//                    if (!TextUtils.isEmpty(folderPath)) {
//                        editTextFolderPath.setText(folderPath);
//                        getFileNameList(folderPath);
//                    }
//                } catch (NullPointerException e) {
//                    Toast.makeText(StepActivity.this, "No file path was copied", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

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
//                if (argument.isFile()) {
//                    editText = new AutoCompleteTextView(this);
//                    ((AutoCompleteTextView) editText).setAdapter(adapter);
//                    ((AutoCompleteTextView) editText).setOnItemClickListener(new OnItemClickListener() {
//                        @Override
//                        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
//                                final long id) {
//                            editText.setText(folderPath + "/" + parent.getAdapter().getItem(position));
//                        }
//                    });
//                } else {
//                    editText = new EditText(this);
//                }
                editText = new EditText(this);
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

        steps = GUIConfiguration.getArticPipelineAutoGeneratedSteps(this);

        Button btnNext = new Button(this);
        btnNext.setText("Next");
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                int arg_id = 0;
                boolean haveSetAllRequiredArgs = true;
                userFilledArgs.put("[WORKING_DIRECTORY]", editTextFolderPath.getText().toString());
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

                replaceCommadArguments();
                GUIConfiguration.configureSteps(steps);
                GUIConfiguration.setPipelineState(PipelineState.CONFIGURED);
                startActivity(new Intent(ArticPipelineActivity.this, TerminalActivity.class));

//                for (Argument argument : arguments) {
//                    Log.d("ARGS", argument.getArgName() + " = " + argument.getArgValue());
//                }
//                step.buildCommandString();
//                if (haveSetAllRequiredArgs) {
//                    if (GUIConfiguration.isFinalStep()) {
//                        //startActivity(new Intent(StepActivity.this, ConfirmationActivity.class));
//                        GUIConfiguration.setPipelineState(PipelineState.CONFIGURED);
//                        startActivity(new Intent(StepActivity.this, TerminalActivity.class));
//                    } else {
//                        Intent intent = new Intent(StepActivity.this, StepActivity.class);
//                        intent.putExtra("FOLDER_PATH", folderPath);
//                        startActivity(intent);
//                    }
//                }
            }
        });
        linearLayout.addView(btnNext);
    }

    private void replaceCommadArguments() {
        for (Step step : steps) {
            String command = step.getCommandString();
            Pattern p = Pattern.compile("\\[(.*?)\\]");
            Matcher m = p.matcher(command);
            while (m.find()) {
                command = command.replace(m.group(0), userFilledArgs.get(m.group(0)));
            }
            Log.e("TAG", "Command = " + command);
            step.setCommandString(command);
        }
    }

}
