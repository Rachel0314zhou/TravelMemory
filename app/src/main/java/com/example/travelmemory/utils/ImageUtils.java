package com.example.travelmemory.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Copies a Uri resource (from gallery or elsewhere) into the App's private file directory under files/photos/
     * Returns the absolute path of the newly copied file.
     */
    public static String copyImageToAppDirectory(Context context, Uri uri) {
        if (uri == null) return "";
        try {
            File photosDir = new File(context.getFilesDir(), "photos");
            if (!photosDir.exists()) {
                photosDir.mkdirs();
            }

            String filename = "photo_" + System.currentTimeMillis() + ".jpg";
            File destFile = new File(photosDir, filename);

            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) {
                return "";
            }

            OutputStream os = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            is.close();

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy image: ", e);
        }
        return "";
    }

    /**
     * Safely loads a bitmap into any ImageView from the local absolute file path.
     * Uses scale-down options to prevent Out Of Memory (OOM) crash in Emulator.
     */
    public static void displayLocalImage(ImageView imageView, String path, int placeholderResId) {
        if (path == null || path.isEmpty()) {
            imageView.setImageResource(placeholderResId);
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            imageView.setImageResource(placeholderResId);
            return;
        }

        try {
            // Decode with inSampleSize to prevent OOM
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate sample size
            int reqWidth = 600;
            int reqHeight = 600;
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(placeholderResId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying local image: " + path, e);
            imageView.setImageResource(placeholderResId);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Saves a temporary bitmap into app's cache folder for the sharing purpose
     */
    public static File saveBitmapToCache(Context context, Bitmap bitmap) {
        try {
            File cacheDir = new File(context.getCacheDir(), "shared");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File file = new File(cacheDir, "shared_memory_card.png");
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return file;
        } catch (Exception e) {
            Log.e(TAG, "Error saving shared bitmap to cache: ", e);
        }
        return null;
    }
}
