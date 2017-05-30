package com.ashik619.dude;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ashik619.dude.custom_views.IconTextView;
import com.ashik619.dude.init.DudeApplication;
import com.ashik619.dude.io.HttpServerBackend;
import com.ashik619.dude.io.RestAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;
import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    @BindView(R.id.continueButton)
    RelativeLayout sendButton;
    @BindView(R.id.reqMsg)
    IconTextView reqMsg;
    @BindView(R.id.requestedLayout)
    LinearLayout requestedLayout;
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
    GoogleApiClient mGoogleApiClient = null;
    Location loc = null;
    private static String TAG = "MAP";
    private boolean flag = false;
    private boolean apiSuccessFlag = false;
    LocationRequest mLocationRequest = null;
    private static final long INTERVAL = 1000 * 3;
    private static final long FASTEST_INTERVAL = 1000 * 2;
    int count = 0;
    LocationManager locationManager;
    private  String userName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(MainActivity.this, "74083949");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loadingLayout.setVisibility(View.VISIBLE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Intent intent = getIntent();
        type = intent.getIntExtra("type", 10);
        friendPhoneNumber = intent.getStringExtra("phone_number");
        myPhoneNum = DudeApplication.getLocalPrefInstance().getNumber();
        userName = DudeApplication.getLocalPrefInstance().getName();
        switch (type) {
            case 0:
                if(!isLocationEnabled()){
                    showTurnOnLoacationDialog();
                }else {
                    locationIsOn();
                }
                break;
            case 1:
                lat = intent.getStringExtra("lat");
                lng = intent.getStringExtra("lng");
                getFriendDetails();
                break;
        }


    }

    void locationIsOn(){
        flag = true;
        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        getFriendDetails();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLocationNottification();
            }
        });


    }

    void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    void getFriendDetails() {
        Call<JsonObject> call = new RestAdapter().getRestInterface().getuser(friendPhoneNumber);
        new HttpServerBackend(MainActivity.this).getData(call, new HttpServerBackend.ResponseListener() {
            @Override
            public void onReturn(boolean success, JsonObject data, int message) {
                super.onReturn(success, data, message);
                if(loc!=null) {
                    loadingLayout.setVisibility(View.INVISIBLE);
                    if (type == 0) {
                        mainLayout.setVisibility(View.VISIBLE);
                    }
                }
                if (success) {
                    if (data.get("success").getAsBoolean()) {
                        JsonArray userArray = data.getAsJsonArray("user");
                        JsonObject user = userArray.get(0).getAsJsonObject();
                        friendPlayerId = user.get("playerId").getAsString();
                        friendName = user.get("name").getAsString();
                        apiSuccessFlag = true;
                        if (type == 1) {
                            Intent intent = new Intent(MainActivity.this, ShowMapActivity.class);
                            intent.putExtra("lat", lat);
                            intent.putExtra("lng", lng);
                            intent.putExtra("friend_name", friendName);
                            startActivity(intent);
                            finish();
                        }
                    }
                } else {
                    showFailureMessage();
                }
            }
        });
    }

    void sendLocationNottification() {
        if (apiSuccessFlag = true && loc != null) {
            mainLayout.setVisibility(View.INVISIBLE);
            loadingLayout.setVisibility(View.VISIBLE);
            try {
                String message = "Hey " + friendName + " I'm here : "+userName;
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
                               // Log.i("OneSignalExample", "postNotification Success: " + response.toString());
                                showSuccessMessage();

                            }

                            @Override
                            public void onFailure(JSONObject response) {
                               // Log.e("OneSignalExample", "postNotification Failure: " + response.toString());
                                showSnackBar("Something went wrong please try again",true);
                            }
                        });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void showSuccessMessage(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingLayout.setVisibility(View.INVISIBLE);
                mainLayout.setVisibility(View.GONE);
                requestedLayout.setVisibility(View.VISIBLE);
                reqMsg.setText("We have notified "+friendName);
            }
        });

    }

    void showFailureMessage(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
                showSnackBar("Please Check Network Connection",true);
            }
        });
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
       // Log.d(TAG, "apiclient connec");
        startLocationUpdates();

    }
    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, MainActivity.this);
           // Log.d(TAG, "Location update started ..............: ");
        }catch (SecurityException e){

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "apiclient connec suspende");
    }
    void showTurnOnLoacationDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.location_dialog);
        dialog.show();
        RelativeLayout okbutton = (RelativeLayout) dialog.findViewById(R.id.okbutton);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                dialog.dismiss();
                startActivityForResult(intent,1);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                handleResult();
                break;
        }
    }
    void handleResult(){
        if(isLocationEnabled()){
            locationIsOn();
        }else showTurnOnLoacationDialog();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
       // Log.d(TAG, "apiclient connec failed");

    }

   public boolean isLocationEnabled(){
       if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
           return true;
       }else{
           return false;
       }
   }

    @Override
    public void onLocationChanged(Location location) {
        if(count >= 3) {
            loc = location;
            if (loc != null) {
                lat = String.valueOf(loc.getLatitude());
                lng = String.valueOf(loc.getLongitude());
                loadingLayout.setVisibility(View.INVISIBLE);
                mainLayout.setVisibility(View.VISIBLE);
                stopLocationUpdates();
            }
        }else count++;

    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        //Log.d(TAG, "Location update stopped .......................");
    }

}
