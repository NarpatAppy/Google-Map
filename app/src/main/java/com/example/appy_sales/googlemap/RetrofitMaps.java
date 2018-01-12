package com.example.appy_sales.googlemap;

import com.example.appy_sales.googlemap.model.Example;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Appy-Sales on 11-01-2018.
 */

public interface RetrofitMaps {
  /*  * Retrofit get our annotation with our URL
    * and our method will return the details
    * */

    @GET("api/directions/json?key=AIzaSyC22GfkHu9FdgT9SwdCWMwKX1a4aohGifM")
    Call<Example> getDistanceDuration(@Query("units") String units, @Query("origin") String origin, @Query("destination") String destination, @Query("mode") String mode);

}
