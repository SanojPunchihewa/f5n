package com.mobilegenomics.f5n;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DownloadActivity extends AppCompatActivity {

    // TODO Let user to set this path
    private String folderPath = "/mnt/sdcard/mobile-genomics";

    private static final String ecoliDataSetURL = "https://zanojmobiapps.com/_tmp/genome/ecoli/ecoli_2kb_region.zip";

    LinearLayout linearLayout;

    EditText urlInputPath;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        linearLayout = findViewById(R.id.vertical_linear_layout);

        urlInputPath = new EditText(this);
        urlInputPath.setHint("Url of the data set");
        linearLayout.addView(urlInputPath);

        Button btnDownload = new Button(this);
        btnDownload.setText("Download Data");
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

        Button btnDownloadEcoli = new Button(this);
        btnDownloadEcoli.setText("Download Sample ecoli DataSet");
        linearLayout.addView(btnDownloadEcoli);

        btnDownloadEcoli.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                downloadDataSet(ecoliDataSetURL);
            }
        });

    }

    private void downloadDataSet(String url) {
        // TODO check wifi connectivity

        DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);

        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("Ecoli DataSet");
            request.setDescription("Downloading");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(true);
            request.setDestinationUri(Uri.parse("file://" + folderPath + "/ecoli_2kb_region.zip"));

            downloadmanager.enqueue(request);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            Log.e("DOWNLOAD-ACTIVITY", "Exception: " + e);
        }
    }
}
