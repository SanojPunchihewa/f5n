package com.mobilegenomics.f5n.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.AppMode;
import com.mobilegenomics.f5n.support.Decompress;
import com.obsez.android.lib.filechooser.ChooserDialog;
import java.io.File;

public class DownloadActivity extends AppCompatActivity {

    private static final String TAG = DownloadActivity.class.getSimpleName();

    private String folderPath;

    private static final String ecoliDataSetURL = "https://zanojmobiapps.com/_tmp/genome/ecoli/ecoli_2kb_region.zip";

    private long downloadID;

    LinearLayout linearLayout;

    EditText urlInputPath;

    EditText folderPathInput;

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

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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
                    extractZip(new File(folderPath));
                    if (GUIConfiguration.getAppMode() == AppMode.SLAVE) {
                        btnRunPipeline.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(DownloadActivity.this, "Please Select a Zip file", Toast.LENGTH_SHORT).show();
                }

            }
        });

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

        DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);

        String nameOfFile = URLUtil.guessFileName(url, null,
                MimeTypeMap.getFileExtensionFromUrl(url));

        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(nameOfFile);
            request.setDescription("Downloading");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(true);
            request.setDestinationUri(
                    Uri.parse("file://" + folderPath + "/" + nameOfFile));

            downloadID = downloadmanager.enqueue(request);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception: " + e);
            enableButtons();
        }
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(DownloadActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                enableButtons();
            }
        }
    };

    private void extractZip(File file) {
        Decompress decompress = new Decompress(DownloadActivity.this, file);
        decompress.unzip();
    }

    private void enableButtons() {
        btnDownload.setEnabled(true);
        btnDownloadEcoli.setEnabled(true);
    }

    private void disableButtons() {
        btnDownload.setEnabled(false);
        btnDownloadEcoli.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }
}
