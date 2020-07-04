package com.tsingsee.easyrtc.http;

import android.content.Context;
import android.util.Log;

import com.tsingsee.easyrtc.RTCApplication;
import com.tsingsee.easyrtc.model.Account;
import com.tsingsee.easyrtc.tool.SharedHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * https://www.jianshu.com/p/ea2055db3dd3
 * */
public class XInterceptor {
    private static final String TAG = "XInterceptor";

    public static class Token implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            // chain就是包含request和response
            Request.Builder builder = chain.request().newBuilder();

            // 请求头添加token
            SharedHelper helper = new SharedHelper(RTCApplication.getContext());
            Account account = helper.readAccount();
            if (!account.getToken().equals("")) {
                builder.addHeader("Cookie", "token=" + account.getToken());
            }

            builder.addHeader("Accept", "application/vnd.apple.mpegurl");

            return chain.proceed(builder.build());
        }
    }

    /**
     * 自定义的，重试N次的拦截器
     * 通过：addInterceptor 设置
     */
    public static class Retry implements Interceptor {

        public int maxRetry;        // 最大重试次数
        private int retryNum = 0;   // 假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）

        public Retry() {
            this.maxRetry = 3;      // 最大重试3次
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            while (!response.isSuccessful() && retryNum < maxRetry) {
                retryNum++;
                Log.i(TAG,"num:" + retryNum);

                response = chain.proceed(request);
            }

            return response;
        }
    }

    /**
     * 设置没有网络的情况下的缓存时间
     * 通过：addInterceptor 设置
     */
    public static class CommonNoNetCache implements Interceptor {

        private int maxCacheTimeSecond;// 设置最大失效时间，失效则不使用
        private Context applicationContext;

        public CommonNoNetCache() {
            this.maxCacheTimeSecond = 60 * 60 * 24 * 30;
            this.applicationContext = RTCApplication.getContext();
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetworkUtils.isConnected(applicationContext)) {
                CacheControl tempCacheControl = new CacheControl.Builder()
                        .onlyIfCached()
                        .maxStale(maxCacheTimeSecond, TimeUnit.SECONDS)
                        .build();
                request = request.newBuilder()
                        .cacheControl(tempCacheControl)
                        .build();
            }

            return chain.proceed(request);
        }
    }

    /**
     * 设置在有网络的情况下的缓存时间。在有网络的时候，会优先获取缓存
     * 通过：addNetworkInterceptor 设置
     */
    public static class CommonNetCache implements Interceptor {
        private int maxCacheTimeSecond;

        public CommonNetCache() {
            this.maxCacheTimeSecond = 30;// 设置最大失效时间，失效则不使用
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            Response originalResponse = chain.proceed(request);
            return originalResponse.newBuilder()
                    .removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, max-age=" + maxCacheTimeSecond)
                    .build();
        }
    }

    /**
     * 设置一个日志打印拦截器
     * 通过：addInterceptor 设置
     */
    public static class CommonLog implements Interceptor {

        private boolean logOpen = true;// 统一的日志输出控制，可以构造方法传入，统一控制日志

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            long t1 = System.currentTimeMillis();// 请求发起的时间
            Response response = chain.proceed(request);
            long t2 = System.currentTimeMillis();// 收到响应的时间

            if (logOpen) {
                //这里不能直接使用response.body().string()的方式输出日志
                //因为response.body().string()之后，response中的流会被关闭，程序会报错，我们需要创建出一
                //个新的response给应用层处理
                ResponseBody responseBody = response.peekBody(1024 * 1024);
                Log.i(TAG, response.request().url() + " , use-timeMs: " + (t2 - t1) + " , data: " + responseBody.string());
            }

            return response;
        }
    }
}
