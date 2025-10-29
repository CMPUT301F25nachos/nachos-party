package com.example.nachos_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageUtils {

    /**
     * Loads a base64 encoded image into an ImageView
     * @param imageView The ImageView to load the image into
     * @param base64String The base64 encoded image string
     * @param placeholderResId Resource ID for placeholder if decoding fails
     */
    public static void loadBase64Image(ImageView imageView, String base64String, int placeholderResId) {
        if (base64String == null || base64String.isEmpty()) {
            imageView.setImageResource(placeholderResId);
            return;
        }

        try {
            byte[] decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(placeholderResId);
            }
        } catch (Exception e) {
            imageView.setImageResource(placeholderResId);
        }
    }

    /**
     * Decodes a base64 string to a Bitmap
     * @param base64String The base64 encoded image string
     * @return Bitmap or null if decoding fails
     */
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}