package com.mobilegenomics.genopo.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
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
import com.mobilegenomics.genopo.GUIConfiguration;
import com.mobilegenomics.genopo.R;
import com.mobilegenomics.genopo.core.MethylationPipelineStep;
import com.mobilegenomics.genopo.core.PipelineStep;
import com.mobilegenomics.genopo.core.PipelineType;
import com.mobilegenomics.genopo.core.VariantPipelineStep;
import com.mobilegenomics.genopo.support.DownloadListener;
import com.mobilegenomics.genopo.support.FileUtil;
import com.mobilegenomics.genopo.support.PipelineState;
import com.mobilegenomics.genopo.support.PreferenceUtil;
import com.mobilegenomics.genopo.support.TimeFormat;
import com.mobilegenomics.genopo.support.ZipListener;
import com.mobilegenomics.genopo.support.ZipManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DemoActivity extends AppCompatActivity {

    private static final String TAG = DemoActivity.class.getSimpleName();

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

        logFile = new File(dir.getAbsolutePath() + "/" + FileUtil.LOG_FILE_NAME);
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

        TextView crashWarning = new TextView(this);
        crashWarning.setText(Html.fromHtml(
                "<b>Do not</b> minimize, rotate or turn off the display, process may crash"));
        crashWarning.setTextColor(getResources().getColor(R.color.colorRead));
        linearLayout.addView(crashWarning);

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
                String datasetFolder = Environment.getExternalStorageDirectory() + "/" + folderName
                        + "/ecoli-data-set";
                FileUtil.deleteFolder(new File(datasetFolder));
                downloadDataSet(getResources().getString(R.string.ecoli_data_set_url));
            }
        });
        linearLayout.addView(btnDownloadAndExtract);

        txtLogView = new TextView(this);
        linearLayout.addView(txtLogView);

        btnRunPipeline = new Button(this);
        btnRunPipeline.setText("Run Pipeline");
        btnRunPipeline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                GUIConfiguration.eraseSelectedPipeline();

                int pipelineType = PreferenceUtil.getSharedPreferenceInt(R.string.key_pipeline_type_preference);
                PipelineStep pipelineStep;

                if (pipelineType == PipelineType.PIPELINE_METHYLATION.ordinal()) {
                    pipelineStep = new MethylationPipelineStep();
                } else {
                    pipelineStep = new VariantPipelineStep();
                }
                for (PipelineStep step : pipelineStep.values()) {
                    GUIConfiguration.addPipelineStep(step);
                }
                GUIConfiguration.printList();
                GUIConfiguration.setPipelineState(PipelineState.CONFIGURED);
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

        com.mobilegenomics.genopo.support.DownloadManager
                downloadManager = new com.mobilegenomics.genopo.support.DownloadManager(url,
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
        Uri treeUri = PreferenceUtil.getSharedPreferenceUri(R.string.sdcard_uri);
        downloadManager.download(DemoActivity.this, treeUri);
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

        ZipManager zipManager = new ZipManager(DemoActivity.this, new ZipListener() {
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
            public void onComplete(@NonNull final boolean success, @NonNull final long timeTook,
                    @Nullable final Exception exception) {
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
        Uri treeUri = PreferenceUtil.getSharedPreferenceUri(R.string.sdcard_uri);
        zipManager.unzip(treeUri, filePath);
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
