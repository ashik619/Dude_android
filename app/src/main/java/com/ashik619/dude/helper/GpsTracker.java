package com.ashik619.dude.helper;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ashik619 on 13-03-2017.
 */
public class GpsTracker implements LocationListener{
    private Context mContext;
    private String TAG = "map";
    Location loc = null;
    Location gpsLocation = null;
    Location networkLocation = null;

    double latitude;
    double longitude;
    private boolean checkGPS = false;
    private boolean checkNetwork = false;
    LocationManager locationManager;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;


    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private final int TWO_MINUTES = 1000 * 60 * 2;

    public GpsTracker(Context mContext) {
        this.mContext = mContext;
        getLocation();
    }
    /*
    void getLocation(){
        try {
            locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            checkGPS = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (checkGPS) {
                Toast.makeText(mContext, "GPS", Toast.LENGTH_SHORT).show();
                if (loc == null) {
                    try {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            loc = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (loc != null) {
                                latitude = loc.getLatitude();
                                longitude = loc.getLongitude();
                            }
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }


                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/



    public void getLocation() {
        try {
            Log.e(TAG,"finding loc");
            locationManager = (LocationManager) mContext
                    .getSystemService(Context.LOCATION_SERVICE);


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

                    latitude = loc.getLatitude();
                    longitude = loc.getLongitude();
                    System.out.println("loc"+latitude+longitude);
                }else {
                    Log.e(TAG,"loc null");
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public double getLongitude() {
        if (loc != null) {
            longitude = loc.getLongitude();
        }
        return longitude;
    }

    public double getLatitude() {
        if (loc != null) {
            latitude = loc.getLatitude();
        }
        return latitude;
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
