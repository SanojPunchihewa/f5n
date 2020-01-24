package com.mobilegenomics.f5n.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import java.io.FileInputStream;
import java.util.Objects;
import net.gotev.uploadservice.UploadService;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamAdapter;

public class MinITActivity extends AppCompatActivity {

    class FTPUploadTask extends AsyncTask<String, Long, Boolean> {

        boolean status;

        long fileSize;

        @Override
        protected Boolean doInBackground(String... urls) {
            FTPClient con;
            try {
                con = new FTPClient();
                con.setDefaultPort(8000);
                con.connect(urls[0]);

                con.setCopyStreamListener(new CopyStreamAdapter() {
                    @Override
                    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                        //this method will be called every time some bytes are transferred
//                        int percent = (int) (totalBytesTransferred * 100 / fileSize);
                        publishProgress(totalBytesTransferred);
                    }

                });

                if (con.login("test", "test")) {
                    con.enterLocalPassiveMode(); // important!
                    con.setFileType(FTP.BINARY_FILE_TYPE);
                    con.setBufferSize(1024000);
                    File fileIn = new File(urls[1]);
                    fileSize = fileIn.length();

                    FileInputStream in = new FileInputStream(fileIn);
                    boolean result = con.storeFile(new File(urls[1]).getName(), in);
                    in.close();
                    status = result;
                    con.logout();
                    con.disconnect();
                }
            } catch (Exception e) {
                Log.e(TAG, "Upload Error: ", e);
                status = false;
            }
            return status;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            statusTextView.setText("Upload cancelled");
        }

        @Override
        protected void onPostExecute(final Boolean uploadSuccess) {
            super.onPostExecute(uploadSuccess);
            Log.i(TAG, "Upload Finished");
            if (uploadSuccess) {
                statusTextView.setText("Upload Completed");
                sendJobResults();
            } else {
                statusTextView.setText("Upload Error");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            statusTextView.setText("Upload started");
        }

        @Override
        protected void onProgressUpdate(final Long... values) {
            super.onProgressUpdate(values);
            String total = Util.humanReadableBytes(fileSize, true);
            String downloaded = Util.humanReadableBytes(values[0], true);
            statusTextView.setText("Uploading: " + downloaded + "/" + total);
            float percent = (float) values[0] / fileSize;
            progressBar.setProgress((int) percent * progressBar.getMax());
        }
    }

    private static final String TAG = MinITActivity.class.getSimpleName();

    private static TextView connectionLogText;

    ProgressBar progressBar;

    TextView statusTextView;

    private Button btnSendResult;

    private String folderPath;

    private boolean ranPipeline = false;

    private String resultsSummary;

    private String serverIP;

    EditText serverAddressInput;

    private String zipFileName;

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

        serverAddressInput = findViewById(R.id.input_server_address);

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
                intent.putExtra("DATA_SET_URL", serverIP);
                intent.putExtra("FILE_NAME", zipFileName);
                startActivity(intent);
            }
        });
    }

    public void copyIPAddress(View view) {
        if (serverAddressInput.getText() != null && !TextUtils.isEmpty(serverAddressInput.getText().toString())) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("serverIP", serverAddressInput.getText().toString());
            clipboard.setPrimaryClip(clip);
        }
    }

    public void pasteIPAddress(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            ClipData.Item item = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0);
            String IPAddress = item.getText().toString();
            if (!TextUtils.isEmpty(IPAddress) && validateIPAddress(IPAddress)) {
                serverAddressInput.setText(IPAddress);
            } else {
                Toast.makeText(MinITActivity.this, "Invalid IP Address", Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException e) {
            Toast.makeText(MinITActivity.this, "No IP Address was copied", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestJob() {
        ServerConnectionUtils.connectToServer(State.REQUEST, new ServerCallback() {
            @Override
            public void onError(final WrapperObject job) {

            }

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
        });
    }

    private void sendJobResults() {
        ServerConnectionUtils.setResultToWrapperObject(resultsSummary);
        ServerConnectionUtils.connectToServer(State.COMPLETED, new ServerCallback() {
            @Override
            public void onError(final WrapperObject job) {

            }

            @Override
            public void onSuccess(final WrapperObject job) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MinITActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void uploadDataSet() {
        // TODO check wifi connectivity
        ZipManager zipManager = new ZipManager(MinITActivity.this, new ZipListener() {
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
                            String path = folderPath + ".out.zip";
                            new FTPUploadTask().execute(serverIP, path);
                        } else {
                            statusTextView.setText("Zip Error");
                        }
                    }
                });
            }
        });
        zipManager.zip(folderPath);
    }

    private boolean validateIPAddress(final String ip) {
        String PATTERN
                = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }

}
