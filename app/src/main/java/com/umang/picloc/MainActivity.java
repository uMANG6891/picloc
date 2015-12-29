package com.umang.picloc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.umang.picloc.instagram.Instagram;
import com.umang.picloc.instagram.InstagramSession;
import com.umang.picloc.instagram.InstagramUser;
import com.umang.picloc.utility.Constants;
import com.umang.picloc.utility.JSC;
import com.umang.picloc.utility.MyCache;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean gotLocation = false;
    GoogleMap map;

    double lastLat = -1000;
    double lastLong = -1000;
    ProgressBar pbLoading;

    LinearLayout llImagesMain;
    HorizontalImageAdapter adapter;

    private InstagramSession mInstagramSession;
    private Instagram mInstagram;
    private InstagramUser instagramUser;

    private List<JSONObject> imageData;
    JSONArray jaAll;
    private HashMap<Marker, JSONObject> hashMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstagram = new Instagram(this, Constants.CLIENT_ID, Constants.CLIENT_SECRET, Constants.CALLBACK_URL);
        mInstagramSession = mInstagram.getSession();
        if (mInstagramSession.isActive()) {
            instagramUser = mInstagramSession.getUser();
            setContentView(R.layout.activity_main);
            initialize();
        } else {
            Intent i = new Intent(this, LoginActivity.class);
            finish();
            startActivity(i);
        }
    }

    public void initialize() {
        pbLoading = (ProgressBar) findViewById(R.id.showLoading);
        llImagesMain = (LinearLayout) findViewById(R.id.main_ll_image_main);
        imageData = new ArrayList<>();
        adapter = new HorizontalImageAdapter(this, imageData, llImagesMain);

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                                LatLng u;
                                JSONObject jo = JSC.strToJOb(new String(response));
                                jaAll = JSC.strToJAr(JSC.getJString(jo, "data"));

                                for (int i = 0; i < jaAll.length(); i++) {
                                    jo = JSC.strToJOb(JSC.jArrToString(jaAll, i));
                                    getLocationImage(jo, i);
                                    u = new LatLng(Double.parseDouble(JSC.getJString(jo, "latitude")),
                                            Double.parseDouble(JSC.getJString(jo, "longitude")));
                                    if (i == 0) {
                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(u, 17));
                                    }
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
                pbLoading.setVisibility(View.GONE);
                LatLng u;
                String title, caption;
                JSONObject jo = JSC.strToJOb(new String(response));
                JSONArray ja = JSC.strToJAr(JSC.getJString(jo, "data"));
                if (ja.length() != 0) {
                    jo = JSC.strToJOb(JSC.jArrToString(ja, 0));
                    JSONObject joLoc = JSC.strToJOb(JSC.getJString(jo, "location")),
                            joImg = JSC.strToJOb(JSC.getJString(jo, "images"));
                    JSONObject joImgLowRes = JSC.strToJOb(JSC.getJString(joImg, "low_resolution"));
                    JSONObject joImgLowThumb = JSC.strToJOb(JSC.getJString(joImg, "thumbnail"));

                    JSONObject joUser = JSC.strToJOb(JSC.getJString(jo, "user"));
                    JSONObject joCap = JSC.strToJOb(JSC.getJString(jo, "caption"));

                    u = new LatLng(Double.parseDouble(JSC.getJString(joLoc, "latitude")),
                            Double.parseDouble(JSC.getJString(joLoc, "longitude")));
                    if (joUser != null) {
                        title = JSC.getJString(joUser, "full_name") + " (" + JSC.getJString(joUser, "username") + ")";
                    } else {
                        title = "UNKNOWN";
                        Log.e("JO", jo.toString() + ":");
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

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        });
    }

    public void getBitmap(String url, final Marker marker) {
        MyCache.with(this).loadImage(url, new MyCache.LoadImage() {
            @Override
            public void onLoad(Bitmap resource) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(Constants.addWhiteBorder(resource)));
            }
        });
    }


    @Override
    public void onClick(View v) {

    }

}
