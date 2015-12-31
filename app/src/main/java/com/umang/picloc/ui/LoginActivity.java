package com.umang.picloc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.umang.picloc.R;
import com.umang.picloc.instagram.Instagram;
import com.umang.picloc.instagram.InstagramSession;
import com.umang.picloc.instagram.InstagramUser;
import com.umang.picloc.utility.Constants;

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
        mInstagram = new Instagram(this, getString(R.string.instagram_client_id), getString(R.string.instagram_client_secret), Constants.CALLBACK_URL);
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
            Toast.makeText(LoginActivity.this, getString(R.string.welcom_message, user.fullName), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            overridePendingTransition(0, 0);
            finish();
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
