package com.umang.picloc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.umang.picloc.instagram.Instagram;
import com.umang.picloc.instagram.InstagramSession;
import com.umang.picloc.instagram.InstagramUser;

/**
 * Created by umang on 7/10/15.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private InstagramSession mInstagramSession;
    private Instagram mInstagram;

    Button bLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
    }

    public void initialize() {
        mInstagram = new Instagram(this, Constants.CLIENT_ID, Constants.CLIENT_SECRET, Constants.CALLBACK_URL);
        mInstagramSession = mInstagram.getSession();
        if (mInstagramSession.isActive()) {

        } else {
            bLogin = ((Button) findViewById(R.id.buttonLogin));
            bLogin.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLogin:
                mInstagram.authorize(mAuthListener);
                break;
        }
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private Instagram.InstagramAuthListener mAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(InstagramUser user) {
            finish();
            Log.e("LoggedIn", "Fullname-" + user.fullName + ":id-" + user.id +
                    ":profile-" + user.profilPicture + ":username-" + user.username + ":ACCESS_TOKEN" + user.accessToken);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }

        @Override
        public void onError(String error) {
            showToast(error);
        }

        @Override
        public void onCancel() {

        }
    };
}
