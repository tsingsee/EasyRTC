package com.tsingsee.easyrtc.activity.Record;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.activity.BaseActivity;
import com.tsingsee.easyrtc.databinding.ActivityCalendarBinding;
import com.tsingsee.easyrtc.http.BaseEntity2;
import com.tsingsee.easyrtc.http.BaseObserver2;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.MonthFlag;
import com.tsingsee.easyrtc.tool.DateUtil;
import com.tsingsee.easyrtc.tool.MyDPCNCalendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.aigestudio.datepicker.bizs.calendars.DPCManager;
import cn.aigestudio.datepicker.bizs.decors.DPDecor;
import cn.aigestudio.datepicker.cons.DPMode;
import cn.aigestudio.datepicker.views.DatePicker;
import io.reactivex.Observable;

/**
 * 展示日历，选择日期
 * */
public class CalendarActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

    private ActivityCalendarBinding binding;

    private List<String> recordDates;

    private String id;
    private Date selectDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calendar);

        recordDates = new ArrayList<>();

        setSupportActionBar(binding.mainToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.mainToolbar.setOnMenuItemClickListener(this);
        // 左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        binding.mainToolbar.setNavigationIcon(R.drawable.back);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        selectDate = (Date) intent.getSerializableExtra("selectDate");

        binding.datePicker.setDate(DateUtil.getDateYear(selectDate), DateUtil.getDateMonth(selectDate));
        binding.datePicker.setMode(DPMode.SINGLE);

        DPCManager.getInstance().initCalendar(new MyDPCNCalendar());

        binding.datePicker.setOnDatePickedListener(new DatePicker.OnDatePickedListener() {
            @Override
            public void onDatePicked(String date) {
                String[]arr = date.split("-");
                String year = arr[0];
                int month = Integer.parseInt(arr[1]);
                int day = Integer.parseInt(arr[2]);
                String d = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);

                for (String str : recordDates) {
                    if (str.equals(d)) {
                        selectDate = DateUtil.getDate(date);
                        clickDate();

                        return;
                    }
                }

                Toast.makeText(CalendarActivity.this, "该日没有录像", Toast.LENGTH_SHORT).show();
            }
        });

        showHub("查询中");
        querymonthly();
    }

    // 返回的功能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    private void querymonthly() {
        String period = DateUtil.getDateStr(selectDate, "yyyyMM");

        Observable<BaseEntity2<MonthFlag>> observable = RetrofitFactory.getRetrofitService2().querymonthly(id, period);
        observable.compose(compose(this.<BaseEntity2<MonthFlag>> bindToLifecycle()))
                .subscribe(new BaseObserver2<MonthFlag>(this, dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(MonthFlag flag) {
                        hideHub();

                        String res = flag.getFlags();
                        for (int i = 0; i < res.length(); i++) {
                            String item = res.charAt(i) + "";

                            if (item.equals("1")) {
                                recordDates.add(DateUtil.getDateStr(selectDate, "yyyy-MM") + "-" + String.format("%02d", i+1));
                            }
                        }

                        DPCManager.getInstance().setDecorBG(recordDates);

                        binding.datePicker.setDPDecor(new DPDecor() {
                            @Override
                            public void drawDecorBG(Canvas canvas, Rect rect, Paint paint) {
                                paint.setColor(Color.RED);
                                canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() / 2F - 4, paint);
                            }
                        });
                    }

                    @Override
                    protected void loginSuccess() {
                        querymonthly();
                    }
                });
    }

    private void clickDate() {
        //数据是使用Intent返回
        Intent intent = new Intent();
        //把返回数据存入Intent
        intent.putExtra("selectDate", selectDate);
        //设置返回数据
        CalendarActivity.this.setResult(RESULT_OK, intent);
        //关闭Activity
        CalendarActivity.this.finish();
    }
}
