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

public class FragmentHelpStandalone extends Fragment {

    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_vertical, container, false);
        linearLayout = view.findViewById(R.id.vertical_linear_layout);
        TextView txt1 = new TextView(getContext());
        txt1.setText(
                "Use this mode to run any of the following steps in this device.\n"
                        + "Select the steps you need to perform on the Data set\n"
                        + "Then choose either GUI mode or TERMINAL mode to configure the selected steps\n\n"
                        + "To find out more about each step, checkout the respective Documentations\n\n"
                        + "IMPORTANT\n"
                        + "Depending on the Memory available you may need to limit the memory usage of steps\n"
                        + "Eg. samtools sort use 768MB of RAM by default, if you have less memory, change it a appropriate value");
        linearLayout.addView(txt1);

        ImageView img1 = new ImageView(getContext());
        img1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.help_steps));
        linearLayout.addView(img1);

        TextView txt2 = new TextView(getContext());
        txt2.setText(
                "\nGUI MODE\n"
                        + "All the parameters related to a tool will be shown in a Graphical manner\n\n"
                        + "Already checked are the required parameters for a given tool\n"
                        + "Other fields will have their default values unless the user makes any changes");
        linearLayout.addView(txt2);

        ImageView img2 = new ImageView(getContext());
        img2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.help_configure_step));
        linearLayout.addView(img2);

        TextView txt3 = new TextView(getContext());
        txt3.setText(
                "\nTERMINAL MODE\n"
                        + "If you want to manually type the command, you can use this mode\n");
        linearLayout.addView(txt3);

        ImageView img3 = new ImageView(getContext());
        img3.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.help_configure_step_terminal));
        linearLayout.addView(img3);

        return view;
    }
}
