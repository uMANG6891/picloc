package com.umang.picloc.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.umang.picloc.HorizontalImageAdapter;
import com.umang.picloc.ImageViewActivity;
import com.umang.picloc.R;
import com.umang.picloc.instagram.Instagram;
import com.umang.picloc.instagram.InstagramSession;
import com.umang.picloc.instagram.InstagramUser;
import com.umang.picloc.utility.Constants;
import com.umang.picloc.utility.JSC;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity{

    boolean gotLocation = false;
    GoogleMap map;

    double lastLat = -1000;
    double lastLong = -1000;
    ProgressBar pbLoading;

    LinearLayout llImagesMain;
    HorizontalImageAdapter adapter;
    ImageView ivToolbarIcon;
    TextView tvToolbarTitle;

    private InstagramSession mInstagramSession;
    private InstagramUser instagramUser;

    private List<JSONObject> imageData;
    JSONArray jaAll;
    private HashMap<Marker, JSONObject> hashMap = new HashMap<>();

    private boolean isActivityRunning = false;
    private boolean isRefreshing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instagram mInstagram = new Instagram(this, getString(R.string.instagram_client_id), getString(R.string.instagram_client_secret), Constants.CALLBACK_URL);
        mInstagramSession = mInstagram.getSession();
        if (mInstagramSession.isActive()) {
            instagramUser = mInstagramSession.getUser();
            setContentView(R.layout.activity_main);
            initialize();
        } else {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            overridePendingTransition(0, 0);
            finish();
        }
    }

    public void initialize() {
        pbLoading = (ProgressBar) findViewById(R.id.showLoading);
        llImagesMain = (LinearLayout) findViewById(R.id.main_ll_image_main);
        imageData = new ArrayList<>();
        adapter = new HorizontalImageAdapter(this, imageData, llImagesMain);
        ivToolbarIcon = (ImageView) findViewById(R.id.toolbar_icon);
        tvToolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            tvToolbarTitle.setText(instagramUser.fullName);
            if (!instagramUser.profilePicture.isEmpty()) {
                Glide.with(this)
                        .load(instagramUser.profilePicture)
                        .asBitmap()
                        .centerCrop()
                        .into(new BitmapImageViewTarget(ivToolbarIcon) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                                circularBitmapDrawable.setCornerRadius(resource.getWidth());
                                ivToolbarIcon.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityRunning = true;
        refreshMapContent();
    }

    private void refreshMapContent() {
        isRefreshing = true;
        pbLoading.setVisibility(View.VISIBLE);
        if (isLocationEnabledOnPhone()) {
            map.setMyLocationEnabled(true);

            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location loc) {
                    // TODO Auto-generated method stub
                    map.setMyLocationEnabled(false);
                    if (loc != null && !gotLocation) {
                        gotLocation = true;
                        lastLat = loc.getLatitude();
                        lastLong = loc.getLongitude();
                        AsyncHttpClient client = new AsyncHttpClient();
                        client.get(Constants.getInstagramNearByImagesUrl(lastLat, lastLong, instagramUser.accessToken), new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                                // called when response HTTP status is "200 OK"
                                if (isActivityRunning) {
                                    LatLng u;
                                    JSONObject jo = JSC.strToJOb(new String(response));
                                    jaAll = JSC.strToJAr(JSC.getJString(jo, "data"));
                                    imageData = new ArrayList<>();
                                    hashMap = new HashMap<>();

                                    for (int i = 0; i < jaAll.length(); i++) {
                                        jo = JSC.strToJOb(JSC.jArrToString(jaAll, i));
                                        getLocationImage(jo, i);
                                        u = new LatLng(Double.parseDouble(JSC.getJString(jo, "latitude")),
                                                Double.parseDouble(JSC.getJString(jo, "longitude")));
                                        if (i == 0) {
                                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(u, 17));
                                        }
                                    }
                                    isRefreshing = false;
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            }
                        });
                    }

                }

            });
        } else {
            buildAlertMessageNoGps();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityRunning = false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_location_disabled_title))
                .setMessage(getString(R.string.message_location_disabled_message))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, getString(R.string.no_location_available), Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, getString(R.string.no_location_available), Toast.LENGTH_SHORT).show();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isLocationEnabledOnPhone() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getLocationImage(JSONObject jo, final int position) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.getInstagramImageInfoUrl(JSC.getJString(jo, "id"), instagramUser.accessToken), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                if (isActivityRunning) {
                    pbLoading.setVisibility(View.GONE);
                    LatLng u;
                    String title, caption;
                    JSONObject jo = JSC.strToJOb(new String(response));
                    JSONArray ja = JSC.strToJAr(JSC.getJString(jo, "data"));
                    if (ja.length() != 0) {
                        jo = JSC.strToJOb(JSC.jArrToString(ja, 0));
                        JSONObject joLoc = JSC.strToJOb(JSC.getJString(jo, "location")),
                                joImg = JSC.strToJOb(JSC.getJString(jo, "images"));
                        JSONObject joImgLowThumb = JSC.strToJOb(JSC.getJString(joImg, "thumbnail"));

                        JSONObject joUser = JSC.strToJOb(JSC.getJString(jo, "user"));
                        JSONObject joCap = JSC.strToJOb(JSC.getJString(jo, "caption"));

                        u = new LatLng(Double.parseDouble(JSC.getJString(joLoc, "latitude")),
                                Double.parseDouble(JSC.getJString(joLoc, "longitude")));
                        if (joUser != null) {
                            title = JSC.getJString(joUser, "full_name") + " (" + JSC.getJString(joUser, "username") + ")";
                        } else {
                            title = "UNKNOWN";
                        }
                        if (joCap != null) {
                            caption = JSC.getJString(joCap, "text");
                        } else {
                            caption = "";
                        }
                        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                JSONObject jo = hashMap.get(marker);
                                Intent i = new Intent(MainActivity.this, ImageViewActivity.class);
                                i.putExtra(ImageViewActivity.EXTRA_IMAGE_DATA, jo.toString());
                                startActivity(i);
                                return false;
                            }
                        });
                        Marker marker = map.addMarker(new MarkerOptions()
                                .title(title)
                                .icon((BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))
                                .snippet(caption)
                                .position(u));
                        // for staring image view activity on marker click
                        hashMap.put(marker, jo);
                        // for staring image view activity on image click
                        imageData.add(jo);
                        adapter.swapData(imageData);

                        getBitmap(JSC.getJString(joImgLowThumb, "url"), marker);
                    } else {
//                    Log.e("ARRAY", jo.toString());
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        });
    }

    public void getBitmap(String url, final Marker marker) {
        if (isActivityRunning) {
            Glide.with(getApplicationContext()).
                    load(url)
                    .asBitmap()
                    .fitCenter()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(Constants.addWhiteBorder(bitmap)));
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if (!isRefreshing) {
                    gotLocation = false;
                    refreshMapContent();
                } else {
                    Toast.makeText(MainActivity.this, "Already refreshing...", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_logout:
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle(R.string.logout_title)
                        .setMessage(R.string.logout_message)
                        .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mInstagramSession.reset();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                                overridePendingTransition(0, 0);
                            }
                        })
                        .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            default:
                return false;
        }
    }
}
