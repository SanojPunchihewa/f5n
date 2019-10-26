package com.mobilegenomics.f5n.support;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipManager {

    private Context mContext;

    private String unzipLocation;

    private static final String TAG = ZipManager.class.getSimpleName();

    public ZipManager(Context context) {
        mContext = context;
    }

    public void unzip(String zipFilePath) {
        try {

            File zipFile = new File(zipFilePath);

            unzipLocation = zipFile.getPath();
            unzipLocation = unzipLocation.substring(0, unzipLocation.lastIndexOf("."));

            Toast.makeText(mContext, "Extraction Started", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Starting to unzip");
            FileInputStream fin = new FileInputStream(zipFile);
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

    public boolean zip(String sourcePath) {

        final int BUFFER = 2048;

        String zipPath;

        if (sourcePath.lastIndexOf(".") == -1) {
            // folder
            zipPath = sourcePath + ".zip";
        } else {
            zipPath = sourcePath.substring(0, sourcePath.lastIndexOf(".")) + ".zip";
        }

        File sourceFile = new File(sourcePath);
        try {
            Log.i(TAG, "Starting to zip ");
            BufferedInputStream bis = null;
            FileOutputStream fos = new FileOutputStream(zipPath);
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
            if (sourceFile.isDirectory()) {
                zipSubFolder(zos, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourceFile);
                bis = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(sourceFile.getName());
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                zos.putNextEntry(entry);
                int count;
                while ((count = bis.read(data, 0, BUFFER)) != -1) {
                    zos.write(data, 0, count);
                }
                zos.closeEntry();
                bis.close();
            }
            zos.close();
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e);
            return false;
        }
        Log.i(TAG, "Finished zip");
        return true;
    }

    private void zipSubFolder(ZipOutputStream out, File folder,
            int basePathLength) throws IOException {

        Log.i(TAG, "Starting to zip folder");

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }

        Log.i(TAG, "Finished zip folder");

    }

    private void _dirChecker(String dir) {
        File f = new File(dir);
        Log.i(TAG, "creating dir " + dir);

        if (dir.length() >= 0 && !f.isDirectory()) {
            f.mkdirs();
        }
    }
}