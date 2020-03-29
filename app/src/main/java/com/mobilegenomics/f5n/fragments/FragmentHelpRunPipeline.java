package com.mobilegenomics.f5n.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.mobilegenomics.f5n.R;

public class FragmentHelpRunPipeline extends Fragment {

    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_vertical, container, false);
        linearLayout = view.findViewById(R.id.vertical_linear_layout);
        TextView txt1 = new TextView(getContext());
        txt1.setText(
                "To run the pipeline configured through the modes STANDALONE_METHYLATION, MINIT and DEMO you need to finally click RUN PIPELINE\n"
                        + "Once it's completed, If you want to save the logcat press WRITE LOG TO FILE. This will create a file called f5n.log in mobile-genomics folder in your main storage\n");
        linearLayout.addView(txt1);

        ImageView img1 = new ImageView(getContext());
        img1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.help_run_pipeline));
        linearLayout.addView(img1);

        return view;
    }
}
