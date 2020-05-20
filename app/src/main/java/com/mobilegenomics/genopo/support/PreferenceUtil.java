package com.mobilegenomics.genopo.support;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobilegenomics.genopo.Application;
import com.mobilegenomics.genopo.core.Step;
import com.mobilegenomics.genopo.dto.WrapperObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    /**
     * Retrieve a list shared preference.
     *
     * @param preferenceId the id of the shared preference.
     * @return the corresponding preference value.
     */
    public static ArrayList<Step> getSharedPreferenceStepList(final int preferenceId) {
        String jsonString = getSharedPreferences()
                .getString(Application.getAppContext().getString(preferenceId), null);

        if (jsonString == null) {
            return null;
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Step>>() {
            }.getType();
            return gson.fromJson(jsonString, type);
        }
    }

    /**
     * Set a shared preference for a List.
     *
     * @param preferenceId the id of the shared preference.
     * @param list         the target value of the preference.
     */
    public static void setSharedPreferenceStepList(final int preferenceId, @Nullable final List<Step> list) {
        Editor editor = getSharedPreferences().edit();
        Gson gson = new Gson();
        String strList = gson.toJson(list);
        editor.putString(Application.getAppContext().getString(preferenceId), strList);
        editor.apply();
    }

    /**
     * Retrieve an Object shared preference.
     *
     * @param preferenceId the id of the shared preference.
     * @return the corresponding preference value.
     */
    public static Object getSharedPreferenceObject(final int preferenceId) {
        String jsonString = getSharedPreferences()
                .getString(Application.getAppContext().getString(preferenceId), null);

        if (jsonString == null) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.fromJson(jsonString, WrapperObject.class);
        }
    }

    /**
     * Set a shared preference for an Object.
     *
     * @param preferenceId the id of the shared preference.
     * @param object       the target value of the preference.
     */
    public static void setSharedPreferenceObject(final int preferenceId, @Nullable final Object object) {
        Editor editor = getSharedPreferences().edit();
        Gson gson = new Gson();
        String strObject = gson.toJson(object);
        editor.putString(Application.getAppContext().getString(preferenceId), strObject);
        editor.apply();
    }

    /**
     * Retrieve app state shared preference.
     *
     * @param preferenceId the id of the shared preference.
     * @return the corresponding preference value.
     */
    public static int getSharedPreferenceInt(final int preferenceId) {
        return getSharedPreferences().getInt(Application.getAppContext().getString(preferenceId), -1);
    }

    /**
     * Retrieve app state shared preference.
     *
     * @param preferenceId the id of the shared preference.
     * @return the corresponding preference value if not the default
     */
    public static int getSharedPreferenceInt(final int preferenceId, int defaultValue) {
        return getSharedPreferences().getInt(Application.getAppContext().getString(preferenceId), defaultValue);
    }

    /**
     * Set a shared preference for app state
     *
     * @param preferenceId the id of the shared preference.
     * @param value        the target value of the preference.
     */
    public static void setSharedPreferenceInt(final int preferenceId, @Nullable final int value) {
        Editor editor = getSharedPreferences().edit();
        editor.putInt(Application.getAppContext().getString(preferenceId), value);
        editor.commit();
    }

    /**
     * Retrieve app state shared preference.
     *
     * @param preferenceId the id of the shared preference.
     * @return the corresponding preference value.
     */
    public static boolean getSharedPreferenceBool(final int preferenceId) {
        return getSharedPreferences().getBoolean(Application.getAppContext().getString(preferenceId), false);
    }

    /**
     * Set a shared preference for app state
     *
     * @param preferenceId the id of the shared preference.
     * @param value        the target value of the preference.
     */
    public static void setSharedPreferenceBool(final int preferenceId, @Nullable final boolean value) {
        Editor editor = getSharedPreferences().edit();
        editor.putBoolean(Application.getAppContext().getString(preferenceId), value);
        editor.apply();
    }

    /**
     * Retrieve app state shared preference.
     *
     * @param preferenceId the id of the shared preference.
     * @return the corresponding preference value.
     */
    public static String getSharedPreferenceString(final int preferenceId) {
        return getSharedPreferences().getString(Application.getAppContext().getString(preferenceId), "");
    }

    /**
     * Retrieve app state shared preference.
     *
     * @param preferenceId the id of the shared preference.
     * @param defaultValue default value if not set by user.
     * @return the corresponding preference value or provided default value
     */
    public static String getSharedPreferenceString(final int preferenceId, final String defaultValue) {
        return getSharedPreferences().getString(Application.getAppContext().getString(preferenceId), defaultValue);
    }

    /**
     * Set a shared preference for app state
     *
     * @param preferenceId the id of the shared preference.
     * @param value        the target value of the preference.
     */
    public static void setSharedPreferenceString(final int preferenceId, @Nullable final String value) {
        Editor editor = getSharedPreferences().edit();
        editor.putString(Application.getAppContext().getString(preferenceId), value);
        editor.apply();
    }

    /**
     * Retrieve app state shared preference.
     *
     * @param preferenceId the id of the shared list preference.
     * @return checked list preferences.
     */
    public static Set<String> getSharedPreferencesStringList(final int preferenceId) {
        return getSharedPreferences().getStringSet(Application.getAppContext().getString(preferenceId), null);
    }

}
