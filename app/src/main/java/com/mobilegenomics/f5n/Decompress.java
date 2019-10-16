package com.mobilegenomics.f5n;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Decompress {

    private File _zipFile;

    private InputStream _zipFileStream;

    private static final String ROOT_LOCATION = "/sdcard";

    private static final String TAG = "UNZIPUTIL";

    public Decompress(File zipFile) {
        _zipFile = zipFile;
        _dirChecker("");
    }

    public Decompress(InputStream zipFile) {
        _zipFileStream = zipFile;

        _dirChecker("");
    }

    public void unzip() {
        try {
            Log.i(TAG, "Starting to unzip");
            InputStream fin = _zipFileStream;
            if (fin == null) {
                fin = new FileInputStream(_zipFile);
            }
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v(TAG, "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
                    _dirChecker(ROOT_LOCATION + "/" + ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(new File(ROOT_LOCATION, ze.getName()));
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
        } catch (Exception e) {
            Log.e(TAG, "Unzip Error", e);
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