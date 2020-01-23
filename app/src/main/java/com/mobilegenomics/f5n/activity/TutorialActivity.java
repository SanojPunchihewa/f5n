package com.mobilegenomics.f5n.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerTitleStrip;
import androidx.viewpager.widget.ViewPager;
import com.mobilegenomics.f5n.R;
import com.mobilegenomics.f5n.fragments.FragmentHelpAllowSDCard;
import com.mobilegenomics.f5n.fragments.FragmentHelpDemo;
import com.mobilegenomics.f5n.fragments.FragmentHelpDownload;
import com.mobilegenomics.f5n.fragments.FragmentHelpMinIt;
import com.mobilegenomics.f5n.fragments.FragmentHelpMode;
import com.mobilegenomics.f5n.fragments.FragmentHelpRunPipeline;
import com.mobilegenomics.f5n.fragments.FragmentHelpStandalone;
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
                    return new FragmentHelpAllowSDCard();
                case 1:
                    return new FragmentHelpMode();
                case 2:
                    return new FragmentHelpDownload();
                case 3:
                    return new FragmentHelpStandalone();
                case 4:
                    return new FragmentHelpMinIt();
                case 5:
                    return new FragmentHelpDemo();
                case 6:
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
                    return "SDcard Permission(Only for Android 5)";
                case 1:
                    return "App Modes";
                case 2:
                    return "Download Dataset";
                case 3:
                    return "Standalone Mode";
                case 4:
                    return "MinIt Mode";
                case 5:
                    return "Demo Mode";
                case 6:
                    return "Run Pipeline";
            }
            return null;
        }
    }

}
