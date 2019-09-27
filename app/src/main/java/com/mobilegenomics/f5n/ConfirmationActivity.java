package com.mobilegenomics.f5n;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfirmationActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    TextView txtLogs;

    LinearLayout linearLayout;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        linearLayout = findViewById(R.id.vertical_linear_layout);

        String[] commands = GUIConfiguration.getSelectedCommandStrings();

        for (String command : commands) {
            TextView txtCommand = new TextView(this);
            txtCommand.setText(command);
            txtCommand.setPadding(10, 10, 10, 0);
            linearLayout.addView(txtCommand);
        }

        Button btnProceed = new Button(this);
        btnProceed.setText("Run the Pipeline");
        btnProceed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                GUIConfiguration.createPipeline();
                new RunPipeline().execute();
            }
        });
        linearLayout.addView(btnProceed);

        View separator1 = new View(this);
        separator1.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                5
        ));
        separator1.setBackgroundColor(Color.parseColor("#000000"));
        linearLayout.addView(separator1);

        txtLogs = new TextView(this);
        linearLayout.addView(txtLogs);
        View separator2 = new View(this);
        separator2.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                5
        ));
        separator2.setBackgroundColor(Color.parseColor("#000000"));
        linearLayout.addView(separator2);

    }

    public class RunPipeline extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressWindow();
            try {
                Runtime.getRuntime().exec("logcat -c");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(final String... strings) {
            try {
                GUIConfiguration.runPipeline();
            } catch (Exception e) {
                Log.e("NATIVE-LIB", "Exception thrown by native code : " + e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            hideProgressWindow();
            try {
                Process process = Runtime.getRuntime().exec("logcat -d");

                BufferedReader bufferedReader = new BufferedReader(

                        new InputStreamReader(process.getInputStream()));

                StringBuilder log = new StringBuilder();

                String line;

                Pattern pattern = Pattern.compile("f5c-android:(.*)|minimap2-native:(.*)|samtools-native:(.*)", 0);
                Matcher matcher;

                while ((line = bufferedReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (!matcher.find()) {
                        continue;
                    }
                    // TODO find a better way to append all the matching groups
                    if (matcher.group(1) != null) {
                        log.append(matcher.group(1));
                    } else if (matcher.group(2) != null) {
                        log.append(matcher.group(2));
                    } else if (matcher.group(3) != null) {
                        log.append(matcher.group(3));
                    }

                    log.append('\n');
                }

                txtLogs.setText(log);

            } catch (Exception e) {
                Log.e("LOGCAT", "Cannot read from logcat :" + e);
            }
            List<PipelineComponent> pipelineComponents = GUIConfiguration.getPipeline();
            for (PipelineComponent pipelineComponent : pipelineComponents) {
                TextView txtRuntime = new TextView(ConfirmationActivity.this);
                txtRuntime.setText(
                        pipelineComponent.getPipelineStep().getCommand() + " took " + pipelineComponent.getRuntime());
                linearLayout.addView(txtRuntime);
            }
        }
    }

    private void showProgressWindow() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Running...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressWindow() {
        progressDialog.dismiss();
    }

}
