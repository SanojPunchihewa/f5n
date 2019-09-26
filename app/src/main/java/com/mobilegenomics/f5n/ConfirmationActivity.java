package com.mobilegenomics.f5n;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ConfirmationActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        LinearLayout linearLayout = findViewById(R.id.vertical_linear_layout);

        String[] commands = GUIConfiguration.getSelectedCommandStrings();

        for (String command : commands) {
            TextView txtCommand = new TextView(this);
            txtCommand.setText(command);
            txtCommand.setPadding(10, 10, 10, 0);
            linearLayout.addView(txtCommand);
        }

        Button btnProceed = new Button(this);
        btnProceed.setText("Run the Pipeline");
        btnProceed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                GUIConfiguration.createPipeline();
                new RunPipeline().execute();
            }
        });
        linearLayout.addView(btnProceed);
    }

    public class RunPipeline extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressWindow();
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
            hideProgressWindow();
        }
    }

    private void showProgressWindow() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Running...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressWindow() {
        progressDialog.dismiss();
    }

}
