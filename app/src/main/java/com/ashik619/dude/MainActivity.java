package com.ashik619.dude;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.ashik619.dude.helper.GpsTracker;
import com.ashik619.dude.init.DudeApplication;
import com.ashik619.dude.io.HttpServerBackend;
import com.ashik619.dude.io.RestAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.continueButton)
    RelativeLayout sendButton;
    private int notificationType = 10;
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.loadingLayout)
    RelativeLayout loadingLayout;
    private String lat = null;
    private String lng = null;
    private String friendPhoneNumber = null;
    private String friendName = null;
    private String friendPlayerId = null;
    private String myPhoneNum = null;
    int type = 10;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mainLayout.setVisibility(View.INVISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        myPhoneNum = DudeApplication.getLocalPrefInstance().getNumber();
        Intent intent = getIntent();
        type = intent.getIntExtra("type",10);
        friendPhoneNumber = intent.getStringExtra("phone_number");
        switch (type){
            case 0:
                getFriendDetails();
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendLocationNottification();
                    }
                });
                break;
            case 1:
                getFriendDetails();
        }

    }

    void getFriendDetails() {
        Call<JsonObject> call = new RestAdapter().getRestInterface().getuser(friendPhoneNumber);
        new HttpServerBackend(MainActivity.this).getData(call, new HttpServerBackend.ResponseListener() {
            @Override
            public void onReturn(boolean success, JsonObject data, String message) {
                super.onReturn(success, data, message);
                loadingLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
                if (success) {
                    if (data.get("success").getAsBoolean()) {
                        JsonArray userArray = data.getAsJsonArray("user");
                        JsonObject user = userArray.get(0).getAsJsonObject();
                        friendPlayerId = user.get("playerId").getAsString();
                        friendName = user.get("name").getAsString();


                    }
                } else {

                }
            }
        });
    }

    void sendLocationNottification() {
        GpsTracker gpsTracker = new GpsTracker(getApplicationContext());
        lng = String.valueOf(gpsTracker.getLongitude());
        lat = String.valueOf(gpsTracker.getLatitude());
        System.out.println("map"+lat+lng);

        try {
            String message = "Hey " + friendName + " I'm here";
            JSONObject contents = new JSONObject();
            contents.put("en", message);
            JSONObject data = new JSONObject();
            data.put("type", 1);
            data.put("phone_number", myPhoneNum);
            data.put("lat", lat);
            data.put("lng", lng);
            JSONArray players = new JSONArray();
            players.put(friendPlayerId);


            JSONObject root = new JSONObject();
            root.put("contents", contents);
            root.put("data", data);
            root.put("include_player_ids", players);

            OneSignal.postNotification(root,
                    new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Log.i("OneSignalExample", "postNotification Success: " + response.toString());
                        }

                        @Override
                        public void onFailure(JSONObject response) {
                            Log.e("OneSignalExample", "postNotification Failure: " + response.toString());
                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
