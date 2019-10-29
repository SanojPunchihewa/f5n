package com.mobilegenomics.f5n.activity;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.PipelineComponent;
import com.mobilegenomics.f5n.support.TimeFormat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfirmationActivity extends AppCompatActivity {

    private static final String TAG_F5C = "f5c-android";

    private static final String TAG_MINIMAP2 = "minimap2-native";

    private static final String TAG_SAMTOOLS = "samtools-native";

    private ProgressDialog progressDialog;

    TextView txtLogs;

    NestedScrollView scrollView;

    LinearLayout linearLayout;

    Button btnWriteLog;

    Button btnProceed;

    ProgressBar mProgressBar;

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

        btnProceed = new Button(this);
        btnProceed.setText("Run the Pipeline");
        btnProceed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                btnProceed.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);
                GUIConfiguration.createPipeline();
                new ShowLogCat().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new RunPipeline().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        linearLayout.addView(btnProceed);

        mProgressBar = new ProgressBar(this,
                null,
                android.R.attr.progressBarStyleHorizontal);
        mProgressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.BLACK));
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);
        linearLayout.addView(mProgressBar);

        View separator1 = new View(this);
        separator1.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                5
        ));
        separator1.setBackgroundColor(Color.parseColor("#000000"));
        linearLayout.addView(separator1);

        scrollView = new NestedScrollView(this);
        linearLayout.addView(scrollView);

        txtLogs = new TextView(this);
        scrollView.addView(txtLogs);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700);
        scrollView.setLayoutParams(params);
        View separator2 = new View(this);
        separator2.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                5
        ));
        separator2.setBackgroundColor(Color.parseColor("#000000"));
        linearLayout.addView(separator2);

        btnWriteLog = new Button(this);
        btnWriteLog.setText("Write Log to File");
        btnWriteLog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                writeLogToFile();
            }
        });
        btnWriteLog.setVisibility(View.GONE);
        linearLayout.addView(btnWriteLog);

    }

    public class RunPipeline extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            List<PipelineComponent> pipelineComponents = GUIConfiguration.getPipeline();
            for (PipelineComponent pipelineComponent : pipelineComponents) {
                TextView txtRuntime = new TextView(ConfirmationActivity.this);
                txtRuntime.setText(
                        pipelineComponent.getPipelineStep().getCommand() + " took " + pipelineComponent.getRuntime());
                linearLayout.addView(txtRuntime);
            }
            btnWriteLog.setVisibility(View.VISIBLE);
            btnProceed.setEnabled(true);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public class ShowLogCat extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            try {
                Runtime.getRuntime().exec("logcat -c");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Process process = Runtime.getRuntime().exec("logcat");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line = "", filtered = "";

                Pattern pattern = Pattern
                        .compile(TAG_F5C + "(.*)|" + TAG_MINIMAP2 + "(.*)|" + TAG_SAMTOOLS + "(.*)", 0);
                Matcher matcher;

                while ((line = bufferedReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (!matcher.find()) {
                        continue;
                    }
                    // TODO find a better way to append all the matching groups
                    if (matcher.group(1) != null) {
                        filtered = TAG_F5C + matcher.group(1);
                    } else if (matcher.group(2) != null) {
                        filtered = TAG_MINIMAP2 + matcher.group(2);
                    } else if (matcher.group(3) != null) {
                        filtered = TAG_SAMTOOLS + matcher.group(3);
                    }
                    publishProgress(filtered);

                }
            } catch (IOException e) {
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            txtLogs.append(values[0] + "\n");
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    private void writeLogToFile() {

        StringBuilder stringBuilder = new StringBuilder();

        String header = "----------- Log for app session " + TimeFormat.millisToDateTime(System.currentTimeMillis())
                + " -----------\n";
        stringBuilder.append(header);

        List<PipelineComponent> pipelineComponents = GUIConfiguration.getPipeline();
        for (PipelineComponent pipelineComponent : pipelineComponents) {
            String command = "Command :\n" + pipelineComponent.getCommand() + "\n";
            String time = "Time taken :\n" + pipelineComponent.getPipelineStep().getCommand() + " took "
                    + pipelineComponent
                    .getRuntime() + "\n";
            stringBuilder.append(command);
            stringBuilder.append(time);
            stringBuilder.append("\n");
        }

        String logcat = txtLogs.getText().toString();

        try {
            String dirPath = Environment.getExternalStorageDirectory() + "/mobile-genomics";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File logFile = new File(dir.getAbsolutePath() + "/f5n-log.txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(logFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(stringBuilder.toString());
            myOutWriter.append(logcat);
            myOutWriter.append("-------------------- End of Log --------------------\n\n");
            myOutWriter.flush();
            myOutWriter.close();
            fOut.close();
            Toast.makeText(getApplicationContext(), "Finished writing to mobile-genomics in home", Toast.LENGTH_LONG)
                    .show(); //##5
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Write failure", Toast.LENGTH_SHORT).show(); //##6
            Log.e("TAG", e.toString());
        }

    }

}
