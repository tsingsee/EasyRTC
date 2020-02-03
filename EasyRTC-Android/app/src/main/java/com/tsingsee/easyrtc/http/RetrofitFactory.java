package com.tsingsee.easyrtc.http;

import android.util.Log;

import com.tsingsee.easyrtc.RTCApplication;
import com.tsingsee.easyrtc.tool.SharedHelper;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {
    // 声明缓存地址和大小
    private static Cache getCache() {
        //cache url
        File httpCacheDirectory = new File(RTCApplication.getContext().getCacheDir(), "responses");
        int cacheSize = 30 * 1024 * 1024; // 30 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        return cache;
    }

    public static String getBaseURL() {
        SharedHelper helper = new SharedHelper(RTCApplication.getContext());
        return helper.getURL();
    }

    // Retrofit是基于OkHttpClient的，可以创建一个OkHttpClient进行一些配置
    private static OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(new XInterceptor.CommonLog())
            .addInterceptor(new XInterceptor.Token())
            .addInterceptor(new XInterceptor.Retry())
            .addInterceptor(new XInterceptor.CommonNoNetCache())
            .addNetworkInterceptor(new XInterceptor.CommonNetCache())
            // 这里可以添加一个HttpLoggingInterceptor，因为Retrofit封装好了从Http请求到解析，出了bug很难找出来问题，添加HttpLoggingInterceptor拦截器方便调试接口
            .addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.d("HttpLoggingInterceptor", message);
                }
            }).setLevel(HttpLoggingInterceptor.Level.HEADERS))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .cache(getCache())
            .build();

//    private static RetrofitService retrofitService = new Retrofit.Builder()
//            .baseUrl(getBaseURL())
//            .addConverterFactory(GsonConverterFactory.create())// 添加Gson转换器
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())// 添加Retrofit到RxJava的转换器
//            .client(httpClient)
//            .build()
//            .create(RetrofitService.class);

    public static RetrofitService getRetrofitService() {
        return new Retrofit.Builder()
                .baseUrl(getBaseURL())
                .addConverterFactory(GsonConverterFactory.create())// 添加Gson转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())// 添加Retrofit到RxJava的转换器
                .client(httpClient)
                .build()
                .create(RetrofitService.class);
    }

//    private static RetrofitService2 retrofitService2 = new Retrofit.Builder()
//            .baseUrl(getBaseURL())
//            .addConverterFactory(GsonConverterFactory.create())// 添加Gson转换器
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())// 添加Retrofit到RxJava的转换器
//            .client(httpClient)
//            .build()
//            .create(RetrofitService2.class);

    public static RetrofitService2 getRetrofitService2() {
        return new Retrofit.Builder()
                .baseUrl(getBaseURL())
                .addConverterFactory(GsonConverterFactory.create())// 添加Gson转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())// 添加Retrofit到RxJava的转换器
                .client(httpClient)
                .build()
                .create(RetrofitService2.class);
    }
}
