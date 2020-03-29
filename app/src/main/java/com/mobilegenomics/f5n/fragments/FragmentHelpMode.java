package com.mobilegenomics.f5n.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.mobilegenomics.f5n.R;

public class FragmentHelpMode extends Fragment {

    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_vertical, container, false);
        linearLayout = view.findViewById(R.id.vertical_linear_layout);
        TextView textView = new TextView(getContext());
        textView.setText(
                "This app has 4 modes,\n\n"
                        + "DOWNLOAD DATASET\n"
                        + "Download and Extract a data set\n\n"
                        + "STANDALONE_METHYLATION\n"
                        + "Run a pipeline in a single(this) device\n\n"
                        + "CONNECT TO MINIT\n"
                        + "Use f5n server to get a Job and do it in this device\n\n"
                        + "RUN A DEMO\n"
                        + "Run the whole pipeline on Ecoli data set as a demo\n\n"
                        + "Swipe right to find more information about these modes"
        );
        linearLayout.addView(textView);
        return view;
    }
}
