package com.mobilegenomics.f5n.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.PipelineStep;
import com.mobilegenomics.f5n.support.TimeFormat;
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

    private long downloadID;

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

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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
                startActivity(new Intent(DemoActivity.this, ConfirmationActivity.class));
            }
        });
        linearLayout.addView(btnRunPipeline);

    }

    private void downloadDataSet(String url) {
        // TODO check wifi connectivity

        writeToLogFile("Downloading data set...\n");

        DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);

        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(fileName);
            request.setDescription("Downloading");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(true);
            request.setDestinationUri(
                    Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/" + folderName + "/"
                            + fileName));

            downloadID = downloadmanager.enqueue(request);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception: " + e);
            writeToLogFile("[Error] Downloading data set: " + e + "\n");
        }
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(DemoActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                writeToLogFile("Downloading data set completed\n");
                extractZip(Environment.getExternalStorageDirectory() + "/" + folderName + "/"
                        + fileName);
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(onDownloadComplete);
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
        writeToLogFile("Extracting data set...\n");
        ZipManager zipManager = new ZipManager(DemoActivity.this);
        zipManager.unzip(filePath);
        writeToLogFile("Extracting data set completed\n");
        btnRunPipeline.setVisibility(View.VISIBLE);
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
