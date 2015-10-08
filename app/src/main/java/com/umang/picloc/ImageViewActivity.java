package com.umang.picloc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.umang.picloc.utility.JSC;

import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

/**
 * Created by umang on 8/10/15.
 */
public class ImageViewActivity extends AppCompatActivity {

    String imageData;
    ImageView iv;
    ProgressBar pb;

    JSONObject joMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imageData = extras.getString("IMAGE_DATA");
            iv = (ImageView) findViewById(R.id.imageView);
            pb = (ProgressBar) findViewById(R.id.ivProgressBar);
        } else {
            Toast.makeText(this, "NO data for this image", Toast.LENGTH_LONG).show();
            finish();
        }

        joMain = JSC.strToJOb(imageData);
        JSONObject joLoc = JSC.strToJOb(JSC.getJString(joMain, "location"));
        JSONObject joImg = JSC.strToJOb(JSC.getJString(joMain, "images"));
        joImg = JSC.strToJOb(JSC.getJString(joImg, "standard_resolution"));
        JSONObject joUser = JSC.strToJOb(JSC.getJString(joMain, "user"));
        JSONObject joCap = JSC.strToJOb(JSC.getJString(joMain, "caption"));

        if (joUser != null) {
            getSupportActionBar().setTitle(JSC.getJString(joUser, "full_name"));
        }
        if (joCap != null) {
            getSupportActionBar().setSubtitle(JSC.getJString(joCap, "text"));
        }

        //get image
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(JSC.getJString(joImg, "url"), new FileAsyncHttpResponseHandler(getBaseContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, File response) {
                // Do something with the file `response`
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                iv.setImageBitmap(bitmap);
                pb.setVisibility(View.GONE);
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Log.e("image", "err");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
