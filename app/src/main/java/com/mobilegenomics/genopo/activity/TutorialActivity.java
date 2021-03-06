package com.mobilegenomics.genopo.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerTitleStrip;
import androidx.viewpager.widget.ViewPager;
import com.mobilegenomics.genopo.R;
import com.mobilegenomics.genopo.fragments.FragmentHelpDemo;
import com.mobilegenomics.genopo.fragments.FragmentHelpDownload;
import com.mobilegenomics.genopo.fragments.FragmentHelpMinIt;
import com.mobilegenomics.genopo.fragments.FragmentHelpMode;
import com.mobilegenomics.genopo.fragments.FragmentHelpRunPipeline;
import com.mobilegenomics.genopo.fragments.FragmentHelpStandalone;
import java.util.Locale;

public class TutorialActivity extends AppCompatActivity {

    SectionsPagerAdapter sectionsPagerAdapter;

    ViewPager viewPager;

    PagerTitleStrip pagerTitleStrip;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_tab);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        pagerTitleStrip = viewPager.findViewById(R.id.pager_title_strip);

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentHelpMode();
                case 1:
                    return new FragmentHelpDownload();
                case 2:
                    return new FragmentHelpStandalone();
                case 3:
                    return new FragmentHelpMinIt();
                case 4:
                    return new FragmentHelpDemo();
                case 5:
                    return new FragmentHelpRunPipeline();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "App Modes";
                case 1:
                    return "Download Dataset";
                case 2:
                    return "Standalone Mode";
                case 3:
                    return "MinIt Mode";
                case 4:
                    return "Demo Mode";
                case 5:
                    return "Run Pipeline";
            }
            return null;
        }
    }

}
