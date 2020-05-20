package com.mobilegenomics.genopo.fragments;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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

public class FragmentHelpMinIt extends Fragment {

    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_vertical, container, false);
        linearLayout = view.findViewById(R.id.vertical_linear_layout);
        TextView txt1 = new TextView(getContext());
        txt1.setText(
                "If you use f5n server to run a cluster, choose this mode to run a Job on this device.\n"
                        + "To get a Job, input the IP address of the server and click CONNECT\n");
        linearLayout.addView(txt1);

        ImageView img1 = new ImageView(getContext());
        img1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.help_minit_connect));
        linearLayout.addView(img1);

        TextView txt2 = new TextView(getContext());
        txt2.setText(
                "Once you have successfully obtained a Job, Press PROCESS JOB to download the data set\n\n"
                        + "Enter the address of the file server and select a folder to download\n"
                        + "To extract the zip file, select that file and press EXTRACT\n");
        linearLayout.addView(txt2);

        ImageView img2 = new ImageView(getContext());
        img2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.help_minit_download));
        linearLayout.addView(img2);

        TextView txt3 = new TextView(getContext());
        txt3.setText("After the extraction is completed, press RUN PIPELINE to configure data set path\n"
                + "Use the SELECT FOLDER option to choose the extracted data set folder");
        linearLayout.addView(txt3);

        ImageView img3 = new ImageView(getContext());
        img3.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.help_minit_configure));
        linearLayout.addView(img3);

        TextView txt4 = new TextView(getContext());
        txt4.setMovementMethod(LinkMovementMethod.getInstance());
        txt4.setText(Html.fromHtml(
                "Download F5N Server from <a href=\"https://github.com/AnjanaSenanayake/f5n_server\">https://github.com/AnjanaSenanayake/f5n_server</a>"));
        linearLayout.addView(txt4);

        return view;
    }
}
