package com.umang.picloc;

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

    public static Bitmap addWhiteBorder(Bitmap bmp) {
        int borderSize = 5;
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }
}
