package com.mobilegenomics.f5n.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.liulishuo.okdownload.core.Util;
import com.mobilegenomics.f5n.BuildConfig;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.dto.State;
import com.mobilegenomics.f5n.dto.WrapperObject;
import com.mobilegenomics.f5n.support.ServerCallback;
import com.mobilegenomics.f5n.support.ServerConnectionUtils;
import com.mobilegenomics.f5n.support.ZipListener;
import com.mobilegenomics.f5n.support.ZipManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceSingleBroadcastReceiver;
import net.gotev.uploadservice.UploadStatusDelegate;

public class MinITActivity extends AppCompatActivity implements UploadStatusDelegate {

    private static final String TAG = MinITActivity.class.getSimpleName();

    private static TextView connectionLogText;

    private String serverIP;

    private String zipFileName;

    private Button btnSendResult;

    private boolean ranPipeline = false;

    private String resultsSummary;

    private String folderPath;

    TextView statusTextView;

    ProgressBar progressBar;

    private UploadServiceSingleBroadcastReceiver uploadReceiver;

    public static void logHandler(Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringBuilder newLogMessage = ServerConnectionUtils.getLogMessage();
                if (newLogMessage != null && newLogMessage.toString().trim().length() != 0) {
                    connectionLogText.setText(newLogMessage);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minit);

        statusTextView = findViewById(R.id.txt_status);
        progressBar = findViewById(R.id.progress_upload_status);

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        uploadReceiver = new UploadServiceSingleBroadcastReceiver(this);

        final EditText serverAddressInput = findViewById(R.id.input_server_address);
        connectionLogText = findViewById(R.id.text_conn_log);
        final Button btnRquestJob = findViewById(R.id.btn_request_job);
        btnSendResult = findViewById(R.id.btn_send_result);

        if (getIntent().getExtras() != null) {
            resultsSummary = getIntent().getExtras().getString("PIPELINE_STATUS");
            folderPath = getIntent().getExtras().getString("FOLDER_PATH");
            if (resultsSummary != null && !TextUtils.isEmpty(resultsSummary)) {
                ranPipeline = true;
                btnRquestJob.setText("Send Results");
            }
        }

        btnRquestJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (serverAddressInput.getText() != null && !TextUtils
                        .isEmpty(serverAddressInput.getText().toString().trim())) {
                    serverIP = serverAddressInput.getText().toString().trim();

                    ServerConnectionUtils.setServerAddress(serverIP);
                    if (ranPipeline) {
                        uploadDataSet();
                    } else {
                        requestJob();
                    }

                } else {
                    Toast.makeText(MinITActivity.this, "Please input a server IP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSendResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MinITActivity.this, DownloadActivity.class);
                // TODO Fix the following
                // Protocol, file server IP and Port
                intent.putExtra("DATA_SET_URL", "http://" + serverIP + ":8000/" + zipFileName);
                startActivity(intent);
            }
        });
    }

    private void requestJob() {
        ServerConnectionUtils.connectToServer(State.REQUEST, new ServerCallback() {
            @Override
            public void onSuccess(final WrapperObject job) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GUIConfiguration.configureSteps(job.getSteps());
                        zipFileName = job.getPrefix();
                        btnSendResult.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(final WrapperObject job) {

            }
        });
    }

    private void sendJobResults() {
        ServerConnectionUtils.setResultToWrapperObject(resultsSummary);
        ServerConnectionUtils.connectToServer(State.COMPLETED, new ServerCallback() {
            @Override
            public void onSuccess(final WrapperObject job) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MinITActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final WrapperObject job) {

            }
        });
    }

    private void uploadDataSet() {
        // TODO check wifi connectivity
        ZipManager zipManager = new ZipManager(new ZipListener() {
            @Override
            public void onStarted(@NonNull final long totalBytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setMax(100);
                        statusTextView.setText("Zip started");
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
                        statusTextView.setText("Zipping: " + perc + "%");
                    }
                });
            }

            @Override
            public void onComplete(@NonNull final boolean success, @Nullable final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            statusTextView.setText("Zip Successful");
                            String path = folderPath + ".zip";
                            try {
                                String uploadId =
                                        new BinaryUploadRequest(MinITActivity.this, "http://" + serverIP + ":8000/")
                                                .setFileToUpload(path)
                                                .setMethod("POST")
                                                .addHeader("file-name", new File(path).getName())
                                                .setNotificationConfig(new UploadNotificationConfig())
                                                .setMaxRetries(2)
                                                .startUpload();
                                // More info about receivers https://github.com/gotev/android-upload-service/wiki/Monitoring-upload-status
                                uploadReceiver.setUploadID(uploadId);
                            } catch (FileNotFoundException e) {
                                statusTextView.setText("File IO Error");
                                Log.e(TAG, "File IO Error: " + e);
                            } catch (MalformedURLException e) {
                                statusTextView.setText("Malformed URL Exception");
                                Log.e(TAG, "URL Error: " + e);
                            }
                        } else {
                            statusTextView.setText("Zip Error");
                        }
                    }
                });
            }
        });
        zipManager.zip(folderPath);
    }

    @Override
    public void onProgress(final Context context, final UploadInfo uploadInfo) {
        String totalBytes = Util.humanReadableBytes(uploadInfo.getTotalBytes(), true);
        String uploadedBytes = Util.humanReadableBytes(uploadInfo.getUploadedBytes(), true);
        String status = "Uploading: " + uploadedBytes + "/" + totalBytes;
        statusTextView.setText(status);
        progressBar.setProgress(uploadInfo.getProgressPercent());
    }

    @Override
    public void onError(final Context context, final UploadInfo uploadInfo, final ServerResponse serverResponse,
            final Exception exception) {
        statusTextView.setText("Result Upload failed: " + serverResponse.getHttpCode());
        Log.e(TAG, "Upload Failed: " + serverResponse.getBodyAsString());
    }

    @Override
    public void onCompleted(final Context context, final UploadInfo uploadInfo, final ServerResponse serverResponse) {
        statusTextView.setText("Result Upload completed: " + serverResponse.getHttpCode());
        sendJobResults();
    }

    @Override
    public void onCancelled(final Context context, final UploadInfo uploadInfo) {
        statusTextView.setText("Result Upload cancelled");
    }

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }

}
