package com.mobilegenomics.genopo.support;

import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import com.mobilegenomics.genopo.Application;
import com.mobilegenomics.genopo.R;

public class ScreenDimUtil {

    public static void changeBrightness(ContentResolver cResolver, Window window, int value) {
        // Check whether has the write settings permission or not.
        boolean settingsCanWrite = true;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            settingsCanWrite = Settings.System.canWrite(Application.getAppContext());
        }

        if (settingsCanWrite && PreferenceUtil.getSharedPreferenceBool(R.string.id_dim_screen)) {
            Settings.System.putInt(cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            //Set the system brightness using the brightness variable value
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, value);
            //Get the current window attributes
            WindowManager.LayoutParams layoutpars = window.getAttributes();
            //Set the brightness of this window
            layoutpars.screenBrightness = value / (float) 255;
            //Apply attribute changes to this window
            window.setAttributes(layoutpars);
        }
    }

}
