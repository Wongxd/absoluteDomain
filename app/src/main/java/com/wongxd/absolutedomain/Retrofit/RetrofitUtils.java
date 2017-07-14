package com.wongxd.absolutedomain.Retrofit;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitUtils {
    private static final class Holder {
        private static class LocalCookieJar implements CookieJar {
            List<Cookie> cookies;

            @Override
            public List<Cookie> loadForRequest(@NonNull HttpUrl arg0) {
                if (cookies != null)
                    return cookies;
                return new ArrayList<>();
            }

            @Override
            public void saveFromResponse(@NonNull HttpUrl arg0, @NonNull List<Cookie> cookies) {
                this.cookies = cookies;
            }

        }

        private static final OkHttpClient sOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(getInterceptor())
                .cookieJar(new LocalCookieJar())
                .build();
        private static final Retrofit INSTANCE_GSON = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(sOkHttpClient)
                .baseUrl("https://www.google.com")
                .build();
        private static final Retrofit INSTANCE_STRING = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(sOkHttpClient)
                .baseUrl("https://www.google.com")
                .build();
        private static HttpLoggingInterceptor getInterceptor() {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            return interceptor;
        }
    }

    public static Retrofit getGsonInstance() {
        return Holder.INSTANCE_GSON;
    }

    public static Retrofit getStringInstance() {
        return Holder.INSTANCE_STRING;
    }
}
