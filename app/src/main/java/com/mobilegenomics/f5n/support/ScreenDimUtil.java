package com.mobilegenomics.f5n.support;

import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import com.mobilegenomics.f5n.Application;

public class ScreenDimUtil {

    public static void changeBrightness(ContentResolver cResolver, Window window, int value) {
        // Check whether has the write settings permission or not.
        boolean settingsCanWrite = true;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            settingsCanWrite = Settings.System.canWrite(Application.getAppContext());
        }

        if (settingsCanWrite) {
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
