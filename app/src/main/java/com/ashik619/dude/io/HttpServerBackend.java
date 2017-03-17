package com.ashik619.dude.io;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.ashik619.dude.init.DudeApplication;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ashik619 on 11-03-2017.
 */
public class HttpServerBackend {
    String message = "Some error occurred, Try again later";
    Integer errorCode = -10;
    ConnectivityManager connectivityManager;
    boolean connected;
    Context context;

    public HttpServerBackend(Context context) {
        this.context = context;
    }

    public void getData(final Call<JsonObject> call, final ResponseListener back) {

        if (isOnline()) {
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

//                    Ln.i("Request_",call.request().body().+"");

                    if (response.code() == 200 || response.code() == 201) {

                        try {
                            Log.i("Response_", response.body().toString() + "");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        back.onReturn(true, response.body().getAsJsonObject(), "");

                    } else {
                        errorCode = response.code();
                        try {
                            Log.i("Response_", response.errorBody().string() + "");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        back.onReturn(false, null, handleErrorByCode(response.errorBody()));
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    //handleIt

                    back.onReturn(false, null, message);
                    try {
                        Log.i("Response_", t.toString() + "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            errorCode = -50;
            System.out.println("No internet");
            //handleErrorByCode(null);
        }
    }

    private String handleErrorByCode(ResponseBody responseBody) {

        JSONObject jsonObject;
        try {

            switch (errorCode) {

                //response body is null for this case
                case -50: {

                    //message = context.getResources().getString(R.string.error_connecting);

                    break;
                }


                default: {
                    message = "Some random error, Error code: " + errorCode;
                }
            }
        } catch (Exception e) {
            Log.i("DebugInfo", "RetrofitErrorHandler");
            errorCode = -10;
        }

        return message;

    }

    private void showUpdateDialog(boolean b) {

    }

    public static class ResponseListener {
        public ResponseListener() {
        }

        public void onReturn(boolean success, JsonObject data, String message) {
        }

        public void updateProgress(float x) {
        }
    }
    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }

}
