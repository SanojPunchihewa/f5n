package com.mobilegenomics.f5n;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DownloadActivity extends AppCompatActivity {

    // TODO Let user to set this path
    private String folderPath = "/mnt/sdcard/mobile-genomics";

    LinearLayout linearLayout;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        linearLayout = findViewById(R.id.vertical_linear_layout);

        EditText urlInputPath = new EditText(this);
        urlInputPath.setHint("Data set Url");
        linearLayout.addView(urlInputPath);

        Button btnDownload = new Button(this);
        btnDownload.setText("Download Data");
        linearLayout.addView(btnDownload);

        btnDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                downloadDataSet();
            }
        });

    }


    private void downloadDataSet() {
        // TODO check wifi connectivity
        String url_path
                = "https://zanojmobiapps.com/_tmp/genome/ecoli/ecoli_2kb_region.zip";

        DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url_path);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("My File");
        request.setDescription("Downloading");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationUri(Uri.parse("file://" + folderPath + "/ecoli_2kb_region.zip"));

        downloadmanager.enqueue(request);

    }
}
