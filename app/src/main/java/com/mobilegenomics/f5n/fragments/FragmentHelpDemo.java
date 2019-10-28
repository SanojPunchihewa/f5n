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

public class FragmentHelpDemo extends Fragment {

    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_vertical, container, false);
        linearLayout = view.findViewById(R.id.vertical_linear_layout);
        TextView txt1 = new TextView(getContext());
        txt1.setText(
                "This mode will run all the 5 steps(minimap2 alignment, samtools sort, samtools index, f5c index, f5c call-methylation and f5c eventalign) on ecoli data set\n"
                        + "The App will automatically download and extract the data set and configure the pipeline\n"
                        + "All of the files will be in a folder called mobile-genomics in your main storage\n");
        linearLayout.addView(txt1);

        ImageView img1 = new ImageView(getContext());
        img1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.help_demo));
        linearLayout.addView(img1);

        return view;
    }
}
