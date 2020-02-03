package com.tsingsee.easyrtc.view.RullerView;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

import java.util.List;

/**
 * 可滑动标尺
 * */
public abstract class BaseScaleView extends View {

    protected int maxSecond;         // 最大刻度
    protected int minSecond;         // 最小刻度
    protected double mCountScale;  // 滑动的总刻度

    protected double mScaleScrollViewRange;

    protected double secondScaleMargin;     // 每一秒的刻度间距
    protected double mScaleHeight;     // 刻度线的高度
    protected double mScaleMaxHeight;  // 整刻度线高度

    protected double mRectWidth;       // 总宽度
    protected double mRectHeight;      // 高度

    protected Scroller mScroller;
    protected double mScrollLastX;

    protected double mTempScale;       // 用于判断滑动方向
    protected double mMidCountScale;   // 中间刻度

    private List<TimeRange> ranges;

    protected OnScrollListener mScrollListener;

    public interface OnScrollListener {
        void onScaleScroll(double scale);
        void touchBegin();
        void touchEnd(double scale);
    }

    public BaseScaleView(Context context) {
        super(context);
        init(null);
    }

    public BaseScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BaseScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseScaleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    protected void init(AttributeSet attrs) {
        // 获取自定义属性
        minSecond = 0;
        maxSecond = 24 * 60 * 60;// 一天的秒数
        secondScaleMargin = 24.0 / 60.0;
        mScaleHeight = 20;

        mScroller = new Scroller(getContext());

        initVar();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 画笔
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        // 抗锯齿
        paint.setAntiAlias(true);
        // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        paint.setDither(true);
        // 空心
        paint.setStyle(Paint.Style.STROKE);
        // 文字居中
        paint.setTextAlign(Paint.Align.CENTER);

        onDrawLine(canvas, paint);
        drawTimeRange(canvas, paint);
        onDrawScale(canvas, paint);     // 画刻度
        onDrawPointer(canvas, paint);   // 画指针

        super.onDraw(canvas);
    }

    protected abstract void initVar();

    // 画线
    protected abstract void onDrawLine(Canvas canvas, Paint paint);

    // 画刻度
    protected abstract void onDrawScale(Canvas canvas, Paint paint);

    // 画指针
    protected abstract void onDrawPointer(Canvas canvas, Paint paint);

    // 高亮某个时间段
    protected abstract void drawTimeRange(Canvas canvas, Paint paint);

    // 滑动到指定刻度
    public abstract void scrollToScale(int second);

    public void setCurScale(int second) {
        if (second >= minSecond && second <= maxSecond) {
            scrollToScale(second);
            postInvalidate();
        }
    }

    /**
     * 使用Scroller时需重写
     */
    @Override
    public void computeScroll() {
        super.computeScroll();

        // 判断Scroller是否执行完毕
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // 通过重绘来不断调用computeScroll
            invalidate();
        }
    }

    public void smoothScrollBy(double dx, double dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), (int)dx, (int)dy);
    }

    public void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    /**
     * 设置回调监听
     *
     * @param listener
     */
    public void setOnScrollListener(OnScrollListener listener) {
        this.mScrollListener = listener;
    }

    public List<TimeRange> getRanges() {
        return ranges;
    }

    public void setRanges(List<TimeRange> ranges) {
        this.ranges = ranges;

        postInvalidate();
    }
}
