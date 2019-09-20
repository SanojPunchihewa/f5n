package com.mobilegenomics.f5n;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class StepActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Step step = GUIConfiguration.getNextStep();
        ArrayList<Argument> arguments = step.getArguments();

    }
}
