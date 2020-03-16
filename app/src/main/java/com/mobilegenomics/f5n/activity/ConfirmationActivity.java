package com.mobilegenomics.f5n.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.AppMode;
import com.mobilegenomics.f5n.core.NativeCommands;
import com.mobilegenomics.f5n.core.PipelineComponent;
import com.mobilegenomics.f5n.support.FileUtil;
import com.mobilegenomics.f5n.support.PipelineState;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import com.mobilegenomics.f5n.support.ScreenDimUtil;
import com.mobilegenomics.f5n.support.TimeFormat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class ConfirmationActivity extends AppCompatActivity {

    private static final String TAG = ConfirmationActivity.class.getSimpleName();

    private static final String FILE_CLOSE_TAG = "EOF";

    private String resultsSummary;

    private boolean logWrittenToFile = false;

    private String folderPath;

    TextView txtLogs;

    NestedScrollView scrollView;

    LinearLayout linearLayout;

    Button btnWriteLog;

    Button btnGoToStart;

    Chronometer txtTimer;

    Button btnProceed;

    Button btnSendResults;

    ProgressBar mProgressBar;

    MediaPlayer mp;

    Handler mHandler;

    String logPipePath;

    File logPipeFile;

    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;

    //Window object, that will store a reference to the current window
    private Window window;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        cResolver = getContentResolver();

        window = getWindow();

        try {
            Settings.System.putInt(cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            //Get the current system brightness
            int brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
            PreferenceUtil.setSharedPreferenceInt(R.string.id_screen_brightness, brightness);
        } catch (SettingNotFoundException e) {
            //Throw an error case it couldn't be retrieved
            Log.e(TAG, "Cannot access system brightness");
        }

        String dirPath = Environment.getExternalStorageDirectory() + "/" + "mobile-genomics";

        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        logPipeFile = new File(dir.getAbsolutePath() + "/" + FileUtil.TMP_LOG_FILE_NAME);
        logPipePath = dir.getAbsolutePath() + "/" + FileUtil.TMP_LOG_FILE_NAME;

        // delete the pipe file if exists and create a new file
        if (logPipeFile.exists()) {
            logPipeFile.delete();
        }

        try {
            logPipeFile.createNewFile();
        } catch (IOException e) {
            Toast.makeText(this, "Error creating log file, Please check permissions", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error : " + e);
        }

        mHandler = new Handler();

        linearLayout = findViewById(R.id.vertical_linear_layout);

        String[] commands = GUIConfiguration.getSelectedCommandStrings();

        for (String command : commands) {
            TextView txtCommand = new TextView(this);
            txtCommand.setText(command);
            txtCommand.setPadding(10, 10, 10, 0);
            linearLayout.addView(txtCommand);
        }

        if (getIntent().getExtras() != null) {
            folderPath = getIntent().getExtras().getString("FOLDER_PATH");
        }

        LinearLayout.LayoutParams layoutParams1 =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        btnProceed = new Button(this);
        layoutParams1.weight = 1f;
        btnProceed.setLayoutParams(layoutParams1);
        horizontalLayout.addView(btnProceed);

        LinearLayout.LayoutParams layoutParams2 =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);

        txtTimer = new Chronometer(this);
        txtTimer.setPadding(20, 0, 0, 0);
        layoutParams2.weight = 4f;
        txtTimer.setLayoutParams(layoutParams2);
        horizontalLayout.addView(txtTimer);

        linearLayout.addView(horizontalLayout);

        btnProceed.setText("Run the Pipeline");
        btnProceed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                ScreenDimUtil.changeBrightness(cResolver, window, 0);
                txtTimer.start();
                btnProceed.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);
                GUIConfiguration.createPipeline();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new FileReader(logPipePath));
                            while (true) {
                                String line = reader.readLine();
                                if (line == null) {
                                    //wait until there is more of the file for us to read
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (line.equals(FILE_CLOSE_TAG)) {
                                        break;
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtLogs.append(line + "\n");
                                            scrollView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    scrollView.fullScroll(View.FOCUS_DOWN);
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                            reader.close();
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "Pipe Not found: " + e);
                        } catch (IOException e) {
                            Log.e(TAG, "IO Exception: " + e);
                        } finally {
                            logPipeFile.delete();
                        }
                    }
                }).start();
                new RunPipeline().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        mProgressBar = new ProgressBar(this,
                null,
                android.R.attr.progressBarStyleHorizontal);
        mProgressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.BLACK));
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);
        linearLayout.addView(mProgressBar);

        View separator1 = new View(this);
        separator1.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                5
        ));
        separator1.setBackgroundColor(Color.parseColor("#000000"));
        linearLayout.addView(separator1);

        scrollView = new NestedScrollView(this);
        linearLayout.addView(scrollView);

        txtLogs = new TextView(this);
        scrollView.addView(txtLogs);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700);
        scrollView.setLayoutParams(params);
        View separator2 = new View(this);
        separator2.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                5
        ));
        separator2.setBackgroundColor(Color.parseColor("#000000"));
        linearLayout.addView(separator2);

        if (GUIConfiguration.getAppMode() == AppMode.STANDALONE) {
            TextView txtOuputFolderInfo = new TextView(this);
            txtOuputFolderInfo.setText(
                    "By default, output files are written to the mobile-genomics folder in your main storage. If you have updated the output paths please check the respective folders\n");
            linearLayout.addView(txtOuputFolderInfo);
        }

        TextView txtOuputFolderInfo = new TextView(this);
        txtOuputFolderInfo.setText(
                "Brightness will be reduced when running the pipeline to save power. Minimizing the app or turning off the display will abort the process");
        linearLayout.addView(txtOuputFolderInfo);

        mp = MediaPlayer.create(this, R.raw.alarm);

        btnWriteLog = new Button(this);
        btnWriteLog.setText("Write Log to File");
        btnWriteLog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                writeLogToFile();
            }
        });
        btnWriteLog.setVisibility(View.GONE);
        linearLayout.addView(btnWriteLog);

        if (GUIConfiguration.getAppMode() == AppMode.STANDALONE || GUIConfiguration.getAppMode() == AppMode.DEMO) {
            btnGoToStart = new Button(this);
            btnGoToStart.setText("Go to Start Screen");
            btnGoToStart.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Intent intent = new Intent(ConfirmationActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
            btnGoToStart.setVisibility(View.GONE);
            linearLayout.addView(btnGoToStart);
        }

        if (GUIConfiguration.getAppMode() == AppMode.SLAVE) {

            btnSendResults = new Button(this);
            btnSendResults.setText("Send Results");
            btnSendResults.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    mp.stop();
                    Intent intent = new Intent(ConfirmationActivity.this, MinITActivity.class);
                    intent.putExtra("PIPELINE_STATUS", resultsSummary);
                    intent.putExtra("FOLDER_PATH", folderPath);
                    startActivity(intent);
                }
            });
            btnSendResults.setVisibility(View.GONE);
            linearLayout.addView(btnSendResults);
        }
    }

    public class RunPipeline extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            NativeCommands.getNativeInstance().startPipeline(logPipePath);
            GUIConfiguration.setPipelineState(PipelineState.RUNNING);
        }

        @Override
        protected String doInBackground(final String... strings) {
            try {
                GUIConfiguration.runPipeline();
            } catch (Exception e) {
                Log.e("NATIVE-LIB", "Exception thrown by native code : " + e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            txtTimer.stop();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            ScreenDimUtil.changeBrightness(cResolver, window,
                    PreferenceUtil.getSharedPreferenceInt(R.string.id_screen_brightness));
            GUIConfiguration.setPipelineState(PipelineState.COMPLETED);
            NativeCommands.getNativeInstance().finishPipeline(logPipePath);
            List<PipelineComponent> pipelineComponents = GUIConfiguration.getPipeline();
            for (PipelineComponent pipelineComponent : pipelineComponents) {
                TextView txtRuntime = new TextView(ConfirmationActivity.this);
                txtRuntime.setText(
                        pipelineComponent.getPipelineStep().getCommand() + " took " + pipelineComponent.getRuntime());
                linearLayout.addView(txtRuntime);
            }
            btnWriteLog.setVisibility(View.VISIBLE);
            if (GUIConfiguration.getAppMode() == AppMode.STANDALONE
                    || GUIConfiguration.getAppMode() == AppMode.DEMO) {
                btnGoToStart.setVisibility(View.VISIBLE);
            }
            btnProceed.setEnabled(true);
            mProgressBar.setVisibility(View.GONE);
            PreferenceUtil
                    .setSharedPreferenceInt(R.string.id_app_mode, GUIConfiguration.getPipelineState().ordinal());

            if (GUIConfiguration.getAppMode() == AppMode.SLAVE) {
                mp.start();
                mp.setLooping(true);
                GUIConfiguration.setPipelineState(PipelineState.TO_BE_UPLOAD);
                PreferenceUtil
                        .setSharedPreferenceInt(R.string.id_app_mode, GUIConfiguration.getPipelineState().ordinal());
                PreferenceUtil
                        .setSharedPreferenceString(R.string.id_results_summary, resultsSummary);
                btnSendResults.setVisibility(View.VISIBLE);
            }
        }
    }

    private void writeLogToFile() {

        StringBuilder stringBuilder = new StringBuilder();

        String header = "----------- Log for app session " + TimeFormat.millisToDateTime(System.currentTimeMillis())
                + " -----------\n";
        stringBuilder.append(header);

        List<PipelineComponent> pipelineComponents = GUIConfiguration.getPipeline();
        for (PipelineComponent pipelineComponent : pipelineComponents) {
            String command = "Command :\n" + pipelineComponent.getCommand() + "\n";
            String time = "Time taken :\n" + pipelineComponent.getPipelineStep().getCommand() + " took "
                    + pipelineComponent
                    .getRuntime() + "\n";
            stringBuilder.append(command);
            stringBuilder.append(time);
            stringBuilder.append("\n");
        }

        String logcat = txtLogs.getText().toString();
        stringBuilder.append(logcat);

        String footer = "\n-------------------- End of Log --------------------\n\n";
        stringBuilder.append(footer);

        resultsSummary = stringBuilder.toString();

        try {
            String dirPath = Environment.getExternalStorageDirectory() + "/mobile-genomics";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File logFile = new File(dir.getAbsolutePath() + "/" + FileUtil.LOG_FILE_NAME);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(logFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(stringBuilder.toString());
            myOutWriter.flush();
            myOutWriter.close();
            fOut.close();
            Toast.makeText(getApplicationContext(), "Finished writing to mobile-genomics in home", Toast.LENGTH_LONG)
                    .show();
            logWrittenToFile = true;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Write failure", Toast.LENGTH_SHORT).show(); //##6
            Log.e("TAG", e.toString());
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        PipelineState state = GUIConfiguration.getPipelineState();
        switch (state) {
            case CONFIGURED:
                ScreenDimUtil.changeBrightness(cResolver, window,
                        PreferenceUtil.getSharedPreferenceInt(R.string.id_screen_brightness));
                super.onBackPressed();
                break;
            case RUNNING:
                showStopPipelineDialog();
                break;
            case COMPLETED:
                if (!logWrittenToFile) {
                    showWriteToFileDialog();
                } else {
                    super.onBackPressed();
                }
                break;
        }
    }

    private void showWriteToFileDialog() {

        new AlertDialog.Builder(ConfirmationActivity.this)
                .setTitle("Save log")
                .setMessage("Are you sure you want to save log?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        writeLogToFile();
                        ConfirmationActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        ConfirmationActivity.super.onBackPressed();
                    }
                })
                .show();

    }

    private void showStopPipelineDialog() {

        new AlertDialog.Builder(ConfirmationActivity.this)
                .setTitle("Stop Running")
                .setMessage("Are you sure to stop the pipeline?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Kill the native process
                        ScreenDimUtil.changeBrightness(cResolver, window,
                                PreferenceUtil.getSharedPreferenceInt(R.string.id_screen_brightness));
                        ConfirmationActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
