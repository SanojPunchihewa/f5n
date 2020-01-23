package com.mobilegenomics.f5n.support;

import static net.gotev.uploadservice.UploadService.BUFFER_SIZE;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import com.mobilegenomics.f5n.R;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipManager {

    private static final String TAG = ZipManager.class.getSimpleName();

    public static boolean showExtractPercentage = true;

    ZipListener zipListener;

    private Context context;

    private String unzipLocation;

    private long compressedBytes = 0;

    private long totalBytesToCompress = 0;

    public ZipManager(Context context, ZipListener zipListener) {
        this.context = context;
        this.zipListener = zipListener;
    }

    public void unzip(Uri treeUri, String zipFilePath) {

        if ((PreferenceUtil.getSharedPreferenceUri(R.string.sdcard_uri) != null) && FileUtil
                .isFileInExternalSdCard(zipFilePath)) {
            unzipUsingSAF(context, treeUri, zipFilePath);
        } else {
            unzip(zipFilePath);
        }
    }

    private void unzip(String zipFilePath) {

        class UnzipAysnc extends AsyncTask<String, Integer, Boolean> {

            private int bytesZipped = 0;

            private int totalBytes;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ZipFile zip = null;
                try {
                    zip = new ZipFile(zipFilePath);
                    totalBytes = zip.size();
                } catch (IOException e) {
                    showExtractPercentage = false;
                    Log.e(TAG, "Error reading Zip File: " + e);
                }
                zipListener.onStarted(totalBytes);
                Log.i(TAG, "Starting to unzip");
            }

            @Override
            protected Boolean doInBackground(final String... strings) {
                try {

                    File zipFile = new File(zipFilePath);

                    unzipLocation = zipFile.getPath();
                    unzipLocation = unzipLocation.substring(0, unzipLocation.lastIndexOf("."));

                    FileInputStream fin = new FileInputStream(zipFile);
                    ZipInputStream zin = new ZipInputStream(fin);
                    ZipEntry ze = null;

                    _dirChecker(unzipLocation);

                    while ((ze = zin.getNextEntry()) != null) {
                        if (ze.isDirectory()) {
                            _dirChecker(unzipLocation + "/" + ze.getName());
                        } else {

                            bytesZipped++;
                            publishProgress(bytesZipped);

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
                } catch (Exception e) {
                    Log.e(TAG, "Unzip Error: " + e);
                    return false;
                }
                return true;
            }

            @Override
            protected void onProgressUpdate(final Integer... values) {
                super.onProgressUpdate(values);
                zipListener.onProgress(bytesZipped, totalBytes);
            }

            @Override
            protected void onPostExecute(final Boolean result) {
                super.onPostExecute(result);
                Log.i(TAG, "Finished unzip");
                zipListener.onComplete(result, null);
            }
        }
        new UnzipAysnc().execute();
    }

    private void unzipUsingSAF(Context context, Uri treeUri, String zipFilePath) {

        class UnzipAysnc extends AsyncTask<String, Integer, Boolean> {

            private int bytesUnZipped = 0;

            private int totalBytes;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ZipFile zip = null;
                try {
                    zip = new ZipFile(zipFilePath);
                    totalBytes = zip.size();
                } catch (IOException e) {
                    Log.e(TAG, "Error reading Zip File: " + e);
                    showExtractPercentage = false;
                }
                zipListener.onStarted(totalBytes);
                Log.i(TAG, "Starting to unzip");
            }

            @Override
            protected Boolean doInBackground(final String... strings) {

                DocumentFile zipDocumentFile = FileUtil
                        .getDocumentFile(context, new File(zipFilePath), false, false, treeUri);

                try {
                    DocumentFile unZippedDirectory = zipDocumentFile.getParentFile()
                            .createDirectory(
                                    zipDocumentFile.getName()
                                            .substring(0, zipDocumentFile.getName().lastIndexOf(".")));

                    InputStream inputStream = context.getContentResolver().openInputStream(zipDocumentFile.getUri());
                    assert inputStream != null;
                    ZipInputStream zipInputStream = new ZipInputStream(
                            new BufferedInputStream(inputStream, BUFFER_SIZE));

                    ZipEntry ze;
                    while ((ze = zipInputStream.getNextEntry()) != null) {
                        if (ze.isDirectory()) {

                            String[] paths = ze.getName().split("/");

                            DocumentFile documentFile = null;
                            for (String path : paths) {
                                if (documentFile == null) {
                                    documentFile = unZippedDirectory.findFile(path);
                                    if (documentFile == null) {
                                        documentFile = unZippedDirectory.createDirectory(path);
                                    }
                                } else {
                                    DocumentFile newDocumentFile = documentFile.findFile(path);
                                    if (newDocumentFile == null) {
                                        documentFile = documentFile.createDirectory(path);
                                    } else {
                                        documentFile = newDocumentFile;
                                    }
                                }
                            }
                            if (documentFile == null || !documentFile.exists()) {
                                Log.e(TAG, "No document file found");
                                return false;
                            }

                        } else {

                            bytesUnZipped++;
                            publishProgress(bytesUnZipped);

                            String[] paths = ze.getName().split("/");

                            DocumentFile documentFile = null;
                            for (int i = 0; i < paths.length - 1; i++) {
                                if (documentFile == null) {
                                    documentFile = unZippedDirectory.findFile(paths[i]);
                                    if (documentFile == null) {
                                        documentFile = unZippedDirectory.createDirectory(paths[i]);
                                    }
                                } else {
                                    DocumentFile newDocumentFile = documentFile.findFile(paths[i]);
                                    if (newDocumentFile == null) {
                                        documentFile = documentFile.createDirectory(paths[i]);
                                    } else {
                                        documentFile = newDocumentFile;
                                    }
                                }
                            }

                            DocumentFile unzipDocumentFile;
                            if (documentFile == null) {
                                unzipDocumentFile = unZippedDirectory
                                        .createFile(URLConnection.guessContentTypeFromName(ze.getName()),
                                                paths[paths.length - 1]);
                            } else {
                                unzipDocumentFile = documentFile
                                        .createFile(URLConnection.guessContentTypeFromName(ze.getName()),
                                                paths[paths.length - 1]);
                            }
                            OutputStream outputStream = context.getContentResolver()
                                    .openOutputStream(unzipDocumentFile.getUri());

                            int read;
                            byte[] data = new byte[BUFFER_SIZE];
                            assert outputStream != null;
                            while ((read = zipInputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                                outputStream.write(data, 0, read);
                            }
                            outputStream.close();
                            zipInputStream.closeEntry();
                        }
                    }
                    zipInputStream.close();

                } catch (Exception e) {
                    Log.e(TAG, "Unzip Error: " + e);
                    return false;
                }
                return true;
            }

            @Override
            protected void onProgressUpdate(final Integer... values) {
                super.onProgressUpdate(values);
                zipListener.onProgress(bytesUnZipped, totalBytes);
            }

            @Override
            protected void onPostExecute(final Boolean result) {
                super.onPostExecute(result);
                Log.i(TAG, "Finished unzip");
                zipListener.onComplete(result, null);
            }
        }
        new UnzipAysnc().execute();
    }

    public void zip(String sourcePath) {

        class ZipAysnc extends AsyncTask<String, Integer, Boolean> {

            private String zipPath;

            FileOutputStream fos;

            ZipOutputStream zos;

            File sourceFile;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sourceFile = new File(sourcePath);
                totalBytesToCompress = getDirectorySize(sourceFile);
                zipListener.onStarted(totalBytesToCompress);
                Log.i(TAG, "Starting to zip");
                zipPath = sourcePath + ".out.zip";
                try {
                    fos = new FileOutputStream(zipPath);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File IO Error: " + e);
                }
                zos = new ZipOutputStream(fos);

            }

            @Override
            protected Boolean doInBackground(final String... strings) {

                File sourceFile = new File(sourcePath);
                try {
                    addDirToZipArchive(zos, sourceFile, null);
                } catch (Exception e) {
                    Log.e(TAG, "Zip Exception: " + e);
                    return false;
                }
                return true;
            }

            @Override
            protected void onProgressUpdate(final Integer... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(final Boolean result) {
                super.onPostExecute(result);
                Log.i(TAG, "Finished zip");
                zipListener.onComplete(result, null);
                try {
                    zos.flush();
                    fos.flush();
                    zos.close();
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "File IO Error: " + e);
                }
            }
        }
        new ZipAysnc().execute();
    }

    public void addDirToZipArchive(ZipOutputStream zos, File fileToZip, String parentDirectoryName)
            throws Exception {
        if (fileToZip == null || !fileToZip.exists()) {
            return;
        }

        String zipEntryName = fileToZip.getName();
        if (parentDirectoryName != null && !parentDirectoryName.isEmpty()) {
            zipEntryName = parentDirectoryName + "/" + fileToZip.getName();
        }

        if (fileToZip.isDirectory()) {
            for (File file : fileToZip.listFiles()) {
                addDirToZipArchive(zos, file, zipEntryName);
            }
        } else {
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(fileToZip);
            zos.putNextEntry(new ZipEntry(zipEntryName));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
                compressedBytes += length;
                zipListener.onProgress(compressedBytes, totalBytesToCompress);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    private void _dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    public static int getZipPercentage(long currentBytes, long totalBytes) {
        return (int) ((100 * currentBytes) / totalBytes);
    }

    private static long getDirectorySize(File dir) {

        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if (fileList[i].isDirectory()) {
                    result += getDirectorySize(fileList[i]);
                } else {
                    // Sum the file size in bytes
                    result += fileList[i].length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }

}