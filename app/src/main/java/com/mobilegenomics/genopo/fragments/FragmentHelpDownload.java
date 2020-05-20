package com.mobilegenomics.genopo.fragments;

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
import com.mobilegenomics.genopo.R;

public class FragmentHelpDownload extends Fragment {

    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_vertical, container, false);
        linearLayout = view.findViewById(R.id.vertical_linear_layout);
        TextView textView = new TextView(getContext());
        textView.setText(
                "You can use this mode to download a data set (zip file) and extract it to a desired location\n"
                        + "You can download a sample data set by pressing DOWNLOAD SAMPLE ECOLI DATSET\n\n"
                        + "Once you have downloaded a zip file, select that file and press EXTRACT to unzip it");
        linearLayout.addView(textView);

        ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.help_download));
        linearLayout.addView(imageView);

        return view;
    }
}
