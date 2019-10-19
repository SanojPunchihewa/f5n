package com.mobilegenomics.f5n.support;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Decompress {

    private Context mContext;

    private File _zipFile;

    private String unzipLocation;

    private static final String TAG = "UNZIPUTIL";

    public Decompress(Context context, File zipFile) {
        mContext = context;
        _zipFile = zipFile;
        unzipLocation = zipFile.getPath();
        unzipLocation = unzipLocation.substring(0, unzipLocation.lastIndexOf("."));
        _dirChecker("");
    }

    public void unzip() {
        try {
            Toast.makeText(mContext, "Extraction Started", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Starting to unzip");
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;

            _dirChecker(unzipLocation);

            while ((ze = zin.getNextEntry()) != null) {
                Log.v(TAG, "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
                    _dirChecker(unzipLocation + "/" + ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(new File(unzipLocation, ze.getName()));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int count;

                    // reading and writing
                    while ((count = zin.read(buffer)) != -1) {
                        baos.write(buffer, 0, count);
                        byte[] bytes = baos.toByteArray();
                        fout.write(bytes);
                        baos.reset();
                    }

                    fout.close();
                    zin.closeEntry();
                }

            }
            zin.close();
            Log.i(TAG, "Finished unzip");
            Toast.makeText(mContext, "Extraction completed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unzip Error", e);
            Toast.makeText(mContext, "Extraction Error", Toast.LENGTH_SHORT).show();
        }

    }

    private void _dirChecker(String dir) {
        File f = new File(dir);
        Log.i(TAG, "creating dir " + dir);

        if (dir.length() >= 0 && !f.isDirectory()) {
            f.mkdirs();
        }
    }
}