package com.ashik619.dude;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.ashik619.dude.custom_views.IconTextView;
import com.ashik619.dude.init.DudeApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.splunk.mint.Mint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static String TAG = "MAP";
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.loadingLayout)
    RelativeLayout loadingLayout;
    @BindView(R.id.messageText)
    IconTextView messageText;
    @BindView(R.id.msgLayout)
    RelativeLayout msgLayout;
    @BindView(R.id.closeButton)
    ImageButton closeButton;
    private LatLng origin = null;
    private LatLng destination = null;
    double lat;
    double lng;
    Location loc = null;
    GoogleApiClient mGoogleApiClient = null;
    boolean firstLocationFlag = false;
    int count = 0;
    boolean flag = false;


    double clatitude = 0;
    double clongitude = 0;
    LocationRequest mLocationRequest = null;
    private static final long INTERVAL = 1000 * 3;
    private static final long FASTEST_INTERVAL = 1000 * 2;


    private GoogleMap mMap;
    private String friendName = null;
    SupportMapFragment mapFragment;
    MarkerOptions markerOptions;
    Marker orginMarker;
    LocationManager locationManager;

    public ShowMapActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(ShowMapActivity.this, "74083949");
        setContentView(R.layout.activity_show_map);
        ButterKnife.bind(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Intent intent = getIntent();
        lat = Double.parseDouble(intent.getStringExtra("lat"));
        lng = Double.parseDouble(intent.getStringExtra("lng"));
        friendName = intent.getStringExtra("friend_name");
        saveTask();
        if (!isLocationEnabled()) {
            showTurnOnLoacationDialog();
        } else {
            locationIsOn();
        }
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(ShowMapActivity.this);
                dialog.setCancelable(true);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.close_dialog);
                dialog.show();
                RelativeLayout okbutton = (RelativeLayout) dialog.findViewById(R.id.okbutton);
                okbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeTask();
                    }
                });
            }
        });
    }
    void closeTask(){
        DudeApplication.getLocalPrefInstance().setTask(null);
        Intent intent = new Intent(ShowMapActivity.this, SelectActivity.class);
        startActivity(intent);
        finish();
    }



    void saveTask() {
        JSONObject task = new JSONObject();
        try {
            task.put("name", friendName);
            task.put("lat", String.valueOf(lat));
            task.put("lng", String.valueOf(lng));
            DudeApplication.getLocalPrefInstance().setTask(task.toString());
        } catch (JSONException e) {
        }
    }

    void locationIsOn() {
        flag = true;
        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        destination = new LatLng(lat, lng);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
    }

    void buildGoogleApiClient() {
        Log.d(TAG, "Bulding");
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

    public boolean isLocationEnabled(){
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return true;
        }else{
            return false;
        }
    }


    void showTurnOnLoacationDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.location_dialog);
        dialog.show();
        RelativeLayout okbutton = (RelativeLayout) dialog.findViewById(R.id.okbutton);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //LatLngBounds latLngBounds = new LatLngBounds(origin,destination);
        //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 13));
        if (origin != null && destination != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 13));
            markerOptions = new MarkerOptions().position(origin).title("You");
            orginMarker = mMap.addMarker(markerOptions);
            mMap.addMarker(new MarkerOptions().position(destination).title(friendName)).showInfoWindow();
            drawDirections();
        }

    }

    void drawDirections() {
        GoogleDirection.withServerKey("AIzaSyAOyBsRWUUxfspMT2Oox-7ngzK-7bQAFbc")
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        loadingLayout.setVisibility(View.INVISIBLE);
                        Log.e(TAG, direction.getStatus());
                        if (direction.isOK()) {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);

                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(ShowMapActivity.this, directionPositionList, 5, Color.RED);
                            mMap.addPolyline(polylineOptions);


                            Info distanceInfo = leg.getDistance();
                            String distance = distanceInfo.getText();
                            Log.d(TAG, distance);
                            showMessage(distance);
                        } else {
                          //  Log.e(TAG, "direction not ok");
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        loadingLayout.setVisibility(View.INVISIBLE);
                      //  Log.e(TAG, "direction failure");
                    }
                });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
       // Log.d(TAG, "apiclient connec");
        startLocationUpdates();

    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, ShowMapActivity.this);
          //  Log.d(TAG, "Location update started ..............: ");
        } catch (SecurityException e) {

        }
    }


    @Override
    public void onConnectionSuspended(int i) {
       // Log.d(TAG, "apiclient connec suspende");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
       // Log.d(TAG, "apiclient connec failed");
        recreate();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (count >= 2) {
            if (!firstLocationFlag) {
                firstLocationFlag = true;
                if (location != null) {
                    clatitude = location.getLatitude();
                    clongitude = location.getLongitude();
                  //  Log.d(TAG, "loc not null" + lat + lng);
                    origin = new LatLng(clatitude, clongitude);
                    mapFragment.getMapAsync(ShowMapActivity.this);
                } else {
                   // Log.d(TAG, "loc  null");
                    recreate();
                }
            } else {
                orginMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        } else count++;
    }

    void showMessage(String distance) {
        msgLayout.setVisibility(View.VISIBLE);
        messageText.setText("You are just " + distance + " away from " + friendName);
        msgLayout.postDelayed(new Runnable() {
            public void run() {
                msgLayout.setVisibility(View.INVISIBLE);
            }
        }, 7000);

    }
}
