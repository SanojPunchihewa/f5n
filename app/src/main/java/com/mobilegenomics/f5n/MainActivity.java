package com.mobilegenomics.f5n;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, PermissionResultCallback {

    PermissionUtils permissionUtils;                    // An instance of the permissionUtils

    ArrayList<String> permissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the permissions
        permissionUtils = new PermissionUtils(MainActivity.this);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            TextView txtVersionName = findViewById(R.id.txt_app_version);
            txtVersionName.setText("App Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void downloadDataSet(View view) {
        startActivity(new Intent(MainActivity.this, DownloadActivity.class));
    }

    public void startStandaloneMode(View view) {
        startActivity(new Intent(MainActivity.this, PipelineActivity.class));
    }

    /////////////////////////////
    // Permission functions
    /////////////////////////////

    @Override
    public void onStart() {
        permissionUtils.check_permission(permissions,
                "The app needs storage permission for reading images and camera permission to take photos", 1);
        super.onStart();
    }

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
                "The app needs storage permission for reading images and camera permission to take photos", 1);
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY", "GRANTED");
        permissionUtils.check_permission(permissions,
                "The app needs storage permission for reading images and camera permission to take photos", 1);
    }

    @Override
    public void PermissionDenied(int request_code) {
        Log.i("PERMISSION", "DENIED");
        permissionUtils.check_permission(permissions,
                "The app needs storage permission for reading images and camera permission to take photos", 1);
    }

    // Callback functions
    @Override
    public void PermissionGranted(int request_code) {
        Log.i("PERMISSION", "GRANTED");
    }
}
