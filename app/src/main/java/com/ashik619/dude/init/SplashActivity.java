package com.ashik619.dude.init;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ashik619.dude.LoginActivity;
import com.ashik619.dude.R;
import com.ashik619.dude.SelectActivity;
import com.ashik619.dude.ShowMapActivity;
import com.splunk.mint.Mint;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {
    private static int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {  android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(SplashActivity.this, "74083949");
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        } else {
            permissionGranted();
        }


    }
    void checkPermissions(){

        if(!hasPermissions(SplashActivity.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(SplashActivity.this, PERMISSIONS, PERMISSION_ALL);
        } else {
            permissionGranted();
        }

    }
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == PERMISSION_ALL){

            //If permission is granted
            boolean flag = false;
            for(int grantResult : grantResults){
                if(grantResult != PackageManager.PERMISSION_GRANTED){
                  //  System.out.println("permission not granted");
                    requestPermissionAgain();
                    flag = false;
                } else {
                   // System.out.println("permission granted");
                    flag = true;
                }
            }
            if(flag){
                permissionGranted();
            }

        }
    }
    void requestPermissionAgain(){
        ActivityCompat.requestPermissions(SplashActivity.this, PERMISSIONS, PERMISSION_ALL);
    }
    void permissionGranted(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(DudeApplication.getLocalPrefInstance().getNumber()!= null && DudeApplication.getLocalPrefInstance().getNumber()!= null&& DudeApplication.getLocalPrefInstance().getName()!= null){
                    String task = DudeApplication.getLocalPrefInstance().getTask();
                    if (task != null){
                        startTask(task);
                    }else {
                        Intent intent = new Intent(SplashActivity.this, SelectActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                //overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            }
        }, 1000);
    }
    void startTask(String task){
        try {
            JSONObject taskObject = new JSONObject(task);
            Intent intent = new Intent(SplashActivity.this, ShowMapActivity.class);
            intent.putExtra("lat",taskObject.getString("lat"));
            intent.putExtra("lng",taskObject.getString("lng"));
            intent.putExtra("friend_name",taskObject.getString("name"));
            startActivity(intent);
            finish();
        }catch (JSONException e){
            e.printStackTrace();
        }

    }
}
