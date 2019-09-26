package com.mobilegenomics.f5n;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class ConfirmationActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    TextView txtLogs;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        String[] commands = GUIConfiguration.getSelectedCommandStrings();

        for (String command : commands) {
            TextView txtCommand = new TextView(this);
            txtCommand.setText(command);
            txtCommand.setPadding(10, 10, 10, 0);
            linearLayout.addView(txtCommand);
        }

        txtLogs = new TextView(this);
        // txtLogs.setMaxLines(10);
        linearLayout.addView(txtLogs);

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
    }

    public class RunPipeline extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressWindow();
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

                System.out.println("Out");
                System.err.println("Err");
                Log.e("TAG", "LOG_ERR");

                BufferedReader bufferedReader = new BufferedReader(

                        new InputStreamReader(process.getInputStream()));

                StringBuilder log = new StringBuilder();

                String line;

                Pattern pattern = Pattern.compile("f5c-android", 0);

                while ((line = bufferedReader.readLine()) != null) {
                    if (pattern != null
                            && !pattern.matcher(line).find()) {
                        continue;
                    }
                    log.append(line);
                    log.append('\n');
                }

                txtLogs.setText(log);

            } catch (Exception e) {

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
