package com.tsingsee.easyrtc.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tsingsee.easyrtc.R;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.components.support.RxAppCompatDialogFragment;
import com.tsingsee.easyrtc.tool.Utils;
import com.tsingsee.easyrtc.view.LoadingView;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BaseFragment extends RxAppCompatDialogFragment {
    protected String TAG = getClass().getSimpleName();

    protected Context mContext;
    protected Map<String, Object> params = new HashMap();
    protected ProgressDialog pd;
    private boolean isFirstLoad = true;
    private boolean isPrepared;
    private boolean isVisible;
    private boolean isPush;

    public LoadingView dialog;

    public void dismissWaitDialog() {
        if ((getActivity() != null) && (!getActivity().isFinishing())) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if ((BaseFragment.this.pd != null) && (BaseFragment.this.pd.isShowing())) {
                        BaseFragment.this.pd.dismiss();
                        BaseFragment.this.pd = null;
                    }
                }
            });
            return;
        }
    }

    protected void lazyLoad() {
        if ((this.isPrepared) && (this.isVisible) && (this.isFirstLoad)) {
            lazyLoadData();
            this.isFirstLoad = false;
            return;
        }
    }

    protected void lazyLoadData() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        isPush = false;
    }

    /**
     * 描  述：重写startActivity方法，加入动画
     * 参  数：intent
     */
    @Override
    public void startActivity(Intent intent) {
        if (isPush) {
            return;
        }
        isPush = true;

        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(R.anim.right_push_in, R.anim.hold);
    }

    /**
     * 描  述：重写startActivityForResult方法，加入动画
     * 参  数：intent
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        getActivity().overridePendingTransition(R.anim.right_push_in, R.anim.hold);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        this.isFirstLoad = true;
        this.mContext = getActivity();
        return inflater.inflate(R.layout.fragment_base, viewGroup, false);
    }

    public void onDestroy() {
        this.mContext = null;
        super.onDestroy();
    }

    public void onHiddenChanged(boolean paramBoolean) {
        super.onHiddenChanged(paramBoolean);
        if (!paramBoolean) {
            this.isVisible = true;
            onVisible();
        } else {
            this.isVisible = false;
            onInvisible();
        }
    }

    protected void onInvisible() {
    }

    public void onSaveInstanceState(Bundle paramBundle) {
        paramBundle.putBoolean("state_save_is_hidden", isHidden());
    }

    public void onViewCreated(View paramView, @Nullable Bundle paramBundle) {
        super.onViewCreated(paramView, paramBundle);
        this.isPrepared = true;
        userBundle(paramBundle);
        lazyLoad();
    }

    protected void onVisible() {
        lazyLoad();
    }

    public void setToolbar(Toolbar paramToolbar) {
        ((AppCompatActivity) getActivity()).setSupportActionBar(paramToolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    public void setUserVisibleHint(boolean paramBoolean) {
        super.setUserVisibleHint(paramBoolean);
        if (getUserVisibleHint()) {
            this.isVisible = true;
            onVisible();
        } else {
            this.isVisible = false;
            onInvisible();
        }
    }

    public void showWaitDialog() {
        showWaitDialog("加载中", true);
    }

    public void showWaitDialog(String paramString) {
        showWaitDialog(paramString, true);
    }

    public void showWaitDialog(final String paramString, final boolean paramBoolean) {
        if ((getActivity() != null) && (!getActivity().isFinishing())) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if ((BaseFragment.this.pd == null) || (!BaseFragment.this.pd.isShowing())) {
                        BaseFragment.this.pd = new ProgressDialog(BaseFragment.this.mContext);
                        BaseFragment.this.pd.setMessage(paramString);
                        BaseFragment.this.pd.setCancelable(paramBoolean);
                        BaseFragment.this.pd.show();
                    }
                }
            });
            return;
        }
    }

    public void startActivity(Class<?> paramClass) {
        startActivity(new Intent(this.mContext, paramClass));
    }

    protected void userBundle(Bundle paramBundle) {
        if (paramBundle != null) {
            boolean bool = paramBundle.getBoolean("state_save_is_hidden");
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (bool) {
                transaction.hide(this);
            } else {
                transaction.show(this);
            }
            transaction.commit();
        }
    }

//    public void showHub(String text) {
//        if (dialog == null) {
//            dialog = new ZLoadingDialog(getContext());
//        }
//
//        dialog.setLoadingBuilder(Z_TYPE.SINGLE_CIRCLE)
//                .setLoadingColor(Color.GRAY)
//                .setHintText(text)
//                .setHintTextSize(15)
//                .setHintTextColor(Color.BLACK)
//                .setDurationTime(0.6)
//                .setDialogBackgroundColor(Color.parseColor("#f5f5f5")) // 设置背景色，默认白色
//                .show();
//    }

    public void showHub(String text) {
        if (dialog == null) {
//            dialog = new ZLoadingDialog(this);
            dialog = new LoadingView(getContext());
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
                                if (!Utils.isNetworkAvailable(getContext())) {
                                    Toast.makeText(getContext(), R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(lifecycle);
            }
        };
    }
}
