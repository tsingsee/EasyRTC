package com.tsingsee.easyrtc.view.RullerView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.tsingsee.easyrtc.R;

/**
 * 水平的 自定义时间轴/可滑动标尺
 * */
public class HorizontalScaleScrollView extends BaseScaleView {
    private Context mContext;

    public HorizontalScaleScrollView(Context context) {
        super(context);
        mContext = context;
    }

    public HorizontalScaleScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public HorizontalScaleScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public HorizontalScaleScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    @Override
    protected void initVar() {
        mRectWidth = (maxSecond - minSecond) * secondScaleMargin;
        mRectHeight = mScaleHeight * 8;
        mScaleMaxHeight = mScaleHeight * 2;

        // 设置layoutParams
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams((int)mRectWidth, (int)mRectHeight);
        this.setLayoutParams(lp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.makeMeasureSpec((int)mRectHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, height);

        mScaleScrollViewRange = getMeasuredWidth();
        mTempScale = mScaleScrollViewRange / secondScaleMargin / 2 + minSecond;
        mMidCountScale = mScaleScrollViewRange / secondScaleMargin / 2 + minSecond;
    }

    @Override
    protected void onDrawLine(Canvas canvas, Paint paint) {
        canvas.drawLine(0, (float) mRectHeight, (float)mRectWidth, (float)mRectHeight, paint);
    }

    @Override
    protected void drawTimeRange(Canvas canvas, Paint paint) {
        if (getRanges() == null) {
            return;
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mContext.getResources().getColor(R.color.colorTheme));

        for (TimeRange range : getRanges()) {
            Rect rect = new Rect((int)(range.start * secondScaleMargin),
                    (int) mRectHeight - 10,
                    (int)((range.start + range.duration) * secondScaleMargin),
                    (int) mRectHeight);
            canvas.drawRect(rect, paint);
        }

        paint.setColor(Color.GRAY);
    }

    @Override
    protected void onDrawScale(Canvas canvas, Paint paint) {
        paint.setTextSize((float)mRectHeight / 5);

        int fiveMinute = 60 * 5;
        int oneMinute = 60 * 1;

        for (int i = 0; i <= maxSecond - minSecond; i+=oneMinute) {
            if (i % fiveMinute == 0) { // 每5分钟
                canvas.drawLine((float)(i * secondScaleMargin),
                        (float)mRectHeight,
                        (float)(i * secondScaleMargin),
                        (float)(mRectHeight - mScaleMaxHeight),
                        paint);

                // 整值文字
                canvas.drawText(secToTime(i),
                        (float)(i * secondScaleMargin),
                        (float)(mRectHeight - mScaleMaxHeight - 20),
                        paint);
            } else if (i % oneMinute == 0) { // 每分钟
                canvas.drawLine((float) (i * secondScaleMargin),
                        (float) mRectHeight,
                        (float) (i * secondScaleMargin),
                        (float) (mRectHeight - mScaleHeight),
                        paint);
            }
        }
    }

    @Override
    protected void onDrawPointer(Canvas canvas, Paint paint) {
        paint.setColor(mContext.getResources().getColor(R.color.colorTheme));

        // 每一屏幕刻度的个数/2
        double countScale = mScaleScrollViewRange / secondScaleMargin / 2;
        // 根据滑动的距离，计算指针的位置【指针始终位于屏幕中间】
        int finalX = mScroller.getFinalX();
        // 滑动的刻度（四舍五入取整）
        int tmpCountScale = (int) Math.rint((double) finalX / secondScaleMargin);
        // 总刻度
        mCountScale = tmpCountScale + countScale + minSecond;

        if (mScrollListener != null) { // 回调方法
            mScrollListener.onScaleScroll(mCountScale);
        }

        // 整值文字
        canvas.drawText(secondToTime((int)mCountScale), (float)(countScale * secondScaleMargin + finalX), 40, paint);

        canvas.drawLine((float)(countScale * secondScaleMargin + finalX),
                (float) mRectHeight,
                (float)(countScale * secondScaleMargin + finalX),
                (float)(mRectHeight - mScaleMaxHeight - mScaleHeight),
                paint);
    }

    @Override
    public void scrollToScale(int second) {
        if (second < minSecond || second > maxSecond) {
            return;
        }

        double dx = (second - mCountScale) * secondScaleMargin;
        smoothScrollBy(dx, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mScroller != null && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mScrollLastX = x;

                if (mScrollListener != null) { // 回调方法
                    mScrollListener.touchBegin();
                }

                return true;
            case MotionEvent.ACTION_MOVE:
                double dataX = mScrollLastX - x;
                if (mCountScale - mTempScale < 0) {         // 向右边滑动
                    if (mCountScale <= minSecond && dataX <= 0)  // 禁止继续向右滑动
                        return super.onTouchEvent(event);
                } else if (mCountScale - mTempScale > 0) {  // 向左边滑动
                    if (mCountScale >= maxSecond && dataX >= 0)  // 禁止继续向左滑动
                        return super.onTouchEvent(event);
                }
                smoothScrollBy(dataX, 0);
                mScrollLastX = x;
                postInvalidate();
                mTempScale = mCountScale;
                return true;
            case MotionEvent.ACTION_UP:
                if (mCountScale < minSecond) {
                    mCountScale = minSecond;
                }

                if (mCountScale > maxSecond) {
                    mCountScale = maxSecond;
                }

                double finalX = (mCountScale - mMidCountScale) * secondScaleMargin;
                mScroller.setFinalX((int)finalX); // 纠正指针位置
                postInvalidate();

                if (mScrollListener != null) { // 回调方法
                    mScrollListener.touchEnd(mCountScale);
                }

                return true;
        }

        return super.onTouchEvent(event);
    }

    private String secondToTime(int second) {
        int hours = second / 3600; //转换小时
        second = second % 3600;     //剩余秒数
        int minutes = second /60;  //转换分钟
        second = second % 60;       //剩余秒数

        return unitFormat(hours)+ ":" + unitFormat(minutes)+ ":" + unitFormat(second);
    }

    private String secToTime(int time) {
        String timeStr;
        int hour, minute;

        if (time <= 0) {
            return "00:00";
        } else {
            minute = time / 60;
            if (minute < 60) {
                timeStr = "00:" + unitFormat(minute);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "23:55";
                minute = minute % 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute);
            }
        }
        return timeStr;
    }

    private String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
}
