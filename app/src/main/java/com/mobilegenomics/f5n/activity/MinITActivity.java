package com.mobilegenomics.f5n.activity;

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
import androidx.appcompat.app.AppCompatActivity;
import com.liulishuo.okdownload.core.Util;
import com.mobilegenomics.f5n.BuildConfig;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.dto.State;
import com.mobilegenomics.f5n.dto.WrapperObject;
import com.mobilegenomics.f5n.support.ServerCallback;
import com.mobilegenomics.f5n.support.ServerConnectionUtils;
import com.mobilegenomics.f5n.support.ZipManager;
import java.io.File;
import java.io.FileInputStream;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceSingleBroadcastReceiver;
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
            } else {
                statusTextView.setText("Upload Error");
            }
            sendJobResults();
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
            Log.d(TAG, "Uploading: " + downloaded + "/" + total);
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

    private UploadServiceSingleBroadcastReceiver uploadReceiver;

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
                intent.putExtra("DATA_SET_URL", serverIP);
                intent.putExtra("FILE_NAME", zipFileName);
                startActivity(intent);
            }
        });
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
                        Log.d("TAG", "Prefix = " + job.getPrefix());
                        Log.d("TAG", "Dir Path = " + job.getPathToDataDir());
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
        ZipManager zipManager = new ZipManager(MinITActivity.this);
        zipManager.zip(folderPath);
        String path = folderPath + ".zip";
        new FTPUploadTask().execute(serverIP, path);
    }
}
