package com.umang.picloc.utility;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

/**
 * Created by umang on 7/10/15.
 */
public class Constants {

    public static String CLIENT_ID = "ea99cc971b114a3eb1de2a9b685f5615";
    public static String CLIENT_SECRET = "a9d62c64798a455b87f763d3a552cd08";
    public static String CALLBACK_URL = "http://umangpandya.com";

    public static final String INSTAGRAM_BASE_URL = "https://api.instagram.com/v1/";

    public static String getInstagramNearByImagesUrl(double latitude, double longitude, String instagramToken) {
        return INSTAGRAM_BASE_URL + "locations/search?lat=" + latitude + "&lng=" + longitude
                + "&DISTANCE=500&access_token=" + instagramToken;
    }

    public static String getInstagramImageInfoUrl(String imageId, String token) {
        return INSTAGRAM_BASE_URL + "locations/" + imageId + "/media/recent?access_token=" + token;
    }

    public static Bitmap addWhiteBorder(Bitmap bmp) {
        int borderSize = 5;
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

}
