package com.tsingsee.easyrtc.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    /**
     * 判断网络是否连接
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @param context 上下文
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnected();
    }

    /**
     * 获取活动网络信息
     *
     * @param context 上下文
     * @return NetworkInfo
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

//    /*
//     * 判断是否有网络
//     */
//    public static boolean isNetworkAvailable2(Context context) {
//        ConnectivityManager cm = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (cm == null) {
//            return false;
//        } else {
//            // 打印所有的网络状态
//            NetworkInfo[] infos = cm.getAllNetworkInfo();
//            if (infos != null) {
//                for (int i = 0; i < infos.length; i++) {
//                    // Log.d(TAG, "isNetworkAvailable - info: " +
//                    // infos[i].toString());
//                    if (infos[i].getState() == NetworkInfo.State.CONNECTED) {
//                        Log.d(TAG, "isNetworkAvailable -  I " + i);
//                    }
//                }
//            }
//
//            // 如果仅仅是用来判断网络连接　　　　　　
//            // 则可以使用 cm.getActiveNetworkInfo().isAvailable();
//            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
//            if (networkInfo != null) {
//                Log.d(TAG,
//                        "isNetworkAvailable - 是否有网络： "
//                                + networkInfo.isAvailable());
//            } else {
//                Log.d(TAG, "isNetworkAvailable - 完成没有网络！");
//                return false;
//            }
//
//            // 1、判断是否有3G网络
//            if (networkInfo != null
//                    && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
//                Log.d(TAG, "isNetworkAvailable - 有3G网络");
//                return true;
//            } else {
//                Log.d(TAG, "isNetworkAvailable - 没有3G网络");
//            }
//
//            // 2、判断是否有wifi连接
//            if (networkInfo != null
//                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//                Log.d(TAG, "isNetworkAvailable - 有wifi连接");
//                return true;
//            } else {
//                Log.d(TAG, "isNetworkAvailable - 没有wifi连接");
//            }
//        }
//
//        return false;
//    }
}
