package com.mobilegenomics.f5n.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.support.ServerConnectionUtils;

public class MinITActivity extends AppCompatActivity {

    private static TextView connectionLogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minit);

        final EditText serverAddressInput = findViewById(R.id.input_server_address);
        connectionLogText = findViewById(R.id.text_conn_log);
        final Button btnConnectServer = findViewById(R.id.btn_connect_server);
        final Button btnProcessJob = findViewById(R.id.btn_process);

        btnConnectServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerConnectionUtils.setServerAddress(serverAddressInput.getText().toString());
                ServerConnectionUtils.connectToServer();
                btnProcessJob.setVisibility(View.VISIBLE);
            }
        });

        btnProcessJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(MinITActivity.this);
                ServerConnectionUtils.sendResult();
            }
        });
    }

    public static void logHandler(Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringBuilder newLogMessage = ServerConnectionUtils.getLogMessage();
                if (newLogMessage != null && newLogMessage.toString().trim().length() != 0)
                    connectionLogText.setText(newLogMessage);
            }
        });
    }
}
