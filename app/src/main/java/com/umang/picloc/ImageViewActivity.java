package com.umang.picloc;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.umang.picloc.utility.Constants;
import com.umang.picloc.utility.JSC;
import com.umang.picloc.utility.Utility;

import org.json.JSONObject;

/**
 * Created by umang on 8/10/15.
 */
public class ImageViewActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_DATA = "extra_image_data";
    String imageData;

    ImageView ivMainPhoto, ivUserImage;
    TextView tvUserName, tvTime, tvDescription;
    ProgressBar pb;


    JSONObject joMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if (extras != null && i.hasExtra(EXTRA_IMAGE_DATA)) {
            imageData = extras.getString(EXTRA_IMAGE_DATA);
            ivMainPhoto = (ImageView) findViewById(R.id.iv_iv_main_photo);
            ivMainPhoto.setMinimumHeight(ivMainPhoto.getWidth());
            ivUserImage = (ImageView) findViewById(R.id.iv_iv_user);

            tvUserName = (TextView) findViewById(R.id.iv_tv_name);
            tvTime = (TextView) findViewById(R.id.iv_tv_time);
            tvDescription = (TextView) findViewById(R.id.iv_tv_description);

            pb = (ProgressBar) findViewById(R.id.iv_pb_loading_image);
            joMain = JSC.strToJOb(imageData);

            JSONObject joImg = JSC.strToJOb(JSC.getJString(joMain, "images"));
            joImg = JSC.strToJOb(JSC.getJString(joImg, "standard_resolution"));
            final JSONObject joUser = JSC.strToJOb(JSC.getJString(joMain, "user"));
            JSONObject joCap = JSC.strToJOb(JSC.getJString(joMain, "caption"));

            if (joUser != null) {
                tvUserName.setText(JSC.getJString(joUser, "full_name"));
                tvUserName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String profileUrl = Constants.BASE_INSTAGRAM_PROFILE + JSC.getJString(joUser, "username");
                        Uri uri = Uri.parse(profileUrl);
                        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                        likeIng.setPackage("com.instagram.android");
                        try {
                            startActivity(likeIng);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl)));
                        }
                    }
                });
            }
            if (joCap != null) {
                tvDescription.setText(JSC.getJString(joCap, "text"));
            }
            tvTime.setText(Utility.getPrettyTime(this, JSC.getJLong(joMain, "created_time")));

            //get image main
            Glide.with(getApplicationContext()).
                    load(JSC.getJString(joImg, "url"))
                    .asBitmap()
                    .fitCenter()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            ivMainPhoto.setImageBitmap(bitmap);
                            pb.setVisibility(View.GONE);
                        }
                    });
            Glide.with(this)
                    .load(JSC.getJString(joUser, "profile_picture"))
                    .asBitmap()
                    .centerCrop()
                    .into(new BitmapImageViewTarget(ivUserImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCornerRadius(resource.getWidth());
                            ivUserImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            Toast.makeText(this, getString(R.string.no_image_data), Toast.LENGTH_LONG).show();
            finish();
        }
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
