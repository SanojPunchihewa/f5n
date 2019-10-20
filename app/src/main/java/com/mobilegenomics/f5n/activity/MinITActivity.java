package com.mobilegenomics.f5n.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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
        final Button btnSendResult = findViewById(R.id.btn_send_result);

        btnRquestJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (serverAddressInput.getText() != null && !TextUtils
                        .isEmpty(serverAddressInput.getText().toString().trim())) {
                    serverIP = serverAddressInput.getText().toString().trim();

                    ServerConnectionUtils.setServerAddress(serverIP);
                    ServerConnectionUtils.connectToServer(State.REQUEST, new ServerCallback() {
                        @Override
                        public void onJobReceivedSuccessfully(final WrapperObject job) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    GUIConfiguration.configureSteps(job.getSteps());
                                    // TODO Get the zip name from the WrapperObject
                                    // job.getZipFileName()
                                    zipFileName = "ecoli_2kb_region.zip";
                                    btnSendResult.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });

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
}
