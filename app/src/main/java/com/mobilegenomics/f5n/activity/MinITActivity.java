package com.mobilegenomics.f5n.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.liulishuo.okdownload.core.Util;
import com.mobilegenomics.f5n.BuildConfig;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.Step;
import com.mobilegenomics.f5n.dto.State;
import com.mobilegenomics.f5n.dto.WrapperObject;
import com.mobilegenomics.f5n.support.PipelineState;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import com.mobilegenomics.f5n.support.ServerCallback;
import com.mobilegenomics.f5n.support.ServerConnectionUtils;
import com.mobilegenomics.f5n.support.TimeFormat;
import com.mobilegenomics.f5n.support.ZipListener;
import com.mobilegenomics.f5n.support.ZipManager;
import com.obsez.android.lib.filechooser.ChooserDialog;

import net.gotev.uploadservice.UploadService;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class MinITActivity extends AppCompatActivity {

    class FTPUploadTask extends AsyncTask<String, Long, Boolean> {

        boolean status;

        long fileSize;

        long uploadStartTime;

        @Override
        protected Boolean doInBackground(String... urls) {
            FTPClient con;
            try {

                Log.e(TAG, "Address = " + urls[0]);
                Log.e(TAG, "File path = " + urls[1]);

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
                    String filePath = "outputs/" + new File(urls[1]).getName();
                    boolean result = con.storeFile(filePath, in);
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
            long uploadTime = System.currentTimeMillis() - uploadStartTime;
            if (uploadSuccess) {
                String time = TimeFormat.millisToShortDHMS(uploadTime);
                GUIConfiguration.setPipelineState(PipelineState.COMPLETED);
                PreferenceUtil
                        .setSharedPreferenceInt(R.string.id_app_mode, GUIConfiguration.getPipelineState().ordinal());
                statusTextView.setText("Upload Completed in " + time);
                sendJobResults();
            } else {
                statusTextView.setText("Upload Error");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uploadStartTime = System.currentTimeMillis();
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

    private static final String DATA_SET_PATH = "\\$DATA_SET_PATH";

    private static TextView connectionLogText;

    ProgressBar progressBar;

    TextView statusTextView;

    private TableRow trSendResults;

    private Button btnProcessJob;

    private Button btnRequestJob;

    private Button btnCompressFiles;

    private Button btnSendResults;

    private String folderPath;

    private String resultsSummary;

    private String serverIP;

    EditText serverAddressInput;

    private String zipFileName;

    private ArrayList<String> fileList;

    private String DATASET_FOLDER = Environment.getExternalStorageDirectory() + "/mobile-genomics/";

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

        if (PreferenceUtil.getSharedPreferenceInt(R.string.id_app_mode) == PipelineState.MINIT_RUNNING.ordinal() &&
                PreferenceUtil.getSharedPreferenceStepList(R.string.id_step_list) != null &&
                PreferenceUtil.getSharedPreferenceObject(R.string.id_wrapper_obj) != null) {
            showResumeMessage(PipelineState.MINIT_RUNNING);
        }

        statusTextView = findViewById(R.id.txt_status);
        progressBar = findViewById(R.id.progress_upload_status);

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        serverAddressInput = findViewById(R.id.input_server_address);

        connectionLogText = findViewById(R.id.text_conn_log);

        trSendResults = findViewById(R.id.tr_select_files_send);

        btnRequestJob = findViewById(R.id.btn_request_job);
        btnProcessJob = findViewById(R.id.btn_process_job);
        btnCompressFiles = findViewById(R.id.btn_select_files);
        btnSendResults = findViewById(R.id.btn_send_result);

        if (getIntent().getExtras() != null) {
            resultsSummary = getIntent().getExtras().getString("PIPELINE_STATUS");
            folderPath = getIntent().getExtras().getString("FOLDER_PATH");
            if (resultsSummary != null && !TextUtils.isEmpty(resultsSummary)) {
                btnRequestJob.setVisibility(View.GONE);
                trSendResults.setVisibility(View.VISIBLE);
            }
        } else {
            if (PreferenceUtil.getSharedPreferenceInt(R.string.id_app_mode) == PipelineState.TO_BE_UPLOAD.ordinal()) {
                showResumeMessage(PipelineState.TO_BE_UPLOAD);
            }
        }

        btnRequestJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (serverAddressInput.getText() != null && !TextUtils
                        .isEmpty(serverAddressInput.getText().toString().trim())) {
                    serverIP = serverAddressInput.getText().toString().trim();

                    ServerConnectionUtils.setServerAddress(serverIP);
                    requestJob();
                } else {
                    Toast.makeText(MinITActivity.this, "Please input a server IP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCompressFiles.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openFileManager(false, true);
            }
        });

        btnSendResults.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (serverAddressInput.getText() != null && !TextUtils
                        .isEmpty(serverAddressInput.getText().toString().trim())) {
                    serverIP = serverAddressInput.getText().toString().trim();
                    ServerConnectionUtils.setServerAddress(serverIP);
                    openFileManager(false, false);
                } else {
                    Toast.makeText(MinITActivity.this, "Please input a server IP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnProcessJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PreferenceUtil
                        .setSharedPreferenceObject(R.string.id_wrapper_obj, ServerConnectionUtils.getWrapperObject());

                configureStepFolderPath();
                // TODO Fix the following
                // Protocol, file server IP and Port
                downloadDataSetFTP();
            }
        });
    }

    private void configureStepFolderPath() {
        for (Step step : GUIConfiguration.getSteps()) {
            step.setCommandString(step.getCommandString().replaceAll(DATA_SET_PATH, PreferenceUtil.getSharedPreferenceString(R.string.key_default_storage)));
        }
    }

    private void downloadDataSetFTP() {
        new MinITActivity.FTPDownloadTask().execute(serverIP, zipFileName);
    }

    private void extractZip(String filePath) {

        ZipManager zipManager = new ZipManager(MinITActivity.this, new ZipListener() {
            @Override
            public void onStarted(@NonNull final long totalBytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setMax(100);
                        statusTextView.setText("Unzip started");
                        connectionLogText.append("Extracting data set...\n");
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
                            connectionLogText.append("Extracting data set completed\n");

                            GUIConfiguration.setPipelineState(PipelineState.TO_BE_CONFIGURED);
                            Intent intent = new Intent(MinITActivity.this, TerminalActivity.class);
                            intent.putExtra("FOLDER_PATH", DATASET_FOLDER);
                            startActivity(intent);
                        } else {
                            statusTextView.setText("Unzip Error");
                            connectionLogText.append("Extracting data set error\n");
                        }
                    }
                });
            }
        });
        Uri treeUri = PreferenceUtil.getSharedPreferenceUri(R.string.sdcard_uri);
        zipManager.unzip(treeUri, filePath);
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
                        zipFileName = job.getPrefix() + ".zip";
                        btnProcessJob.setVisibility(View.VISIBLE);
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

    private void uploadDataSet(String filePath) {
        new FTPUploadTask().execute(serverIP, filePath);
    }

    private void compressDataSet() {
        // TODO check wifi connectivity
        folderPath = fileList.get(fileList.size() - 1);
        String zipFileName = folderPath + "/" + folderPath.substring(folderPath.lastIndexOf("/") + 1)
                + ".out.zip";

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
                        } else {
                            statusTextView.setText("Zip Error");
                        }
                    }
                });
            }
        });

        fileList.remove(fileList.size() - 1);
        zipManager.zip(fileList, zipFileName);
    }

    private void openFileManager(boolean dirOnly, boolean toCompress) {
        fileList = new ArrayList<>();

        if (toCompress) {
            new ChooserDialog(MinITActivity.this)
                    .withFilter(dirOnly, false)
                    .enableMultiple(true)
                    .withChosenListener(new ChooserDialog.Result() {
                        @Override
                        public void onChoosePath(String path, File pathFile) {
                            if (fileList.contains(path)) {
                                fileList.remove(path);
                            } else {
                                fileList.add(path);
                            }
                        }
                    })
                    // to handle the back key pressed or clicked outside the dialog:
                    .withOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            dialog.cancel();
                        }
                    })
                    .withOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(final DialogInterface dialog) {
                            compressDataSet();
                        }
                    })
                    .withResources(R.string.title_choose_any_file, R.string.title_choose, R.string.dialog_cancel)
                    .build()
                    .show();
        } else {
            new ChooserDialog(MinITActivity.this)
                    .withFilter(dirOnly, false)
                    // to handle the result(s)
                    .withChosenListener(new ChooserDialog.Result() {
                        @Override
                        public void onChoosePath(String path, File pathFile) {
                            uploadDataSet(path);
                        }
                    })
                    .build()
                    .show();
        }
    }

    private void showResumeMessage(PipelineState state) {

        String msg;

        if (state == PipelineState.TO_BE_UPLOAD) {
            msg = "You can upload the data from previous job";
        } else {
            msg = "You can re run the previous job";
        }

        new AlertDialog.Builder(this)
                .setTitle("Resume App State")
                .setMessage(msg)
                .setPositiveButton("Resume", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ServerConnectionUtils.setWrapperObject(
                                (WrapperObject) PreferenceUtil
                                        .getSharedPreferenceObject(R.string.id_wrapper_obj));
                        if (state == PipelineState.TO_BE_UPLOAD) {
                            btnRequestJob.setVisibility(View.GONE);
                            trSendResults.setVisibility(View.VISIBLE);
                            resultsSummary = PreferenceUtil.getSharedPreferenceString(R.string.id_results_summary);
                        } else {
                            GUIConfiguration.setPipelineState(PipelineState.CONFIGURED);
                            GUIConfiguration
                                    .setSteps(PreferenceUtil.getSharedPreferenceStepList(R.string.id_step_list));
                            Intent intent = new Intent(MinITActivity.this, TerminalActivity.class);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("Get new Job", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private boolean validateIPAddress(final String ip) {
        String PATTERN
                = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }

    class FTPDownloadTask extends AsyncTask<String, Long, Boolean> {

        long downloadStartTime;

        long fileSize;

        boolean status;

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
                        publishProgress(totalBytesTransferred);
                    }

                });

                if (con.login("test", "test")) {
                    con.enterLocalPassiveMode(); // important!
                    con.setFileType(FTP.BINARY_FILE_TYPE);
                    con.setBufferSize(1024000);
                    FTPFile[] ff = con.listFiles(urls[1]);

                    if (ff != null) {
                        fileSize = (ff[0].getSize());
                    }

                    File dir = new File(DATASET_FOLDER + urls[1]);

                    OutputStream out = new FileOutputStream(dir);
                    status = con.retrieveFile(urls[1], out);
                    out.close();
                    con.logout();
                    con.disconnect();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e);
                status = false;
            }
            return status;
        }

        @Override
        protected void onPostExecute(final Boolean downloadSuccess) {
            super.onPostExecute(downloadSuccess);
            Log.i(TAG, "Download Finished");
            connectionLogText.append("Download completed\n");
            long downloadTime = System.currentTimeMillis() - downloadStartTime;
            if (downloadSuccess) {
                String time = TimeFormat.millisToShortDHMS(downloadTime);
                statusTextView.setText("Download Completed in " + time);
                extractZip(DATASET_FOLDER + zipFileName);
            } else {
                statusTextView.setText("Download Error");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "Download Started");
            connectionLogText.append("\nDownload Started for" + zipFileName + "\n");
            downloadStartTime = System.currentTimeMillis();
            statusTextView.setText("Download Started");
            progressBar.setMax(100);
        }

        @Override
        protected void onProgressUpdate(final Long... values) {
            super.onProgressUpdate(values);
            String total = Util.humanReadableBytes(fileSize, true);
            String downloaded = Util.humanReadableBytes(values[0], true);
            statusTextView.setText("Downloading: " + downloaded + "/" + total);
            float percent = (float) values[0] / fileSize;
            progressBar.setProgress((int) percent * progressBar.getMax());
        }
    }
}
