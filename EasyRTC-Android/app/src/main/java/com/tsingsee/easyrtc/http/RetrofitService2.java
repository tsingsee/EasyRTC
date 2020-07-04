package com.tsingsee.easyrtc.http;

import com.tsingsee.easyrtc.model.MonthFlag;
import com.tsingsee.easyrtc.model.Record;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 *
 */
public interface RetrofitService2 {

    /**
     * 查询会议室录像日记录
     *
     * @param id     通道号
     * @param period 日期, YYYYMMDD
     * @return
     */
    @GET("record/query_daily")
    Observable<BaseEntity3<List<Record>>> queryDaily(@Query("id") String id, @Query("period") String period);

    /**
     * 查询会议室录像月记录
     *
     * @param id     通道号
     * @param period 月份, YYYYMM
     * @return
     */
    @GET("record/query_flags")
    Observable<BaseEntity3<MonthFlag>> querymonthly(@Query("id") String id, @Query("period") String period);

    /**
     * 删除单条录像
     *
     * @param id     通道号
     * @param period 录像开始时间, YYYYMMDDHHmmss
     * @return
     */
    @GET("record/remove")
    Observable<BaseEntity3<String>> removeRecord(@Query("id") String id, @Query("period") String period);
}
