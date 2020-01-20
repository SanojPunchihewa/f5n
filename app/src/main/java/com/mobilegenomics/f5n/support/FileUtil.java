package com.mobilegenomics.f5n.support;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URLConnection;

/**
 * Utility class for helping parsing file systems.
 */
public class FileUtil {

    /**
     * The name of the temp file created by the pipe
     */
    public static final String TMP_LOG_FILE_NAME = "tmp.log";

    /**
     * The name of the log file created by f5n
     */
    public static final String LOG_FILE_NAME = "f5n.log";

    /**
     * The name of the primary volume (LOLLIPOP).
     */
    private static final String PRIMARY_VOLUME_NAME = "primary";

    /**
     * Hide default constructor.
     */
    private FileUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file              The file.
     * @param isDirectory       flag indicating if the file should be a directory.
     * @param createDirectories flag indicating if intermediate path directories should be created if not existing.
     * @return The DocumentFile
     */
    public static DocumentFile getDocumentFile(final Context context, final File file, final boolean isDirectory,
            final boolean createDirectories, final Uri treeUri) {
        if (treeUri == null) {
            return null;
        }

        String fullPath = null;
        try {
            fullPath = file.getCanonicalPath();
        } catch (IOException e) {
            return null;
        }

        String baseFolder = getFullPathFromTreeUri(context, treeUri);

        if (baseFolder == null) {
            return null;
        }

        String relativePath = fullPath.substring(baseFolder.length() + 1);

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);

        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if (i < parts.length - 1) {
                    if (createDirectories) {
                        nextDocument = document.createDirectory(parts[i]);
                    } else {
                        return null;
                    }
                } else if (isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile(URLConnection.guessContentTypeFromName(parts[i]), parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    /**
     * Get the full path of a document from its tree URI.
     *
     * @param treeUri The tree RI.
     * @return The path (without trailing file separator).
     */
    private static String getFullPathFromTreeUri(final Context context, final Uri treeUri) {
        if (treeUri == null) {
            return null;
        }
        String volumePath = FileUtil.getVolumePath(context, FileUtil.getVolumeIdFromTreeUri(treeUri));
        if (volumePath == null) {
            return FileUtil.getVolumeIdFromTreeUri(treeUri);
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = FileUtil.getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath != null && documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            } else {
                return volumePath + File.separator + documentPath;
            }
        } else {
            return volumePath;
        }
    }

    /**
     * Get the path of a certain volume.
     *
     * @param volumeId The volume id.
     * @return The path.
     */
    private static String getVolumePath(final Context context, final String volumeId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            StorageManager mStorageManager =
                    (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }
            }

            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get the volume ID from the tree URI.
     *
     * @param treeUri The tree URI.
     * @return The volume ID.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        } else {
            return null;
        }
    }

    /**
     * Get the document path (relative to volume name) for a tree URI (LOLLIPOP).
     *
     * @param treeUri The tree URI.
     * @return the document path.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        } else {
            return File.separator;
        }
    }

    /**
     * Check wether the device has an external SDCard
     *
     * @return availability of external sdcard.
     */
    public static boolean isExternalSDCardAvailable(Context context) {
        return ContextCompat.getExternalFilesDirs(context, null).length >= 2;
    }

    /**
     * Check wether the file is in external SDCard
     *
     * @return file is in external SDCard.
     */
    public static boolean isFileInExternalSdCard(String filePath) {
        String internalStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        return !filePath.contains(internalStorage);
    }

}
