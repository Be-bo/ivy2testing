package com.ivy2testing.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public final class ImageUtils {

    public static int IMAGE_MAX_DIMEN = 1500;
    public static int PREVIEW_IMAGE_DIVIDER = 3;

    public static byte[] compressAndGetPreviewBytes(Bitmap bitmap){ //returns byte array of an incoming bitmap and reduces it into our standardized preview image size
        boolean widthLarger = bitmap.getWidth() >= bitmap.getHeight(); //check which dimension is greater
        boolean dimenLimitExceeded = Integer.max(bitmap.getWidth(), bitmap.getHeight()) > IMAGE_MAX_DIMEN; //and which if any exceeds our dimension limit
        int newHeight;
        int newWidth;

        if(dimenLimitExceeded && widthLarger){ //width is the larger (the defining) dimension and it exceeded the dimension limit
            newWidth = IMAGE_MAX_DIMEN;
            newHeight = (int) (((float)bitmap.getHeight()/(float)bitmap.getWidth()) * (float) IMAGE_MAX_DIMEN);

        }else if(dimenLimitExceeded && !widthLarger){ //height is the larger (the defining) dimension and it exceeded the dimension limit
            newHeight = IMAGE_MAX_DIMEN;
            newWidth = (int) (((float)bitmap.getWidth()/(float)bitmap.getHeight()) * (float) IMAGE_MAX_DIMEN);

        }else{ //dimension limit not exceeded
            newHeight = bitmap.getHeight();
            newWidth = bitmap.getWidth();
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth/PREVIEW_IMAGE_DIVIDER,newHeight/PREVIEW_IMAGE_DIVIDER, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    public static byte[] compressAndGetBytes(Bitmap bitmap){ //returns byte array of an incoming bitmap and reduces it into our standardized image size
        boolean widthLarger = bitmap.getWidth() >= bitmap.getHeight(); //check which dimension is greater
        boolean dimenLimitExceeded = Integer.max(bitmap.getWidth(), bitmap.getHeight()) > IMAGE_MAX_DIMEN; //and which if any exceeds our dimension limit
        int newHeight;
        int newWidth;

        if(dimenLimitExceeded && widthLarger){ //width is the larger (the defining) dimension and it exceeded the dimension limit
            newWidth = IMAGE_MAX_DIMEN;
            newHeight = (int) (((float)bitmap.getHeight()/(float)bitmap.getWidth()) * (float) IMAGE_MAX_DIMEN);

        }else if(dimenLimitExceeded && !widthLarger){ //height is the larger (the defining) dimension and it exceeded the dimension limit
            newHeight = IMAGE_MAX_DIMEN;
            newWidth = (int) (((float)bitmap.getWidth()/(float)bitmap.getHeight()) * (float) IMAGE_MAX_DIMEN);

        }else{ //dimension limit not exceeded
            newHeight = bitmap.getHeight();
            newWidth = bitmap.getWidth();
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth,newHeight, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    // Return path of a user's preview image
    public static String getPreviewPath(String user_id) {
        return "userfiles/" + user_id + "/previewimage.jpg";
    }
}
