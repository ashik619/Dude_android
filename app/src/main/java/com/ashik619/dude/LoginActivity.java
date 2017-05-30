package com.ashik619.dude;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.ashik619.dude.custom_views.CustomEditText;
import com.ashik619.dude.init.DudeApplication;
import com.ashik619.dude.io.HttpServerBackend;
import com.ashik619.dude.io.RestAdapter;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;
import com.splunk.mint.Mint;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.phoneNumber)
    CustomEditText phoneNumber;
    @BindView(R.id.nameText)
    CustomEditText nameText;
    @BindView(R.id.continueButton)
    RelativeLayout continueButton;
    @BindView(R.id.mainLayout)
    LinearLayout mainLayout;
    @BindView(R.id.loadingLayout)
    RelativeLayout loadingLayout;
    private String phoneNum = null;
    private String name;
    private String playerId = null;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(LoginActivity.this, "74083949");
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        DudeApplication application = (DudeApplication)getApplication();
        mTracker = application.getDefaultTracker();

        if (phoneNum != null) {
            phoneNumber.setText(phoneNum);
        }
        getPlayerId();
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });
    }

    void getPlayerId() {
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                playerId = userId;

            }
        });
    }

    void createUser() {
        name = nameText.getText().toString();
        phoneNum = phoneNumber.getText().toString();
        if (name.matches("")) {
            showSnackBar("Enter Name",false);
        } else if (phoneNum.matches("")) {
            showSnackBar("Enter Phone Number",false);
        } else if (phoneNum.length() < 10) {
            showSnackBar("Enter Valid Phone Number",false);
        } else {
            createUserApiCall();
        }
    }

    void createUserApiCall() {
        JsonObject input = new JsonObject();
        input.addProperty("phone_number", phoneNum);
        input.addProperty("player_id", playerId);
        input.addProperty("name", name);
        Call<JsonObject> call = new RestAdapter().getRestInterface().createUser(input);
        mainLayout.setVisibility(View.INVISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        new HttpServerBackend(LoginActivity.this).getData(call, new HttpServerBackend.ResponseListener() {
            @Override
            public void onReturn(boolean success, JsonObject data, int message) {
                super.onReturn(success, data, message);
                loadingLayout.setVisibility(View.INVISIBLE);
                System.out.println(message);
                if (success) {
                    mTracker.setScreenName("Login Activity");
                    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                    if (data.get("success").getAsBoolean()) {
                        if (data.get("msg").getAsInt() == 0) {
                            saveUserData();
                            startSelectActivity();
                        }
                    } else {
                        if (data.get("msg").getAsInt() == 1) {
                            saveUserData();
                            startSelectActivity();
                        }else showSnackBar("This number is alredy registered in another device",true);
                    }

                } else {
                    showSnackBar("Please Check Network Connection",true);
                }
            }
        });
    }

    void startSelectActivity() {
        Intent intent = new Intent(LoginActivity.this, SelectActivity.class);
        startActivity(intent);
        finish();
    }

    void saveUserData() {
        DudeApplication.getLocalPrefInstance().setName(name);
        DudeApplication.getLocalPrefInstance().setNumber(phoneNum);
        DudeApplication.getLocalPrefInstance().setPlayerId(playerId);
    }
    void showSnackBar(String message, final boolean flag){
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), message , Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (flag) {
                            recreate();
                        }
                    }
                });
        snackbar.show();
    }
}
