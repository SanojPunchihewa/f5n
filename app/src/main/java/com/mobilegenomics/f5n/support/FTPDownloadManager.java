package com.mobilegenomics.f5n.support;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.activity.MinITActivity;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class FTPDownloadManager {

    private static final String TAG = FTPDownloadManager.class.getSimpleName();

    private String url;

    private static String folderPath;

    private static DownloadListener downloadListener;

    public FTPDownloadManager(@NonNull final String url, @NonNull final String folderPath,
                              @NonNull final DownloadListener downloadListener) {
        this.url = url;
        if (folderPath.endsWith("/")) {
            FTPDownloadManager.folderPath = folderPath;
        } else {
            FTPDownloadManager.folderPath = folderPath + "/";
        }
        FTPDownloadManager.downloadListener = downloadListener;
    }

    public void download(MinITActivity minITActivity) {
        // TODO Add correct exceptions and status codes
        String[] urlData = url.split("/");
        new FTPDownloadTask(minITActivity).execute(urlData[0], urlData[1]);
    }

    static class FTPDownloadTask extends AsyncTask<String, Long, Boolean> {

        private long downloadStartTime;

        private long fileSize;

        private boolean status;

        private WeakReference<MinITActivity> activityReference;

        FTPDownloadTask(MinITActivity context) {
            // A weak reference to the activity
            activityReference = new WeakReference<>(context);
        }


        @Override
        protected Boolean doInBackground(String... urls) {
            FTPClient con;
            try {
                con = new FTPClient();
                con.setDefaultPort(8000);
                con.connect(urls[0]);

                con.setCopyStreamListener(new CopyStreamAdapter() {
                    @Override
                    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred,
                                                 long streamSize) {
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

                    OutputStream out = new FileOutputStream(new File(folderPath + urls[1]));
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
            long downloadTime = System.currentTimeMillis() - downloadStartTime;
            EndCause cause;
            // Get a reference to the activity if it is still there
            MinITActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            TextView statusTextView = activity.findViewById(R.id.txt_status);
            if (downloadSuccess) {
                String time = TimeFormat.millisToShortDHMS(downloadTime);
                statusTextView.setText(String.format("Download Completed in %s", time));
                cause = EndCause.COMPLETED;
            } else {
                statusTextView.setText("Download Error");
                cause = EndCause.ERROR;
            }
            downloadListener.onComplete(cause, null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "Download Started");
            downloadStartTime = System.currentTimeMillis();

            // Get a reference to the activity if it is still there
            MinITActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            ProgressBar progressBar = activity.findViewById(R.id.progress_upload_status);
            TextView statusTextView = activity.findViewById(R.id.txt_status);
            statusTextView.setText("Download Started");
            progressBar.setMax(100);
        }

        @Override
        protected void onProgressUpdate(final Long... values) {
            super.onProgressUpdate(values);
            MinITActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            TextView statusTextView = activity.findViewById(R.id.txt_status);
            ProgressBar progressBar = activity.findViewById(R.id.progress_upload_status);

            String total = Util.humanReadableBytes(fileSize, true);
            String downloaded = Util.humanReadableBytes(values[0], true);
            statusTextView.setText(String.format("Downloading: %s/%s", downloaded, total));
            float percent = (float) values[0] / fileSize;
            progressBar.setProgress((int) percent * progressBar.getMax());
        }
    }
}
