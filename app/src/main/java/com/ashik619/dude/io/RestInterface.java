package com.ashik619.dude.io;

import com.google.gson.JsonObject;


import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by ashik619 on 11-03-2017.
 */
public interface RestInterface {

    @POST("/user/create")
    Call<JsonObject> createUser(@Body JsonObject user_input);

    @GET("user/getuser")
    Call<JsonObject> getuser(@Query("phone_number") String phone_number);


}
