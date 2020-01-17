package com.mobilegenomics.f5n.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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
import com.mobilegenomics.f5n.core.AppMode;
import com.mobilegenomics.f5n.support.DownloadListener;
import com.mobilegenomics.f5n.support.DownloadManager;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import com.mobilegenomics.f5n.support.ZipListener;
import com.mobilegenomics.f5n.support.ZipManager;
import com.obsez.android.lib.filechooser.ChooserDialog;
import java.io.File;

public class DownloadActivity extends AppCompatActivity {

    private static final String TAG = DownloadActivity.class.getSimpleName();

    private String folderPath;

    private static final String ecoliDataSetURL = "https://zanojmobiapps.com/_tmp/genome/ecoli/ecoli-data-set.zip";

    LinearLayout linearLayout;

    EditText urlInputPath;

    EditText folderPathInput;

    TextView statusTextView;

    ProgressBar progressBar;

    Button btnDownload;

    Button btnDownloadEcoli;

    EditText filePathInput;

    Button btnSelectFilePath;

    Button btnExtract;

    Button btnRunPipeline;

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
            if (path != null && !TextUtils.isEmpty(path)) {
                urlInputPath.setText(path);
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
                    downloadDataSet(urlInputPath.getText().toString().trim());
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
                    Intent intent = new Intent(DownloadActivity.this, TerminalActivity.class);
                    intent.putExtra("FOLDER_PATH", folderPath.substring(0, folderPath.lastIndexOf(".")));
                    startActivity(intent);
                }
            });
        }

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

    private void enableButtons() {
        btnDownload.setEnabled(true);
        btnDownloadEcoli.setEnabled(true);
    }

    private void disableButtons() {
        btnDownload.setEnabled(false);
        btnDownloadEcoli.setEnabled(false);
    }
}
