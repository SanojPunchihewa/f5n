package com.mobilegenomics.f5n.support;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import com.mobilegenomics.f5n.Application;

public class PreferenceUtil {

    /**
     * Hide default constructor.
     */
    private PreferenceUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve the default shared preferences of the application.
     *
     * @return the default shared preferences.
     */
    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(Application.getAppContext());
    }

    /**
     * Retrieve an Uri shared preference.
     *
     * @param preferenceId the id of the shared preference.
     * @return the corresponding preference value.
     */
    public static Uri getSharedPreferenceUri(final int preferenceId) {
        String uriString = getSharedPreferences()
                .getString(Application.getAppContext().getString(preferenceId), null);

        if (uriString == null) {
            return null;
        } else {
            return Uri.parse(uriString);
        }
    }

    /**
     * Set a shared preference for an Uri.
     *
     * @param preferenceId the id of the shared preference.
     * @param uri          the target value of the preference.
     */
    public static void setSharedPreferenceUri(final int preferenceId, @Nullable final Uri uri) {
        Editor editor = getSharedPreferences().edit();
        if (uri == null) {
            editor.putString(Application.getAppContext().getString(preferenceId), null);
        } else {
            editor.putString(Application.getAppContext().getString(preferenceId), uri.toString());
        }
        editor.apply();
    }

}
