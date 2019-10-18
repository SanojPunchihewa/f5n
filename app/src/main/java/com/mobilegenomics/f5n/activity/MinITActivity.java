package com.mobilegenomics.f5n.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.dto.State;
import com.mobilegenomics.f5n.support.ServerConnectionUtils;

public class MinITActivity extends AppCompatActivity {

    private static TextView connectionLogText;

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
                ServerConnectionUtils.setServerAddress(serverAddressInput.getText().toString());
                ServerConnectionUtils.connectToServer(State.REQUEST);
                btnSendResult.setVisibility(View.VISIBLE);
            }
        });

        btnSendResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerConnectionUtils.connectToServer(State.COMPLETED);
            }
        });
    }
}
