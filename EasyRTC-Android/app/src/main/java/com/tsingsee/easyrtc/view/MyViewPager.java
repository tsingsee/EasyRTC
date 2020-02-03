package com.tsingsee.easyrtc.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public class MyViewPager extends ViewPager {

    private boolean scrollble = false;

    public MyViewPager(@NonNull Context context) {
        this(context, null);
    }

    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (scrollble == false) {
            return false;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (scrollble == false) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

}
