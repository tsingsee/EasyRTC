package com.tsingsee.easyrtc.http;

import com.tsingsee.easyrtc.model.Account;
import com.tsingsee.easyrtc.model.LiveSessionModel;
import com.tsingsee.easyrtc.model.RoomBean;
import com.tsingsee.easyrtc.model.RoomRecord;
import com.tsingsee.easyrtc.model.ServiceInfo;
import com.tsingsee.easyrtc.model.UploadBean;
import com.tsingsee.easyrtc.model.UserInfo;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 *
 */
public interface RetrofitService {

    /**
     * 登录
     */
    @POST("login")
    Observable<BaseEntity3<Account>> login(@Query("username") String username, @Query("password") String password);

    /**
     * 登录用户信息
     */
//    @POST("userInfo")
//    Observable<BaseEntity3<UserInfo>> userInfo();
    @POST("user/get")
    Observable<BaseEntity3<UserInfo>> userInfo(@Query("id") String username);

    /**
     * 获取服务器信息
     */
    @GET("getserverinfo")
    Observable<BaseEntity3<ServiceInfo>> getServerInfo();

    /**
     * 激活产品
     */
    @GET("verifyproductcode")
    Observable<BaseEntity3<Object>> verifyproductcode(@Query("productcode") String code);

    /**
     * 查询有录像的会议室列表
     */
    @GET("record/query_conferences")
    Observable<BaseEntity3<List<RoomRecord>>> queryDevices();

    /**
     * 直播列表
     */
    @POST("live/sessions")
    Observable<BaseEntity3<List<LiveSessionModel>>> getLiveSessions();

    /**
     * 获取视频会议列表
     */
    @GET("conference/list")
    Observable<BaseEntity3<RoomBean>> getConferences(@Query("start") int start, @Query("limit") int limit);

    /**
     * 上传图片
     */
    @Multipart
    @POST("screenshot/upload")
    Observable<BaseEntity3<UploadBean>> uploadImages(@Part("id") RequestBody id, @Part MultipartBody.Part cover);
}
