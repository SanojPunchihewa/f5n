package com.mobilegenomics.f5n.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.PipelineStep;
import com.mobilegenomics.f5n.support.DownloadListener;
import com.mobilegenomics.f5n.support.TimeFormat;
import com.mobilegenomics.f5n.support.ZipListener;
import com.mobilegenomics.f5n.support.ZipManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DemoActivity extends AppCompatActivity {

    private static final String TAG = DemoActivity.class.getSimpleName();

    private static final String ecoliDataSetURL = "https://zanojmobiapps.com/_tmp/genome/ecoli/ecoli-data-set.zip";

    private static final String folderName = "mobile-genomics";

    private static final String fileName = "ecoli-data-set.zip";

    private TextView statusTextView;

    private ProgressBar progressBar;

    private LinearLayout linearLayout;

    private TextView txtLogView;

    private Button btnRunPipeline;

    private File logFile;

    private FileOutputStream fOut;

    private OutputStreamWriter myOutWriter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        String dirPath = Environment.getExternalStorageDirectory() + "/" + folderName;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        logFile = new File(dir.getAbsolutePath() + "/f5n-log.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Toast.makeText(this, "Error creating log file, Please check permissions", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error : " + e);
            }
        }
        try {
            fOut = new FileOutputStream(logFile, true);
            myOutWriter = new OutputStreamWriter(fOut);
            String header = "----------- Log for Demo app session " + TimeFormat
                    .millisToDateTime(System.currentTimeMillis())
                    + " -----------\n";
            myOutWriter.append(header);
        } catch (Exception e) {
            Log.e(TAG, "Error : " + e);
        }

        linearLayout = findViewById(R.id.vertical_linear_layout);

        TextView txtInfo = new TextView(this);
        txtInfo.setText(getResources().getString(R.string.pipeline_description));
        linearLayout.addView(txtInfo);

        TextView txtSkipDownload = new TextView(this);
        txtSkipDownload.setText(
                "If you have already downloaded and extracted the ecoli data set to main-storage/mobile-genomics folder, you can skip Download & Extract");
        txtSkipDownload.setPadding(0, 10, 0, 0);
        linearLayout.addView(txtSkipDownload);

        statusTextView = new TextView(this);
        linearLayout.addView(statusTextView);

        progressBar = new ProgressBar(this,
                null,
                android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgress(0);
        progressBar.setProgressTintList(ColorStateList
                .valueOf(Color.BLACK));
        linearLayout.addView(progressBar);

        Button btnDownloadAndExtract = new Button(this);
        btnDownloadAndExtract.setText("Download & Extract");
        btnDownloadAndExtract.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                btnDownloadAndExtract.setEnabled(false);
                downloadDataSet(ecoliDataSetURL);
            }
        });
        linearLayout.addView(btnDownloadAndExtract);

        txtLogView = new TextView(this);
//        txtLogView.setTextSize(f);
        linearLayout.addView(txtLogView);

        btnRunPipeline = new Button(this);
        btnRunPipeline.setText("Run Pipeline");
        btnRunPipeline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                GUIConfiguration.eraseSelectedPipeline();
                for (PipelineStep step : PipelineStep.values()) {
                    GUIConfiguration.addPipelineStep(step);
                }
                GUIConfiguration.printList();
                GUIConfiguration.configureSteps(DemoActivity.this,
                        Environment.getExternalStorageDirectory() + "/" + folderName + "/"
                                + fileName.substring(0, fileName.lastIndexOf(".")));
                startActivity(new Intent(DemoActivity.this, TerminalActivity.class));
            }
        });
        linearLayout.addView(btnRunPipeline);

    }

    private void downloadDataSet(String url) {
        // TODO check wifi connectivity

        writeToLogFile("Downloading data set...\n");

        com.mobilegenomics.f5n.support.DownloadManager
                downloadManager = new com.mobilegenomics.f5n.support.DownloadManager(url,
                Environment.getExternalStorageDirectory() + "/" + folderName, statusTextView,
                progressBar,
                new DownloadListener() {
                    @Override
                    public void onComplete(@NonNull final EndCause cause, @Nullable final Exception realCause) {
                        if (cause == EndCause.COMPLETED) {
                            writeToLogFile("Downloading data set completed\n");
                            extractZip(Environment.getExternalStorageDirectory() + "/" + folderName + "/"
                                    + fileName);
                        } else {
                            writeToLogFile("Downloading data set error : " + realCause);
                        }
                    }
                });
        downloadManager.download();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            myOutWriter.append("-------------------- End of Log --------------------\n\n");
            myOutWriter.flush();
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractZip(String filePath) {

        ZipManager zipManager = new ZipManager(new ZipListener() {
            @Override
            public void onStarted(@NonNull final long totalBytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setMax(100);
                        statusTextView.setText("Unzip started");
                        writeToLogFile("Extracting data set...\n");
                    }
                });
            }

            @Override
            public void onProgress(@NonNull final long bytesDone, @NonNull final long totalBytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int perc = ZipManager.getZipPercentage(bytesDone, totalBytes);
                        progressBar.setProgress(perc);
                        statusTextView.setText("Unzipping: " + perc + "%");
                    }
                });
            }

            @Override
            public void onComplete(@NonNull final boolean success, @Nullable final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            statusTextView.setText("Unzip Successful");
                            writeToLogFile("Extracting data set completed\n");
                            btnRunPipeline.setVisibility(View.VISIBLE);
                        } else {
                            statusTextView.setText("Unzip Error");
                            writeToLogFile("Extracting data set error\n");
                        }
                    }
                });
            }
        });
        zipManager.unzip(filePath);
    }

    private void writeToLogFile(String lines) {
        txtLogView.append(lines);
        try {
            myOutWriter.append(lines);
            myOutWriter.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error : " + e);
        }
    }

}
