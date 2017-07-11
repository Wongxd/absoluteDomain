package com.wongxd.absolutedomain.Retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by jiangzehui on 16/10/17.
 */
public interface HttpService {


    @GET("index?key=9e05423f7ac6acf6d0dce3425c4ea9fe")
    Call<String> Get_news(@Query("type") String type);


    @GET("neihan/stream/mix/v1/?mpic=1&webp=1&essence=1&content_type=-104&message_cursor=-1")
    Call<String> Get_video();




}
