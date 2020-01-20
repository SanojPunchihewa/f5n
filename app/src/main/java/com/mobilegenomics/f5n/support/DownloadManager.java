package com.mobilegenomics.f5n.support;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend.Listener4SpeedModel;
import com.mobilegenomics.f5n.R;
import java.io.File;
import java.util.List;
import java.util.Map;

public class DownloadManager {

    private static final String TAG = DownloadManager.class.getSimpleName();

    private DownloadTask downloadTask;

    private String url;

    private String folderPath;

    private TextView statusTextView;

    private ProgressBar progressBar;

    private DownloadListener downloadListener;

    public DownloadManager(@NonNull final String url, @NonNull final String folderPath,
            @NonNull final TextView statusTextView,
            @NonNull final ProgressBar progressBar,
            @NonNull final DownloadListener downloadListener) {
        this.url = url;
        this.folderPath = folderPath;
        this.statusTextView = statusTextView;
        this.progressBar = progressBar;
        this.downloadListener = downloadListener;
    }

    public void download(Context context, Uri treeUri) {
        String nameOfFile = URLUtil.guessFileName(url, null,
                MimeTypeMap.getFileExtensionFromUrl(url));

        if ((PreferenceUtil.getSharedPreferenceUri(R.string.sdcard_uri) != null) && FileUtil
                .isFileInExternalSdCard(folderPath)) {
            DocumentFile file = FileUtil
                    .getDocumentFile(context, new File(folderPath + "/" + nameOfFile), false, true, treeUri);
            downloadTask = new DownloadTask.Builder(url, file.getUri()).build();
        } else {
            downloadTask = new DownloadTask.Builder(url, folderPath, nameOfFile).setConnectionCount(1)
                    .build();
        }

        initStatus(statusTextView, progressBar);

        downloadTask.enqueue(new DownloadListener4WithSpeed() {

            private long totalLength;

            private String readableTotalLength;

            @Override
            public void infoReady(@NonNull final DownloadTask task, @NonNull final BreakpointInfo info,
                    final boolean fromBreakpoint,
                    @NonNull final Listener4SpeedModel model) {
                totalLength = info.getTotalLength();
                readableTotalLength = Util.humanReadableBytes(totalLength, true);
                calcProgressToView(progressBar, info.getTotalOffset(), totalLength);
            }

            @Override
            public void progressBlock(@NonNull final DownloadTask task, final int blockIndex,
                    final long currentBlockOffset,
                    @NonNull final SpeedCalculator blockSpeed) {

            }

            @Override
            public void progress(@NonNull final DownloadTask task, final long currentOffset,
                    @NonNull final SpeedCalculator taskSpeed) {
                final String readableOffset = Util.humanReadableBytes(currentOffset, true);
                final String progressStatus = readableOffset + "/" + readableTotalLength;
                final String speed = taskSpeed.speed();
                final String progressStatusWithSpeed = progressStatus + "(" + speed + ")";

                statusTextView.setText(progressStatusWithSpeed);
                calcProgressToView(progressBar, currentOffset, totalLength);
            }

            @Override
            public void blockEnd(@NonNull final DownloadTask task, final int blockIndex, final BlockInfo info,
                    @NonNull final SpeedCalculator blockSpeed) {

            }

            @Override
            public void taskEnd(@NonNull final DownloadTask task, @NonNull final EndCause cause,
                    @Nullable final Exception realCause,
                    @NonNull final SpeedCalculator taskSpeed) {
                Log.d(TAG, "Download Ended");
                final String statusWithSpeed = cause.toString() + " " + taskSpeed.averageSpeed();
                statusTextView.setText(statusWithSpeed);
                if (realCause != null) {
                    Log.e(TAG, "Download Error : " + realCause);
                }
                downloadListener.onComplete(cause, realCause);
            }

            @Override
            public void taskStart(@NonNull final DownloadTask task) {
                statusTextView.setText("Download Started");
                Log.d(TAG, "Download Started");
            }

            @Override
            public void connectStart(@NonNull final DownloadTask task, final int blockIndex,
                    @NonNull final Map<String, List<String>> requestHeaderFields) {

            }

            @Override
            public void connectEnd(@NonNull final DownloadTask task, final int blockIndex, final int responseCode,
                    @NonNull final Map<String, List<String>> responseHeaderFields) {

            }
        });
    }

    private void initStatus(TextView statusTv, ProgressBar progressBar) {
        final StatusUtil.Status status = StatusUtil.getStatus(downloadTask);
        if (status == StatusUtil.Status.COMPLETED) {
            progressBar.setProgress(progressBar.getMax());
        }
        statusTv.setText(status.toString());
        final BreakpointInfo info = StatusUtil.getCurrentInfo(downloadTask);
        if (info != null) {
            Log.d(TAG, "init status with: " + info.toString());
            calcProgressToView(progressBar, info.getTotalOffset(), info.getTotalLength());
        }
    }

    private static void calcProgressToView(ProgressBar progressBar, long offset, long total) {
        final float percent = (float) offset / total;
        progressBar.setProgress((int) (percent * progressBar.getMax()));
    }

}

