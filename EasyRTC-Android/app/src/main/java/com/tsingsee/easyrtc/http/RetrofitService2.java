package com.tsingsee.easyrtc.http;

import com.tsingsee.easyrtc.model.MonthFlag;
import com.tsingsee.easyrtc.model.RecordModel;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 *
 */
public interface RetrofitService2 {

    /**
     * 按日查询通道录像
     *
     * @param id 通道号
     * @param period 日期, YYYYMMDD
     * @return*/
    @GET("query_record_daily")
    Observable<BaseEntity2<RecordModel>> queryDaily(@Query("id") String id, @Query("period") String period);

    /**
     * 按月查询通道录像记录
     *
     * @param id 通道号
     * @param period 月份, YYYYMM
     * @return
     */
    @GET("query_record_monthly")
    Observable<BaseEntity2<MonthFlag>> querymonthly(@Query("id") String id, @Query("period") String period);

    /**
     * 删除单条录像
     *
     * @param id 通道号
     * @param period 录像开始时间, YYYYMMDDHHmmss
     * @return
     */
    @GET("record/remove")
    Observable<BaseEntity2<String>> removeRecord(@Query("id") String id, @Query("period") String period);
}
