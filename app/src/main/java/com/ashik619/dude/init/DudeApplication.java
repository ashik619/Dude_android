package com.ashik619.dude.init;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Switch;

import com.ashik619.dude.MainActivity;

import com.ashik619.dude.ShowMapActivity;
import com.ashik619.dude.helper.PrefHandler;
import com.ashik619.dude.helper.SharedPrefConstant;
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

    public static PrefHandler getLocalPrefInstance() {
        return localStorageHandler;
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
                String phone_number = data.getString("phone_number");
                switch (type){
                    case 0:
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("phone_number",phone_number);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case 1:
                        String lat = data.getString("lat");
                        String lng = data.getString("lng");
                        System.out.println("loc"+lat+lng);
                        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                        intent1.putExtra("type",type);
                        intent1.putExtra("phone_number",phone_number);
                        intent1.putExtra("lat",lat);
                        intent1.putExtra("lng",lng);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);
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
    }




}
