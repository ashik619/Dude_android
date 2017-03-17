package com.ashik619.dude;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ashik619.dude.custom_views.CustomEditText;
import com.ashik619.dude.init.DudeApplication;
import com.ashik619.dude.io.HttpServerBackend;
import com.ashik619.dude.io.RestAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

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
    private String phoneNum = null;
    private String name;
    private String playerId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
       // phoneNum = getPhoneNumber();
        if(phoneNum!= null){
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

    String getPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();
        return mPhoneNumber;
    }
    void getPlayerId(){
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                playerId = userId;

            }
        });
    }
    void createUser(){
        name = nameText.getText().toString();
        phoneNum = phoneNumber.getText().toString();
        if(name.matches("")){
            /*Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), "Please verify email", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Skip", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });*/
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();


        }else if(phoneNum.matches("")){

        }else if(phoneNum.length()<10){

        }else {
            createUserApiCall();
        }
    }

    void createUserApiCall(){
        JsonObject input = new JsonObject();
        input.addProperty("phone_number", phoneNum);
        input.addProperty("player_id", playerId);
        input.addProperty("name",name);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setTitle("loading");
        Call<JsonObject> call = new RestAdapter().getRestInterface().createUser(input);
        new HttpServerBackend(LoginActivity.this).getData(call, new HttpServerBackend.ResponseListener() {
            @Override
            public void onReturn(boolean success, JsonObject data, String message) {
                super.onReturn(success, data, message);
                progressDialog.dismiss();
                System.out.println(message);
                if (success) {
                    if(data.get("success").getAsBoolean()){
                        if (data.get("msg").getAsInt() == 0){
                            saveUserData();
                            startSelectActivity();
                        }
                    }else {
                        if (data.get("msg").getAsInt() == 1){
                            saveUserData();
                            startSelectActivity();
                        }
                    }

                } else {
                }
            }
        });
    }
    void startSelectActivity(){
        Intent intent = new Intent(LoginActivity.this,SelectActivity.class);
        startActivity(intent);
        finish();
    }
    void saveUserData(){
       DudeApplication.getLocalPrefInstance().setName(name);
        DudeApplication.getLocalPrefInstance().setNumber(phoneNum);
        DudeApplication.getLocalPrefInstance().setPlayerId(playerId);
    }
}
