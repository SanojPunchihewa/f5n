package com.mobilegenomics.f5n.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.mobilegenomics.f5n.GUIConfiguration;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.core.AppMode;
import com.mobilegenomics.f5n.core.PipelineType;
import com.mobilegenomics.f5n.support.PreferenceUtil;

public class ChoosePipelineActivity extends AppCompatActivity {

    private Button btnPipelineTypeMethylation;

    private Button btnPipelineTypeVariant;

    private Button btnPipelineTypeArtic;

    private Button btnPipelineTypeConsensus;

    private Button btnPipelineTypeSingleTool;

    private int pipelineType;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pipeline);

        btnPipelineTypeMethylation = findViewById(R.id.btn_pipeline_type_methylation);
        btnPipelineTypeVariant = findViewById(R.id.btn_pipeline_type_variant);
        btnPipelineTypeArtic = findViewById(R.id.btn_pipeline_type_artic);
        btnPipelineTypeConsensus = findViewById(R.id.btn_pipeline_type_consensus);
        btnPipelineTypeSingleTool = findViewById(R.id.btn_pipeline_type_single_tool);

        pipelineType = PreferenceUtil.getSharedPreferenceInt(R.string.key_pipeline_type_preference);

        btnPipelineTypeMethylation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openPipelineActivity(PipelineType.PIPELINE_METHYLATION);
            }
        });

        btnPipelineTypeVariant.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openPipelineActivity(PipelineType.PIPELINE_VARIANT);
            }
        });

        btnPipelineTypeArtic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openPipelineActivity(PipelineType.PIPELINE_ARTIC);
            }
        });

        btnPipelineTypeConsensus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openPipelineActivity(PipelineType.PIPELINE_CONSENSUS);
            }
        });

        btnPipelineTypeSingleTool.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openPipelineActivity(PipelineType.SINGLE_TOOL);
            }
        });

    }

    private void openPipelineActivity(PipelineType type) {

        if (type == PipelineType.PIPELINE_METHYLATION) {
            if (pipelineType == PipelineType.PIPELINE_METHYLATION.ordinal()) {
                if (GUIConfiguration.getAppMode() == AppMode.DEMO) {
                    startActivity(new Intent(ChoosePipelineActivity.this, DemoActivity.class));
                } else {
                    startActivity(new Intent(ChoosePipelineActivity.this, PipelineActivity.class));
                }
            } else {
                showSettingsDialog("METHYLATION");
            }
        } else if (type == PipelineType.PIPELINE_VARIANT) {
            if (pipelineType == PipelineType.PIPELINE_VARIANT.ordinal()) {
                if (GUIConfiguration.getAppMode() == AppMode.DEMO) {
                    startActivity(new Intent(ChoosePipelineActivity.this, DemoActivity.class));
                } else {
                    startActivity(new Intent(ChoosePipelineActivity.this, PipelineActivity.class));
                }
            } else {
                showSettingsDialog("VARIANT");
            }
        } else if (type == PipelineType.PIPELINE_ARTIC) {
            if (pipelineType == PipelineType.PIPELINE_ARTIC.ordinal()) {
                if (GUIConfiguration.getAppMode() == AppMode.DEMO) {
                    Toast.makeText(ChoosePipelineActivity.this, "Artic Pipeline Demo is under development",
                            Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(ChoosePipelineActivity.this, ArticPipelineActivity.class));
                }
            } else {
                showSettingsDialog("ARTIC");
            }
        } else if (type == PipelineType.PIPELINE_CONSENSUS) {
            if (pipelineType == PipelineType.PIPELINE_CONSENSUS.ordinal()) {
                if (GUIConfiguration.getAppMode() == AppMode.DEMO) {
                    Toast.makeText(ChoosePipelineActivity.this, "Consensus Pipeline Demo is under development",
                            Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(ChoosePipelineActivity.this, ArticPipelineActivity.class));
                }
            } else {
                showSettingsDialog("CONSENSUS");
            }
        } else if (type == PipelineType.SINGLE_TOOL) {
            if (pipelineType == PipelineType.SINGLE_TOOL.ordinal()) {
                if (GUIConfiguration.getAppMode() == AppMode.DEMO) {
                    Toast.makeText(ChoosePipelineActivity.this, "Single Tool Demo is under development",
                            Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(ChoosePipelineActivity.this, PipelineActivity.class));
                }
            } else {
                showSettingsDialog("SINGLE_TOOL");
            }
        }
    }

    private void showSettingsDialog(String type) {

        String message = "To run " + type + " pipeline, Please go to settings and change the pipeline type";

        new AlertDialog.Builder(ChoosePipelineActivity.this)
                .setTitle("Change Pipeline Type")
                .setMessage(message)
                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ChoosePipelineActivity.this, SettingsActivity.class);
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
