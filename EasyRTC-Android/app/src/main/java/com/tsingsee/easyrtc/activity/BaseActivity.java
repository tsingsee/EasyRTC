package com.tsingsee.easyrtc.activity;

import android.support.v4.view.ViewCompat;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.tool.Utils;
import com.tsingsee.easyrtc.view.LoadingView;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BaseActivity extends RxAppCompatActivity {

//    public ZLoadingDialog dialog;
    public LoadingView dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        setStatusBarColor(R.color.colorTheme);
    }

    protected <T> ObservableTransformer<T, T> compose(final LifecycleTransformer<T> lifecycle) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                // 可添加网络连接判断等
                                if (!Utils.isNetworkAvailable(BaseActivity.this)) {
                                    Toast.makeText(BaseActivity.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(lifecycle);
            }
        };
    }

    public void showHub(String text) {
        if (dialog == null) {
//            dialog = new ZLoadingDialog(this);
            dialog = new LoadingView(this);
        }

        dialog.show();
        dialog.setContentText(text);

//        dialog.setLoadingBuilder(Z_TYPE.SINGLE_CIRCLE)//设置类型
//                .setLoadingColor(Color.GRAY)//颜色
//                .setHintText(text)
//                .setHintTextSize(16) // 设置字体大小 dp
//                .setHintTextColor(Color.GRAY)  // 设置字体颜色
//                .setDurationTime(0.5) // 设置动画时间百分比 - 0.5倍
//                .setDialogBackgroundColor(Color.parseColor("#fff5f5f5")) // 设置背景色，默认白色
//                .show();
    }

    public void hideHub() {
        if (dialog != null) {
            dialog.cancel();
            dialog.dismiss();
        }
    }

    void setStatusBarColor(int statusColor) {
        if (android.os.Build.VERSION.SDK_INT > 19) {
            Window window = getWindow();
            // 取消状态栏透明
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 添加Flag把状态栏设为可绘制模式
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 设置状态栏颜色
            window.setStatusBarColor(getResources().getColor(statusColor));
            // 设置系统状态栏处于可见状态
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            // 让view不根据系统窗口来调整自己的布局
            ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                ViewCompat.setFitsSystemWindows(mChildView, false);
                ViewCompat.requestApplyInsets(mChildView);
            }
        }
    }
}
