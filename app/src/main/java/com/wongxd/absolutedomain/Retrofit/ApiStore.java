package com.wongxd.absolutedomain.Retrofit;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

public interface ApiStore {
//    @GET
//    Observable<ClassifyBean> tngou_classify(@Url String url);
//
//    @GET
//    Observable<TGBean> tngou_image(@Url String url);
//
//    @GET
//    Observable<List<BooruBean>> booru(@Url String url);
//
//    @GET
//    Observable<List<DonmaiBean>> danbooru(@Url String url);
//
//    @GET
//    Observable<GankBean> gank(@Url String url);
//
//    @GET
//    Observable<BingBean> bing(@Url String url);
//
//    @GET
//    Observable<JuZiBean> hitokoto(@Url String url);

    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 UBrowser/6.1.2716.5 Safari/537.36"})
    @GET
    Observable<String> getString(@Url String url);

}
