package com.ashik619.dude;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowMapActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener {
    private static String TAG = "MAP";
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.loadingLayout)
    RelativeLayout loadingLayout;
    private LatLng camera = new LatLng(37.782437, -122.4281893);
    private LatLng origin = null;
    private LatLng destination = null;
    double lat;
    double lng;
    Location loc = null;
    Location gpsLocation = null;
    Location networkLocation = null;

    double clatitude = 0;
    double clongitude = 0;
    private boolean checkGPS = false;
    private boolean checkNetwork = false;
    LocationManager locationManager;
    private final int TWO_MINUTES = 1000 * 60 * 2;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;


    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;


    private GoogleMap mMap;

    public ShowMapActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        ButterKnife.bind(this);
        getCurrentLocation();
        Intent intent = getIntent();
        lat = Double.parseDouble(intent.getStringExtra("lat"));
        lng = Double.parseDouble(intent.getStringExtra("lng"));
        destination = new LatLng(lat,lng);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



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
        if(origin != null && destination != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13));
            mMap.addMarker(new MarkerOptions().position(origin));
            mMap.addMarker(new MarkerOptions().position(destination));
            drawDirections();
        }

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    void drawDirections() {
        GoogleDirection.withServerKey("AIzaSyAOyBsRWUUxfspMT2Oox-7ngzK-7bQAFbc")
                .from(origin)
                .to(destination)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        loadingLayout.setVisibility(View.INVISIBLE);
                        Log.e(TAG, "direction ok");
                        if (direction.isOK()) {

                            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                            mMap.addPolyline(DirectionConverter.createPolyline(ShowMapActivity.this, directionPositionList, 5, Color.RED));


                        } else {
                            Log.e(TAG, "direction not ok");
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        loadingLayout.setVisibility(View.INVISIBLE);
                        Log.e(TAG, "direction failure");
                    }
                });
    }
    public void getCurrentLocation() {
        try {
            Log.e(TAG,"finding loc");
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


            checkGPS = locationManager
                    .isProviderEnabled(locationManager.GPS_PROVIDER);
            checkNetwork = locationManager
                    .isProviderEnabled(locationManager.NETWORK_PROVIDER);

            if (checkGPS || checkNetwork) {
                try {
                    if (checkGPS) {
                        Log.e(TAG,"gps enabled");
                        locationManager.requestLocationUpdates(
                                locationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        gpsLocation = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    if (checkNetwork) {
                        Log.e(TAG,"network enabled");

                        locationManager.requestLocationUpdates(
                                locationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        networkLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }

                }catch (SecurityException e){
                    e.printStackTrace();
                }
                if (gpsLocation != null && networkLocation != null) {
                    Log.e(TAG,"both loc not null");

                    loc = getBetterLocation(gpsLocation, networkLocation);
                } else if (gpsLocation != null) {
                    Log.e(TAG,"gps loc not null");

                    loc = gpsLocation;
                } else if (networkLocation != null) {
                    Log.e(TAG,"network loc not null");

                    loc = networkLocation;
                }
                if (loc != null) {
                    Log.e(TAG,"loc not null");

                    clatitude = loc.getLatitude();
                    clongitude = loc.getLongitude();
                    origin = new LatLng(clatitude,clongitude);
                }else {
                    Log.e(TAG,"loc null");
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    protected Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return newLocation;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return newLocation;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
