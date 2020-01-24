package com.mobilegenomics.f5n.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.AppMode;
import com.mobilegenomics.f5n.support.DownloadListener;
import com.mobilegenomics.f5n.support.DownloadManager;
import com.mobilegenomics.f5n.support.PipelineState;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import com.mobilegenomics.f5n.support.ZipListener;
import com.mobilegenomics.f5n.support.ZipManager;
import com.obsez.android.lib.filechooser.ChooserDialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;

public class DownloadActivity extends AppCompatActivity {

    class FTPDownloadTask extends AsyncTask<String, Long, Boolean> {

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

                    OutputStream out = new FileOutputStream(new File(folderPath + "/" + urls[1]));
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
            if (downloadSuccess) {
                statusTextView.setText("Download Completed");
            } else {
                statusTextView.setText("Download Error");
            }
            enableButtons();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "Download Started");
            statusTextView.setText("Download Started");
            progressBar.setMax(100);
            disableButtons();
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

    private static final String TAG = DownloadActivity.class.getSimpleName();

    private static String folderPath;

    private static final String ecoliDataSetURL = "https://zanojmobiapps.com/_tmp/genome/ecoli/ecoli-data-set.zip";

    Button btnDownload;

    Button btnDownloadEcoli;

    Button btnExtract;

    Button btnRunPipeline;

    Button btnSelectFilePath;

    EditText filePathInput;

    EditText folderPathInput;

    LinearLayout linearLayout;

    ProgressBar progressBar;

    TextView statusTextView;

    EditText urlInputPath;

    private DownloadTask downloadTask;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        linearLayout = findViewById(R.id.vertical_linear_layout);

        urlInputPath = new EditText(this);
        urlInputPath.setHint("Url of the data set");
        linearLayout.addView(urlInputPath);

        if (getIntent().getExtras() != null) {
            String path = getIntent().getExtras().getString("DATA_SET_URL");
            String fileName = getIntent().getExtras().getString("FILE_NAME");
            if (path != null && !TextUtils.isEmpty(path)) {
                urlInputPath.setText(path + "/" + fileName + ".zip");
            }
        }

        folderPathInput = new EditText(this);
        folderPathInput.setHint("Path to download data");
        linearLayout.addView(folderPathInput);

        Button btnSetFolderPath = new Button(this);
        btnSetFolderPath.setText("Select Folder");
        btnSetFolderPath.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openFileManager(true);
            }
        });
        linearLayout.addView(btnSetFolderPath);

        statusTextView = new TextView(this);
        linearLayout.addView(statusTextView);

        progressBar = new ProgressBar(this,
                null,
                android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgress(0);
        progressBar.setProgressTintList(ColorStateList
                .valueOf(Color.BLACK));
        linearLayout.addView(progressBar);

        btnDownload = new Button(this);
        btnDownload.setText("Download Data");
        btnDownload.setEnabled(false);
        linearLayout.addView(btnDownload);

        btnDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (urlInputPath.getText() != null && !TextUtils.isEmpty(urlInputPath.getText().toString().trim())) {
                    //downloadDataSet(urlInputPath.getText().toString().trim());
                    downloadDatasetFTP(urlInputPath.getText().toString().trim());
                } else {
                    Toast.makeText(DownloadActivity.this, "Please input a URL", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDownloadEcoli = new Button(this);
        btnDownloadEcoli.setText("Download Sample ecoli DataSet");
        btnDownloadEcoli.setEnabled(false);
        linearLayout.addView(btnDownloadEcoli);

        btnDownloadEcoli.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                downloadDataSet(ecoliDataSetURL);
            }
        });

        filePathInput = new EditText(this);
        LinearLayout.LayoutParams editText_LayoutParams =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        editText_LayoutParams.setMargins(0, 150, 0, 0);
        filePathInput.setLayoutParams(editText_LayoutParams);
        filePathInput.setHint("Path to compressed file");
        linearLayout.addView(filePathInput);

        btnSelectFilePath = new Button(this);
        btnSelectFilePath.setText("Select File");
        linearLayout.addView(btnSelectFilePath);

        btnSelectFilePath.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openFileManager(false);
            }
        });

        btnExtract = new Button(this);
        btnExtract.setText("Extract");
        btnExtract.setEnabled(false);
        linearLayout.addView(btnExtract);

        btnExtract.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!TextUtils.isEmpty(folderPath)) {
                    if (GUIConfiguration.getAppMode() == AppMode.SLAVE) {
                        btnRunPipeline.setVisibility(View.VISIBLE);
                    }
                    extractZip(folderPath);
                } else {
                    Toast.makeText(DownloadActivity.this, "Please Select a Zip file", Toast.LENGTH_SHORT).show();
                }

            }
        });

        TextView txtSDCardWarning = new TextView(this);
        txtSDCardWarning.setText(
                "Cannot download or extract to SD card? Please check Help -> View Tutorial");
        txtSDCardWarning.setTextColor(getResources().getColor(R.color.colorRead));
        linearLayout.addView(txtSDCardWarning);

        if (GUIConfiguration.getAppMode() == AppMode.SLAVE) {
            btnRunPipeline = new Button(this);
            btnRunPipeline.setText("Run Pipeline");
            btnRunPipeline.setVisibility(View.GONE);
            linearLayout.addView(btnRunPipeline);
            btnRunPipeline.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    GUIConfiguration.setPipelineState(PipelineState.TO_BE_CONFIGURED);
                    Intent intent = new Intent(DownloadActivity.this, TerminalActivity.class);
                    intent.putExtra("FOLDER_PATH", folderPath.substring(0, folderPath.lastIndexOf(".")));
                    startActivity(intent);
                }
            });
        }

    }

    private void enableButtons() {
        btnDownload.setEnabled(true);
        btnDownloadEcoli.setEnabled(true);
    }

    private void disableButtons() {
        btnDownload.setEnabled(false);
        btnDownloadEcoli.setEnabled(false);
    }

    private void downloadDataSet(String url) {
        // TODO check wifi connectivity

        disableButtons();

        DownloadManager downloadManager = new DownloadManager(url, folderPath, statusTextView, progressBar,
                new DownloadListener() {
                    @Override
                    public void onComplete(@NonNull final EndCause cause, @Nullable final Exception realCause) {
                        enableButtons();
                    }
                });
        Uri treeUri = PreferenceUtil.getSharedPreferenceUri(R.string.sdcard_uri);
        downloadManager.download(DownloadActivity.this, treeUri);
    }

    private void downloadDatasetFTP(String url) {
        String[] urlData = url.split("/");
        Log.e(TAG, "URL=" + urlData[1]);
        new FTPDownloadTask().execute(urlData[0], urlData[1]);
    }

    private void extractZip(String filepath) {

        ZipManager zipManager = new ZipManager(DownloadActivity.this, new ZipListener() {
            @Override
            public void onStarted(@NonNull final long totalBytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnExtract.setEnabled(false);
                        progressBar.setMax(100);
                        statusTextView.setText("Unzip started");
                    }
                });
            }

            @Override
            public void onProgress(@NonNull final long bytesDone, @NonNull final long totalBytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ZipManager.showExtractPercentage) {
                            int perc = ZipManager.getZipPercentage(bytesDone, totalBytes);
                            progressBar.setProgress(perc);
                            statusTextView.setText("Unzipping: " + perc + "%");
                        } else {
                            statusTextView.setText("Unzipping...");
                        }
                    }
                });
            }

            @Override
            public void onComplete(@NonNull final boolean success, @Nullable final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnExtract.setEnabled(true);
                        if (success) {
                            statusTextView.setText("Unzip Successful");
                        } else {
                            statusTextView.setText("Unzip Error");
                        }
                    }
                });
            }
        });
        Uri treeUri = PreferenceUtil.getSharedPreferenceUri(R.string.sdcard_uri);
        zipManager.unzip(treeUri, filepath);
    }

    private void openFileManager(boolean dirOnly) {

        new ChooserDialog(DownloadActivity.this)
                .withFilter(dirOnly, false)
                // to handle the result(s)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        folderPath = path;
                        if (dirOnly) {
                            folderPathInput.setText(folderPath);
                            enableButtons();
                        } else {
                            filePathInput.setText(folderPath);
                            btnExtract.setEnabled(true);
                        }
                    }
                })
                .build()
                .show();
    }

}
