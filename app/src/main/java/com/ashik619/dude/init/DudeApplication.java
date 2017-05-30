package com.ashik619.dude.init;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Switch;

import com.ashik619.dude.MainActivity;

import com.ashik619.dude.R;
import com.ashik619.dude.ShowMapActivity;
import com.ashik619.dude.helper.PrefHandler;
import com.ashik619.dude.helper.SharedPrefConstant;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ashik619 on 09-03-2017.
 */
public class DudeApplication extends Application {

    public static PrefHandler localStorageHandler;
    private Tracker mTracker;

    public static PrefHandler getLocalPrefInstance() {
        return localStorageHandler;
    }
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new DudeNotificationOpenedHandler())
                .init();
        if (localStorageHandler == null) {
            localStorageHandler = new PrefHandler(getApplicationContext());
        }

        // Call syncHashedEmail anywhere in your app if you have the user's email.
        // This improves the effectiveness of OneSignal's "best-time" notification scheduling feature.
        // OneSignal.syncHashedEmail(userEmail);
    }
    public class DudeNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            OSNotificationAction.ActionType actionType = result.action.type;
            JSONObject data = result.notification.payload.additionalData;
            System.out.println("notification data "+data.toString());
            try {
                int type = data.getInt("type");
                switch (type){
                    case 0:
                        String phone_number = data.getString("phone_number");
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("phone_number",phone_number);
                        intent.putExtra("type",type);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case 1:
                        String phone_number1 = data.getString("phone_number");
                        String lat = data.getString("lat");
                        String lng = data.getString("lng");
                        System.out.println("loc"+lat+lng);
                        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                        intent1.putExtra("type",type);
                        intent1.putExtra("phone_number",phone_number1);
                        intent1.putExtra("lat",lat);
                        intent1.putExtra("lng",lng);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);
                        break;
                    case 2:
                        showPlayStoreLink();
                        break;

                }

            }catch (JSONException e){
                e.printStackTrace();
            }
            String customKey;

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
            }

            if (actionType == OSNotificationAction.ActionType.ActionTaken)
                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

        }
        void showPlayStoreLink(){
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }

}
