package com.mobilegenomics.f5n.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
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

import com.liulishuo.okdownload.core.cause.EndCause;
import com.mobilegenomics.f5n.BuildConfig;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.Step;
import com.mobilegenomics.f5n.dto.State;
import com.mobilegenomics.f5n.dto.WrapperObject;
import com.mobilegenomics.f5n.support.DownloadListener;
import com.mobilegenomics.f5n.support.FTPManager;
import com.mobilegenomics.f5n.support.LogHandler;
import com.mobilegenomics.f5n.support.PipelineState;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import com.mobilegenomics.f5n.support.ServerCallback;
import com.mobilegenomics.f5n.support.ServerConnectionUtils;
import com.mobilegenomics.f5n.support.ZipListener;
import com.mobilegenomics.f5n.support.ZipManager;
import com.obsez.android.lib.filechooser.ChooserDialog;

import net.gotev.uploadservice.UploadService;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class MinITActivity extends AppCompatActivity {

    private static final String TAG = MinITActivity.class.getSimpleName();

    private static boolean AUTOMATED = false;

    public TextView connectionLogText;

    ProgressBar progressBar;

    TextView statusTextView;

    private TableRow trSendResults;

    private TableRow trBackToRequestJob;

    private Button btnProcessJob;

    private Button btnRequestJob;

    private Button btnBackToRequestJob;

    private Button btnSendResults;

    private String folderPath;

    private String resultsSummary;

    private String serverIP;

    EditText serverAddressInput;

    private String zipFileName;

    private ArrayList<String> fileList;

    private String DEFAULT_STORAGE_PATH = Environment.getExternalStorageDirectory() + "/mobile-genomics/";

    private String STORAGE_PATH = PreferenceUtil
            .getSharedPreferenceString(R.string.key_storage_preference, DEFAULT_STORAGE_PATH);

    private static final String DATA_SET = "\\$DATA_SET/";

    private static final String REFERENCE_GNOME = "\\$REF_GNOME/";

    public static void logHandler(LogHandler logHandler) {
        logHandler.post(new Runnable() {
            @Override
            public void run() {
                StringBuilder newLogMessage = ServerConnectionUtils.getLogMessage();
                if (newLogMessage != null && newLogMessage.toString().trim().length() != 0) {
                    logHandler.getmActivity().get().connectionLogText.setText(newLogMessage);
                    PreferenceUtil.setSharedPreferenceString(R.string.id_prev_conn_log, newLogMessage.toString());
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minit);

        if (PreferenceUtil.getSharedPreferenceInt(R.string.id_app_mode) == PipelineState.MINIT_DOWNLOAD.ordinal() &&
                PreferenceUtil.getSharedPreferenceObject(R.string.id_wrapper_obj) != null) {
            showResumeMessage(PipelineState.MINIT_DOWNLOAD);
        } else if (PreferenceUtil.getSharedPreferenceInt(R.string.id_app_mode) == PipelineState.MINIT_EXTRACT.ordinal() &&
                PreferenceUtil.getSharedPreferenceObject(R.string.id_wrapper_obj) != null) {
            showResumeMessage(PipelineState.MINIT_EXTRACT);
        } else if (PreferenceUtil.getSharedPreferenceInt(R.string.id_app_mode) == PipelineState.MINIT_CONFIGURE.ordinal() &&
                PreferenceUtil.getSharedPreferenceObject(R.string.id_wrapper_obj) != null) {
            showResumeMessage(PipelineState.MINIT_CONFIGURE);
        }

        statusTextView = findViewById(R.id.txt_status);
        progressBar = findViewById(R.id.progress_upload_status);

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        serverAddressInput = findViewById(R.id.input_server_address);

        connectionLogText = findViewById(R.id.text_conn_log);

        trSendResults = findViewById(R.id.tr_select_files_send);
        trBackToRequestJob = findViewById(R.id.tr_back_to_req_job);

        btnRequestJob = findViewById(R.id.btn_request_job);
        btnProcessJob = findViewById(R.id.btn_process_job);
        btnBackToRequestJob = findViewById(R.id.btn_back_to_req_job);
        btnSendResults = findViewById(R.id.btn_send_result);

        if (getIntent().getExtras() != null) {
            resultsSummary = getIntent().getExtras().getString("PIPELINE_STATUS");
            folderPath = getIntent().getExtras().getString("FOLDER_PATH");
            if (resultsSummary != null && !TextUtils.isEmpty(resultsSummary)) {
                if (MinITActivity.isAUTOMATED()) {
                    btnRequestJob.setVisibility(View.GONE);
                    trSendResults.setVisibility(View.GONE);
                    trBackToRequestJob.setVisibility(View.VISIBLE);
                    compressDataSet();
                } else {
                    btnRequestJob.setVisibility(View.GONE);
                    trSendResults.setVisibility(View.VISIBLE);
                    trBackToRequestJob.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (PreferenceUtil.getSharedPreferenceInt(R.string.id_app_mode) == PipelineState.MINIT_COMPRESS.ordinal()) {
                showResumeMessage(PipelineState.MINIT_COMPRESS);
            } else if (PreferenceUtil.getSharedPreferenceInt(R.string.id_app_mode) == PipelineState.MINIT_UPLOAD.ordinal()) {
                showResumeMessage(PipelineState.MINIT_UPLOAD);
            }
        }

        btnRequestJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (serverAddressInput.getText() != null && !TextUtils
                        .isEmpty(serverAddressInput.getText().toString().trim())) {
                    serverIP = serverAddressInput.getText().toString().trim();

                    ServerConnectionUtils.setServerAddress(serverIP);
                    ServerConnectionUtils.clearLogMessage();
                    connectionLogText.setText(null);
                    statusTextView.setText(null);
                    requestJob();
                } else {
                    Toast.makeText(MinITActivity.this, "Please input a server IP", Toast.LENGTH_SHORT).show();
                }
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
                processJob();
            }
        });

        btnBackToRequestJob.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GUIConfiguration.getPipelineState() == PipelineState.MINIT_UPLOAD) {
                    new AlertDialog.Builder(MinITActivity.this)
                            .setTitle("Confirm Action")
                            .setMessage("Do you want to go back before uploading the results?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    returnToAnotherRequestJob();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    btnRequestJob.setVisibility(View.VISIBLE);
                    trSendResults.setVisibility(View.GONE);
                    trBackToRequestJob.setVisibility(View.GONE);
                    connectionLogText.setText(null);
                    statusTextView.setText(null);
                }
            }
        });
    }

    private void processJob() {
        PreferenceUtil
                .setSharedPreferenceObject(R.string.id_wrapper_obj, ServerConnectionUtils.getWrapperObject());
        // TODO Fix the following
        // Protocol, file server IP and Port
        downloadDataSetFTP();
    }

    private void configureStepFolderPath() {
        String dataSetFolderPath = STORAGE_PATH + zipFileName.substring(0, zipFileName.length() - 4) + "/";
        String referenceGnomePath = PreferenceUtil
                .getSharedPreferenceString(R.string.key_reference_gnome, dataSetFolderPath);
        for (Step step : GUIConfiguration.getSteps()) {
            step.setCommandString(step.getCommandString().replaceAll(DATA_SET, dataSetFolderPath));
            step.setCommandString(step.getCommandString().replaceAll(REFERENCE_GNOME, referenceGnomePath));
        }
    }

    private void downloadDataSetFTP() {
        serverIP = ServerConnectionUtils.getServerAddress();
        String url = serverIP + "/" + zipFileName;
        new FTPManager().download(url, STORAGE_PATH, this,
                new DownloadListener() {
                    @Override
                    public void onComplete(@NonNull final EndCause cause, @Nullable final Exception realCause) {
                        if (cause == EndCause.COMPLETED) {
                            GUIConfiguration.setPipelineState(PipelineState.MINIT_EXTRACT);
                            extractZip(STORAGE_PATH + zipFileName);
                        } else {
                            statusTextView.setText(
                                    "Unable to start the unzipping process due to an error in the download process");
                        }
                    }
                });
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
                            configureStepFolderPath();
                            GUIConfiguration.setPipelineState(PipelineState.MINIT_CONFIGURE);
                            if (AUTOMATED) {
                                automatedSetUpLaunch(filePath);
                            } else {
                                Intent intent = new Intent(MinITActivity.this, TerminalActivity.class);
                                intent.putExtra("FOLDER_PATH", STORAGE_PATH);
                                startActivity(intent);
                            }
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
        ServerConnectionUtils.connectToServer(State.REQUEST, this, new ServerCallback() {
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
                        GUIConfiguration.setPipelineState(PipelineState.MINIT_DOWNLOAD);
                        setAUTOMATED(true);
                        if (isAUTOMATED()) {
                            btnProcessJob.setVisibility(View.GONE);
                            processJob();
                        } else {
                            btnProcessJob.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    private void sendJobResults() {
        resultsSummary = PreferenceUtil.getSharedPreferenceString(R.string.id_results_summary);
        ServerConnectionUtils.setResultToWrapperObject(resultsSummary);
        ServerConnectionUtils.connectToServer(State.COMPLETED, this, new ServerCallback() {
            @Override
            public void onError(final WrapperObject job) {

            }

            @Override
            public void onSuccess(final WrapperObject job) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GUIConfiguration.setPipelineState(PipelineState.STATE_ZERO);
                        Toast.makeText(MinITActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void uploadDataSetFTP(String filePath) {
        serverIP = ServerConnectionUtils.getServerAddress();
        new FTPManager().upload(serverIP, filePath, this, new DownloadListener() {
            @Override
            public void onComplete(@NonNull EndCause cause, @Nullable Exception realCause) {
                if (cause == EndCause.COMPLETED) {
                    sendJobResults();
                } else {
                    showManualCompressUploadMessage();
                }
            }
        });
    }

    private void compressDataSet() {
        // TODO check wifi connectivity
        if (isAUTOMATED()) {
            Set<String> files = PreferenceUtil.getSharedPreferencesStringList(R.string.key_min_it_upload_files);
            if (files != null && !files.isEmpty()) {
                fileList = new ArrayList<>();
                for (String fileName : files) {
                    fileList.add(folderPath.substring(0, folderPath.length() - 4) + "/" + fileName);
                }
            } else {
                getFileList(folderPath.substring(0, folderPath.length() - 4));
            }
            fileList.add(folderPath.substring(0, folderPath.length() - 4));
        }
        folderPath = fileList.get(fileList.size() - 1);
        String zipFileName = folderPath + "/" + folderPath.substring(folderPath.lastIndexOf("/") + 1)
                + "_output.zip";

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
                            GUIConfiguration.setPipelineState(PipelineState.MINIT_UPLOAD);
                            PreferenceUtil.setSharedPreferenceString(R.string.id_compressed_file, zipFileName);
                            uploadDataSetFTP(zipFileName);
                        } else {
                            statusTextView.setText("Zip Error");
                            showManualCompressUploadMessage();
                        }
                    }
                });
            }
        });

        fileList.remove(fileList.size() - 1);
        zipManager.zip(fileList, zipFileName);
    }

    private void showManualCompressUploadMessage() {
        String title, msg;
        if (GUIConfiguration.getPipelineState() == PipelineState.MINIT_COMPRESS) {
            title = "Compress files manually";
            msg = "One or more files selected to upload does not exists. Please select files manually and compress to upload";
        } else {
            title = "Upload files manually";
            msg = "File upload failed. Please try manual upload";
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        WrapperObject prevJob = (WrapperObject) PreferenceUtil.getSharedPreferenceObject(R.string.id_wrapper_obj);
                        ServerConnectionUtils.setWrapperObject(prevJob);
                        btnRequestJob.setVisibility(View.GONE);
                        trSendResults.setVisibility(View.VISIBLE);
                        trBackToRequestJob.setVisibility(View.VISIBLE);
                        setAUTOMATED(false);
                    }
                })
                .setNegativeButton("Get new Job", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        returnToAnotherRequestJob();
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void returnToAnotherRequestJob() {
        GUIConfiguration.setPipelineState(PipelineState.STATE_ZERO);
        btnRequestJob.setVisibility(View.VISIBLE);
        trSendResults.setVisibility(View.GONE);
        trBackToRequestJob.setVisibility(View.GONE);
        connectionLogText.setText(null);
        statusTextView.setText(null);
    }

    private void getFileList(String path) {
        // TODO consider about getting file names inside folders
        fileList = new ArrayList<>();
        File dir = new File(path);
        for (File f : dir.listFiles()) {
            if (f.isFile())
                fileList.add(dir + "/" + f.getName());
        }
    }

    private void openFileManager(boolean dirOnly, boolean toCompress) {
        fileList = new ArrayList<>();
        final boolean[] isCancelled = {false};

        if (PreferenceUtil.getSharedPreferenceInt(R.string.id_app_mode) == PipelineState.MINIT_UPLOAD.ordinal()) {
            zipFileName = PreferenceUtil.getSharedPreferenceString(R.string.id_compressed_file);
            uploadDataSetFTP(zipFileName);
        } else {
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
                            isCancelled[0] = true;
                            dialog.cancel();
                        }
                    })
                    .withNegativeButtonListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            isCancelled[0] = true;
                        }
                    })
                    .withOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(final DialogInterface dialog) {
                            if (!fileList.isEmpty() && !isCancelled[0]) {
                                compressDataSet();
                            } else {
                                Toast.makeText(MinITActivity.this, "No files selected to compress",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .withResources(R.string.title_choose_any_file, R.string.title_choose, R.string.dialog_cancel)
                    .build()
                    .show();
        }
    }

    private void showResumeMessage(PipelineState state) {

        String msg = null;

        if (state == PipelineState.MINIT_DOWNLOAD) {
            msg = "You can download the data set for previous job";
        } else if (state == PipelineState.MINIT_EXTRACT) {
            msg = "You can unzip the previous job";
        } else if (state == PipelineState.MINIT_CONFIGURE) {
            msg = "You can reconfigure and run the previous job";
        } else if (state == PipelineState.MINIT_COMPRESS) {
            msg = "You can upload the result from previous job";
        } else if (state == PipelineState.MINIT_UPLOAD) {
            msg = "You can upload the result from previous job";
        }

        new AlertDialog.Builder(this)
                .setTitle("Resume App State")
                .setMessage(msg)
                .setPositiveButton("Resume", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        WrapperObject prevJob = (WrapperObject) PreferenceUtil.getSharedPreferenceObject(R.string.id_wrapper_obj);
                        ServerConnectionUtils.setWrapperObject(prevJob);
                        String previousConnectionLog = PreferenceUtil.getSharedPreferenceString(R.string.id_prev_conn_log);
                        if (state == PipelineState.MINIT_DOWNLOAD && prevJob != null) {
                            GUIConfiguration.configureSteps(prevJob.getSteps());
                            zipFileName = prevJob.getPrefix() + ".zip";
                            connectionLogText.setText(previousConnectionLog);
                            btnProcessJob.setVisibility(View.VISIBLE);
                            GUIConfiguration.setPipelineState(PipelineState.MINIT_DOWNLOAD);
                        } else if (state == PipelineState.MINIT_EXTRACT) {
                            connectionLogText.setText(previousConnectionLog);
                            GUIConfiguration.configureSteps(prevJob.getSteps());
                            zipFileName = prevJob.getPrefix() + ".zip";
                            extractZip(STORAGE_PATH + zipFileName);
                        } else if (state == PipelineState.MINIT_CONFIGURE) {
                            GUIConfiguration
                                    .setSteps(PreferenceUtil.getSharedPreferenceStepList(R.string.id_step_list));
                            Intent intent = new Intent(MinITActivity.this, TerminalActivity.class);
                            startActivity(intent);
                            GUIConfiguration.setPipelineState(PipelineState.MINIT_CONFIGURE);
                        } else if (state == PipelineState.MINIT_COMPRESS) {
                            btnRequestJob.setVisibility(View.GONE);
                            trSendResults.setVisibility(View.VISIBLE);
                            trBackToRequestJob.setVisibility(View.VISIBLE);
                            resultsSummary = PreferenceUtil.getSharedPreferenceString(R.string.id_results_summary);
                            GUIConfiguration.setPipelineState(PipelineState.MINIT_COMPRESS);
                        } else if (state == PipelineState.MINIT_UPLOAD) {
                            btnRequestJob.setVisibility(View.GONE);
                            trSendResults.setVisibility(View.VISIBLE);
                            trBackToRequestJob.setVisibility(View.VISIBLE);
                            resultsSummary = PreferenceUtil.getSharedPreferenceString(R.string.id_results_summary);
                            GUIConfiguration.setPipelineState(PipelineState.MINIT_UPLOAD);
                        }
                    }
                })
                .setNegativeButton("Get new Job", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        GUIConfiguration.setPipelineState(PipelineState.STATE_ZERO);
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void automatedSetUpLaunch(String filePath) {
        GUIConfiguration.createPipeline();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Setting up to execute job");
        System.gc();
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                progressDialog.dismiss();
                finish();
                Intent intent = new Intent(MinITActivity.this, ConfirmationActivity.class);
                intent.putExtra("FOLDER_PATH", filePath);
                startActivity(intent);
            }
        }, 2500);
    }

    public static boolean isAUTOMATED() {
        return AUTOMATED;
    }

    public static void setAUTOMATED(boolean AUTOMATED) {
        MinITActivity.AUTOMATED = AUTOMATED;
    }

    private boolean validateIPAddress(final String ip) {
        String PATTERN
                = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }
}
