package com.mobilegenomics.genopo.support;

import android.content.Context;
import androidx.annotation.RawRes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.util.Scanner;

public class JSONFileHelper {

    private static String readJsonAsString(Context context, @RawRes int file) {
        //1 Takes your JSON file from the raw folder
        InputStream inputStream = context.getResources().openRawResource(file);
        //2 This reads your JSON file
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
        return jsonString;
    }

    public static JsonObject rawtoJsonObject(Context context, @RawRes int file) {
        JsonParser parser = new JsonParser();
        return (JsonObject) parser.parse(readJsonAsString(context, file));
    }

}
