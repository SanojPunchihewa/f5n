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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class FTPManager {

    private static final String TAG = FTPManager.class.getSimpleName();

    private static String folderPath;

    private static DownloadListener downloadListener;

    public FTPManager() {
    }

    public void download(@NonNull final String url, @NonNull final String folderPath,
                         @NonNull MinITActivity minITActivity, @NonNull final DownloadListener downloadListener) {
        if (folderPath.endsWith("/")) {
            FTPManager.folderPath = folderPath;
        } else {
            FTPManager.folderPath = folderPath + "/";
        }
        FTPManager.downloadListener = downloadListener;
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
            TextView connLogTextView = activity.findViewById(R.id.text_conn_log);
            if (downloadSuccess) {
                String time = TimeFormat.millisToShortDHMS(downloadTime);
                statusTextView.setText(String.format("Download Completed in %s", time));
                connLogTextView.append(String.format("Download Completed in %s\n", time));
                cause = EndCause.COMPLETED;
            } else {
                statusTextView.setText("Download Error");
                connLogTextView.setText("Download Error");
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
            TextView connLogTextView = activity.findViewById(R.id.text_conn_log);
            statusTextView.setText("Download Started");
            connLogTextView.append("Download Started\n");
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

    public void upload(@NonNull String serverIP, @NonNull String filePath, @NonNull MinITActivity minITActivity, @NonNull DownloadListener uploadListener) {
        FTPManager.downloadListener = uploadListener;
        new FTPUploadTask(minITActivity).execute(serverIP, filePath);
    }

    static class FTPUploadTask extends AsyncTask<String, Long, Boolean> {

        boolean status;

        long fileSize;

        long uploadStartTime;

        private WeakReference<MinITActivity> activityReference;

        FTPUploadTask(MinITActivity context) {
            // A weak reference to the activity
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            FTPClient con;
            try {

                Log.d(TAG, "Address = " + urls[0]);
                Log.d(TAG, "File path = " + urls[1]);

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
            MinITActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            TextView statusTextView = activity.findViewById(R.id.txt_status);
            statusTextView.setText("Upload cancelled");
        }

        @Override
        protected void onPostExecute(final Boolean uploadSuccess) {
            super.onPostExecute(uploadSuccess);
            Log.i(TAG, "Upload Finished");
            long uploadTime = System.currentTimeMillis() - uploadStartTime;
            EndCause cause;
            MinITActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            TextView statusTextView = activity.findViewById(R.id.txt_status);
            TextView connLogTextView = activity.findViewById(R.id.text_conn_log);
            if (uploadSuccess) {
                String time = TimeFormat.millisToShortDHMS(uploadTime);
                cause = EndCause.COMPLETED;
                statusTextView.setText(String.format("Upload Completed in %s", time));
                connLogTextView.append(String.format("Upload Completed in %s\n\n", time));
            } else {
                cause = EndCause.ERROR;
                statusTextView.setText("Upload Error");
                connLogTextView.append("Upload Error");
            }
            downloadListener.onComplete(cause, null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uploadStartTime = System.currentTimeMillis();
            MinITActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            TextView statusTextView = activity.findViewById(R.id.txt_status);
            TextView connLogTextView = activity.findViewById(R.id.text_conn_log);
            statusTextView.setText("Upload started");
            connLogTextView.append("Upload started\n");
        }

        @Override
        protected void onProgressUpdate(final Long... values) {
            super.onProgressUpdate(values);
            String total = Util.humanReadableBytes(fileSize, true);
            String downloaded = Util.humanReadableBytes(values[0], true);
            MinITActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            TextView statusTextView = activity.findViewById(R.id.txt_status);
            ProgressBar progressBar = activity.findViewById(R.id.progress_upload_status);
            statusTextView.setText(String.format("Uploading: %s/%s", downloaded, total));
            float percent = (float) values[0] / fileSize;
            progressBar.setProgress((int) percent * progressBar.getMax());
        }
    }
}
