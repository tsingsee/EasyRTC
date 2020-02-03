package com.tsingsee.easyrtc.http;

import com.tsingsee.easyrtc.model.Devices;
import com.tsingsee.easyrtc.model.LiveSessionModel;
import com.tsingsee.easyrtc.model.RequestKeyModel;
import com.tsingsee.easyrtc.model.ServiceInfo;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 *
 */
public interface RetrofitService {

    @GET("getserverinfo")
    Observable<BaseEntity<ServiceInfo>> getServerInfo();

    @GET("getrequestkey")
    Observable<BaseEntity<RequestKeyModel>> getRequestkey();

    @GET("verifyproductcode")
    Observable<BaseEntity<Object>> verifyproductcode(@Query("productcode") String code);

    @GET("query_devices")
    Observable<BaseEntity2<Devices>> queryDevices();

    @GET("getlivesessions")
    Observable<BaseEntity<LiveSessionModel>> getLiveSessions();

//    @GET("/api/v1/getchannels")
//    Observable<BaseEntity<Video>> getChannels(@Query("q") String keyword, @Query("start") int start, @Query("limit") int limit);
//
//    @GET("/api/v1/getchannelstream")
//    Observable<BaseEntity<Live>> getChannelStream(@Query("Channel") String channel, @Query("Protocol") String potocol);
//
//    @GET("/api/v1/ptzcontrol")
//    Observable<BaseEntity<Object>> ptzcontrol(@Query("channel") String channel,        // 通道ID
//                                              @Query("command") String command,        // 动作命令:stop停止、up向上移动、down向下移动、left向左移动、right向右移动、zoomin、zoomout、focusin、focusout、aperturein、apertureout
//                                              @Query("actiontype") String actiontype,  // 动作类型:continuous或者single
//                                              @Query("speed") String speed,            // 动作速度:5
//                                              @Query("protocol") String protocol);     // 摄像机接入的协议:onvif
//
//    @GET
//    Observable<BaseEntity<String>> checkURLFormat(@Url String url);
}
