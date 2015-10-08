package com.umang.picloc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.umang.picloc.instagram.Instagram;
import com.umang.picloc.instagram.InstagramSession;
import com.umang.picloc.instagram.InstagramUser;
import com.umang.picloc.utility.JSC;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean gotLocation = false;
    GoogleMap map;

    double lastLat = -1000;
    double lastLong = -1000;
    ProgressBar pbLoading;

    private InstagramSession mInstagramSession;
    private Instagram mInstagram;
    private InstagramUser instagramUser;

    MarkerOptions tempMarker;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
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
                    client.get("https://api.instagram.com/v1/locations/search?lat=" + lastLat + "&lng=" + lastLong
                            + "&DISTANCE=500&access_token=" + instagramUser.accessToken, new AsyncHttpResponseHandler() {

                        @Override
                        public void onStart() {
                            // called before request is started
                        }

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
                        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            // called when request is retried
                        }
                    });
                }

            }

        });
    }

    private void getLocationImage(JSONObject jo, final int position) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://api.instagram.com/v1/locations/" + JSC.getJString(jo, "id") +
                "/media/recent?access_token=" + instagramUser.accessToken, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

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
                    JSONObject joLoc = JSC.strToJOb(JSC.getJString(jo, "location"));
                    JSONObject joImg = JSC.strToJOb(JSC.getJString(jo, "images"));
                    joImg = JSC.strToJOb(JSC.getJString(joImg, "thumbnail"));
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
                            i.putExtra("IMAGE_DATA", jo.toString());
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
                    hashMap.put(marker, jo);
                    getBitmap(JSC.getJString(joImg, "url"), marker);
                } else {
                    Log.e("ARRAY", jo.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    public void getBitmap(String url, final Marker marker) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new FileAsyncHttpResponseHandler(getBaseContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, File response) {
                // Do something with the file `response`
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(Constants.addWhiteBorder(bitmap)));
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Log.e("image", "err");
            }
        });
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
