package com.mobilegenomics.f5n.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.AppMode;
import com.mobilegenomics.f5n.core.PipelineType;
import com.mobilegenomics.f5n.support.PermissionResultCallback;
import com.mobilegenomics.f5n.support.PermissionUtils;
import com.mobilegenomics.f5n.support.PreferenceUtil;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, PermissionResultCallback {

    PermissionUtils permissionUtils;                    // An instance of the permissionUtils

    ArrayList<String> permissions = new ArrayList<>();

    boolean firstOpen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("F5N", MODE_PRIVATE);
        firstOpen = preferences.getBoolean("FIRST_OPEN", true);

        // Setup the permissions
        permissionUtils = new PermissionUtils(MainActivity.this);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (firstOpen) {

            String logFileDirectory = Environment.getExternalStorageDirectory() + "/"
                    + "mobile-genomics/";
            PreferenceUtil.setSharedPreferenceString(R.string.key_log_file_preference, logFileDirectory);

            // Set Pipeline type to Methylation by default
            PreferenceUtil.setSharedPreferenceInt(R.string.key_pipeline_type_preference,
                    PipelineType.PIPELINE_METHYLATION.ordinal());

            showAlert();
        } else {
            int tempPipelineType = PreferenceUtil.getSharedPreferenceInt(R.string.key_pipeline_type_temp_preference);
            PreferenceUtil.setSharedPreferenceInt(R.string.key_pipeline_type_preference,
                    tempPipelineType);
        }

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            TextView txtVersionName = findViewById(R.id.txt_app_version);
            txtVersionName.setText("App Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void showAlert() {
        new AlertDialog.Builder(this)
                .setTitle("F5N")
                .setMessage(getResources().getString(R.string.app_info))
                .setPositiveButton("I Understood", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences Preferences = getApplicationContext()
                                .getSharedPreferences("F5N", MODE_PRIVATE);
                        SharedPreferences.Editor editor = Preferences.edit();
                        editor.putBoolean("FIRST_OPEN", false);
                        editor.apply();
                        dialog.dismiss();
                        permissionUtils.check_permission(permissions,
                                "The app needs storage permission for reading and writing pipeline data",
                                1);
                    }
                })
                .setCancelable(false)
                .show();
    }

    public void downloadDataSet(View view) {
        GUIConfiguration.setAppMode(AppMode.DOWNLOAD_DATA);
        startActivity(new Intent(MainActivity.this, DownloadActivity.class));
    }

    public void startStandaloneMode(View view) {
        GUIConfiguration.setAppMode(AppMode.STANDALONE);
        startActivity(new Intent(MainActivity.this, ChoosePipelineActivity.class));
    }

    public void startMinITMode(View view) {
        if (PreferenceUtil.getSharedPreferenceInt(R.string.key_pipeline_type_preference)
                == PipelineType.PIPELINE_METHYLATION.ordinal()) {
            GUIConfiguration.setAppMode(AppMode.SLAVE);
            startActivity(new Intent(MainActivity.this, MinITActivity.class));
        } else {
            showSettingsDialog();
        }
    }

    public void startDemoMode(View view) {
        GUIConfiguration.setAppMode(AppMode.DEMO);
        startActivity(new Intent(MainActivity.this, ChoosePipelineActivity.class));
    }

    /////////////////////////////
    // Permission functions
    /////////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        // redirects to utils
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void NeverAskAgain(int request_code) {
        Log.i("PERMISSION", "NEVER ASK AGAIN");
        permissionUtils.check_permission(permissions,
                "The app needs storage permission for reading and writing pipeline data", 1);
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY", "GRANTED");
        permissionUtils.check_permission(permissions,
                "The app needs storage permission for reading and writing pipeline data", 1);
    }

    @Override
    public void PermissionDenied(int request_code) {
        Log.i("PERMISSION", "DENIED");
        permissionUtils.check_permission(permissions,
                "The app needs storage permission for reading and writing pipeline data", 1);
    }

    // Callback functions
    @Override
    public void PermissionGranted(int request_code) {
        Log.i("PERMISSION", "GRANTED");
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {

        String message = "Please go to settings and change the pipeline type to METHYLATION to connect to F5N Server";

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Change Pipeline Type")
                .setMessage(message)
                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}
