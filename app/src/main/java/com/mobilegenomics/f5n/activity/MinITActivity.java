package com.mobilegenomics.f5n.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.dto.State;
import com.mobilegenomics.f5n.dto.WrapperObject;
import com.mobilegenomics.f5n.support.ServerCallback;
import com.mobilegenomics.f5n.support.ServerConnectionUtils;

public class MinITActivity extends AppCompatActivity {

    private static TextView connectionLogText;

    private String serverIP;

    private String zipFileName;

    private Button btnSendResult;

    private boolean ranPipeline = false;

    public static void logHandler(Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringBuilder newLogMessage = ServerConnectionUtils.getLogMessage();
                if (newLogMessage != null && newLogMessage.toString().trim().length() != 0) {
                    connectionLogText.setText(newLogMessage);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minit);

        final EditText serverAddressInput = findViewById(R.id.input_server_address);
        connectionLogText = findViewById(R.id.text_conn_log);
        final Button btnRquestJob = findViewById(R.id.btn_request_job);
        btnSendResult = findViewById(R.id.btn_send_result);

        if (getIntent().getExtras() != null) {
            String path = getIntent().getExtras().getString("PIPELINE_STATUS");
            if (path != null && !TextUtils.isEmpty(path)) {
                ranPipeline = true;
                btnRquestJob.setText("Send Results");
            }
        }

        btnRquestJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (serverAddressInput.getText() != null && !TextUtils
                        .isEmpty(serverAddressInput.getText().toString().trim())) {
                    serverIP = serverAddressInput.getText().toString().trim();

                    ServerConnectionUtils.setServerAddress(serverIP);
                    if (ranPipeline) {
                        sendJobResults();
                    } else {
                        requestJob();
                    }

                } else {
                    Toast.makeText(MinITActivity.this, "Please input a server IP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSendResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MinITActivity.this, DownloadActivity.class);
                // TODO Fix the following
                // Protocol, file server IP and Port
                intent.putExtra("DATA_SET_URL", "http://" + serverIP + ":8000/" + zipFileName);
                startActivity(intent);
            }
        });
    }

    private void requestJob() {
        ServerConnectionUtils.connectToServer(State.REQUEST, new ServerCallback() {
            @Override
            public void onSuccess(final WrapperObject job) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GUIConfiguration.configureSteps(job.getSteps());
                        zipFileName = job.getPrefix();
                        btnSendResult.setVisibility(View.VISIBLE);
                        Log.d("TAG", "Prefix = " + job.getPrefix());
                        Log.d("TAG", "Dir Path = " + job.getPathToDataDir());
                    }
                });
            }

            @Override
            public void onError(final WrapperObject job) {

            }
        });
    }

    private void sendJobResults() {

        ServerConnectionUtils.connectToServer(State.COMPLETED, new ServerCallback() {
            @Override
            public void onSuccess(final WrapperObject job) {
                Toast.makeText(MinITActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(final WrapperObject job) {

            }
        });
    }
}
