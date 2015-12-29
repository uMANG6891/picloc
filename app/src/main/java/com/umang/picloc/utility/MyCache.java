package com.umang.picloc.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by umang on 28/12/15.
 */
public class MyCache {

    private LruCache<String, Bitmap> mMemoryCache;
    private Context con;
    private LoadImage callback;

    public static MyCache with(Context con) {
        MyCache cache = new MyCache();
        cache.con = con;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        cache.mMemoryCache = new LruCache<String, Bitmap>(maxMemory) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        return cache;
    }

    public void loadImage(String imageUrl, LoadImage callback) {
        final Bitmap bitmap = mMemoryCache.get(imageUrl);
        if (bitmap != null) {
            callback.onLoad(bitmap);
        } else {
            this.callback = callback;
            BitmapWorkerTask task = new BitmapWorkerTask();
            task.execute(imageUrl);
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        String imageUrl;

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                imageUrl = params[0];
                URL url = new URL(imageUrl);
                URLConnection conn = url.openConnection();
                return BitmapFactory.decodeStream(conn.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                callback.onLoad(bitmap);
                mMemoryCache.put(imageUrl, bitmap);
            }
        }
    }

    public interface LoadImage {
        public void onLoad(Bitmap resource);
    }
}
