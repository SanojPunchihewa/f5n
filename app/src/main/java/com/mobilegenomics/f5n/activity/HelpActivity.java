package com.mobilegenomics.f5n.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.f5n.R;

public class HelpActivity extends AppCompatActivity {

    LinearLayout linearLayout;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vertical);

        linearLayout = findViewById(R.id.vertical_linear_layout);

        TextView txtIntro = new TextView(this);
        txtIntro.setText(
                getResources().getString(R.string.app_info));
        linearLayout.addView(txtIntro);

        Button btnTutorial = new Button(this);
        btnTutorial.setText("View Tutorial");
        btnTutorial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(HelpActivity.this, TutorialActivity.class));
            }
        });
        linearLayout.addView(btnTutorial);

        TextView txtContribute = new TextView(this);
        txtContribute.setText(
                "Please contribute to our work by testing,debugging, developing and submitting issues on our product");
        linearLayout.addView(txtContribute);

        Button btnReport = new Button(this);
        btnReport.setText("Report Bugs");
        btnReport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Uri web = Uri
                        .parse("https://github.com/SanojPunchihewa/f5n/issues/new?assignees=&labels=bug&template=bug_report.md&title=");
                Intent website = new Intent(Intent.ACTION_VIEW, web);
                startActivity(website);
            }
        });
        linearLayout.addView(btnReport);

        Button btnRequestFeature = new Button(this);
        btnRequestFeature.setText("Request Feature");
        btnRequestFeature.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Uri web = Uri
                        .parse("https://github.com/SanojPunchihewa/f5n/issues/new?assignees=&labels=Feature+Request&template=feature_request.md&title=");
                Intent website = new Intent(Intent.ACTION_VIEW, web);
                startActivity(website);
            }
        });
        linearLayout.addView(btnRequestFeature);
    }
}
