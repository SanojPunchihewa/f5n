package com.mobilegenomics.f5n.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.activity.MainActivity;
import com.mobilegenomics.f5n.support.FileUtil;
import com.mobilegenomics.f5n.support.PreferenceUtil;

public class FragmentHelpAllowSDCard extends Fragment {

    private LinearLayout linearLayout;

    private static final int REQUEST_CODE_DOCUMENT_TREE = 148;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_vertical, container, false);
        linearLayout = view.findViewById(R.id.vertical_linear_layout);
        TextView txtInfo = new TextView(getContext());
        txtInfo.setText(
                "Inorder to write to SdCard you must grant write access to f5n.\n\n"
                        + "Press Grant and use the Android System dialog to select the root of the storage"
        );
        linearLayout.addView(txtInfo);
        Button btnGrant = new Button(getContext());
        btnGrant.setEnabled(false);
        if (FileUtil.isExternalSDCardAvailable(getContext())) {
            if (PreferenceUtil.getSharedPreferenceUri(R.string.sdcard_uri) != null) {
                btnGrant.setText("SD Card Write Permission Already granted");
            } else {
                btnGrant.setEnabled(true);
                btnGrant.setText("Grant SD Card Write Permission");
                btnGrant.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intent, REQUEST_CODE_DOCUMENT_TREE);
                    }
                });
            }
        } else {
            btnGrant.setText("No SD Card Found");
        }
        linearLayout.addView(btnGrant);
        TextView txtToolWrite = new TextView(getContext());
        txtToolWrite.setText(
                "Depending on your device f5n may not have the permission to write to the SD Card\n\n"
        );
        linearLayout.addView(txtToolWrite);

        TextView txtSystemSettings = new TextView(getContext());
        txtSystemSettings.setText(
                "Due to the limitations in background processes, the app needs to run in foreground which will keep the display on.\n\n"
                        + "To reduce the power consumption, you can reduce the screen brightness.\nIf you grant permission, F5N will automatically "
                        + "dim the display while a pipeline is running and restore to the default value after the execution"
        );
        linearLayout.addView(txtSystemSettings);

        Button btnGrantWriteSetting = new Button(getContext());
        btnGrantWriteSetting.setEnabled(false);

        Handler handler = new Handler();

        Runnable checkSettings = new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return;
                }
                boolean settingsCanWrite = Settings.System.canWrite(getContext());
                if (settingsCanWrite) {
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }
                handler.postDelayed(this, 1000);
            }
        };

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean settingsCanWrite = Settings.System.canWrite(getContext());

            if (settingsCanWrite) {
                btnGrantWriteSetting.setText("Write System Settings Permission Already granted");
            } else {
                btnGrantWriteSetting.setEnabled(true);
                btnGrantWriteSetting.setText("Grant System Settings Write Permission");
                btnGrantWriteSetting.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + getContext().getPackageName()));
                        startActivity(intent);
                        handler.postDelayed(checkSettings, 1000);
                    }
                });
            }

        } else {
            btnGrantWriteSetting.setText("This Permission is required only for devices above Android M");
        }
        linearLayout.addView(btnGrantWriteSetting);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DOCUMENT_TREE && resultCode == Activity.RESULT_OK && data != null) {
            // Get Uri from Storage Access Framework.
            Uri treeUri = data.getData();

            // Persist access permissions.
            getActivity().getContentResolver()
                    .takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            PreferenceUtil.setSharedPreferenceUri(R.string.sdcard_uri, treeUri);
        }
    }
}
