package com.mudah.my.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.mudah.my.R;
import com.mudah.my.configs.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created by ec-3 on 9/30/13.
 */
public class ImageUploadController {

    private static final String TAG = ImageUploadController.class.getSimpleName();

    /**
     * Use for constant of file extension
     */
    public enum Extension {
        GIF,
        JPEG,
        JPG,
        PNG
    }

    /**
     * Create temp file on external storage
     * @param filename file name
     * @param extension extension of file name
     * @return {@link File} blank file
     */
    public static File createFile(String filename, Extension extension) {
        if (TextUtils.isEmpty(filename)) {
            filename = "" + System.currentTimeMillis();
        }

        File blankFile = null;

        try {
            File localDir = new File(android.os.Environment.getExternalStorageDirectory(), Constants.IMAGE_LOCAL_ROOT_DIR_NAME);
            if (!localDir.exists())
                localDir.mkdir();

            switch (extension) {
                case GIF:
                    blankFile = new File(localDir.getAbsolutePath() + "/" + filename + "." + Extension.GIF);
                    break;
                case JPEG:
                    blankFile = new File(localDir.getAbsolutePath() + "/" + filename + "." + Extension.JPEG);
                    break;
                case JPG:
                    blankFile = new File(localDir.getAbsolutePath() + "/" + filename + "." + Extension.JPG);
                    break;
                case PNG:
                    blankFile = new File(localDir.getAbsolutePath() + "/" + filename + "." + Extension.PNG);
                    break;
            }
            Log.i(TAG, blankFile.getAbsolutePath());
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        return blankFile;
    }

    /**
     * save file to local storage
     * @param bitmap
     * @return file path
     */
    public static String saveFile(Context ctx, Bitmap bitmap) {
        if (bitmap == null)
            return null;
        // 1. save file
        // 2. resize file & validate resolution [pending]
        // 3. return file path
        try {
            File fileDir = ctx.getFilesDir();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, ctx.getResources().getInteger(R.integer.compress_quality), bytes);
            Log.d(TAG, " images quality = " + String.valueOf(ctx.getResources().getInteger(R.integer.compress_quality)));
            File file = new File(fileDir + "/" + Constants.IMAGE_FILENAME_LOCAL_PREFIX + System.currentTimeMillis() + "." + Extension.JPEG);
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.close();
            Log.e(TAG, "file size " + file.length() + ", resolution " + bitmap.getWidth() +"X"+ bitmap.getHeight());
            if (Build.VERSION.SDK_INT > 13) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "saveFile " + e.getMessage());
            return null;
        }
    }

    /**
     * Remove temporary photos off the devices
     * @param ctx
     */
    public static void clearTempImage(Context ctx) {

        // Clear upload photos temp folder
        File fileDir = ctx.getFilesDir();
        removeAllImagesFromFolder(fileDir);

    }

    /**
     * Remove all images off the given folder
     * @param fileDir
     */
    private static void removeAllImagesFromFolder(File fileDir) {

        if(fileDir != null) {
            String[] filenames = fileDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {

                    Log.d(TAG, "Filter File: " + filename);
                    if(filename.startsWith(Constants.IMAGE_FILENAME_LOCAL_PREFIX)
                            && filename.endsWith("." + Extension.JPEG)) {
                        Log.d(TAG, "Filter File: included");
                        return true;
                    }
                    return false;
                }
            });

            for(String name: filenames) {
                deleteFile(fileDir, name);
            }
        }
    }

    /**
     * Delete file off the discs
     * @param fileDir Folder in which the file stored in
     * @param name Name of the file.
     * @return
     */
    public static boolean deleteFile(File fileDir, String name) {
        try {
            File f = new File(fileDir, name);

            if(f.exists()) {
                boolean b = f.delete();
                f = null;
                Log.w(TAG,  (b? "Delete": "Cannot Delete") +" File " + name);
            }else {
                Log.w(TAG, "File doesn't exists: " + name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Delete file off the discs
     * @param uri
     * @return
     */
    public static boolean deleteFile(Uri uri) {

        File f = new File(uri.getPath());

        if(f.exists()) {
            return f.delete();
        }
        // File doesn't exist, return true anyway
        return true;
    }
}
