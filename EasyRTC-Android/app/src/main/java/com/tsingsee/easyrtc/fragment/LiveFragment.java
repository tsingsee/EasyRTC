package com.tsingsee.easyrtc.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.activity.LiveActivity;
import com.tsingsee.easyrtc.activity.LiveDetailActivity;
import com.tsingsee.easyrtc.activity.SettingActivity;
import com.tsingsee.easyrtc.adapter.LiveAdapter;
import com.tsingsee.easyrtc.databinding.FragmentLiveBinding;
import com.tsingsee.easyrtc.http.BaseEntity;
import com.tsingsee.easyrtc.http.BaseObserver;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.LiveSessionModel;
import com.tsingsee.rtc.Options;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class LiveFragment extends BaseFragment implements View.OnClickListener {
    private FragmentLiveBinding binding;
    private AlertDialog alertDialog;

    private LiveSessionModel liveSessionModel;

    TimeCount mTimeCount = null;

    public LiveFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_live, container, false);
        binding.setOnClick(this);
        initView();

        showHub("查询中");

        mTimeCount = new TimeCount(1000 * 60 * 60 * 24, 1000 * 10);
        mTimeCount.start();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting: {
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
            }
                break;
            default:

                break;
        }
    }

    private void initView() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(manager);

        // 添加侧滑菜单
        binding.recyclerView.setSwipeMenuCreator(new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                SwipeMenuItem item1 = new SwipeMenuItem(getContext());
                item1.setText("播放");
                item1.setTextSize(15);
                item1.setTextColor(getResources().getColor(R.color.white_color));
                item1.setBackgroundColor(getResources().getColor(R.color.colorTheme));
                item1.setWidth(200);
                item1.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

                SwipeMenuItem item2 = new SwipeMenuItem(getContext());
                item2.setText("详情");
                item2.setTextSize(15);
                item2.setTextColor(getResources().getColor(R.color.white_color));
                item2.setBackgroundColor(getResources().getColor(R.color.color_f4780b));
                item2.setWidth(200);
                item2.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

                swipeRightMenu.addMenuItem(item1);
                swipeRightMenu.addMenuItem(item2);
            }
        });
        binding.recyclerView.setSwipeMenuItemClickListener(new SwipeMenuItemClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
                menuBridge.closeMenu();

                LiveSessionModel.Session session = liveSessionModel.getSessions().getSessions().get(menuBridge.getAdapterPosition());
                if (menuBridge.getPosition() == 0) {
                    // 播放
                    showSingleAlertDialog(session);
                } else {
                    // 详情
                    Intent intent = new Intent(getContext(), LiveDetailActivity.class);
                    intent.putExtra("session", session);
                    startActivity(intent);
                }
            }
        });
    }

    public void getLiveSessions() {
        Observable<BaseEntity<LiveSessionModel>> observable = RetrofitFactory.getRetrofitService().getLiveSessions();
        observable.compose(compose(this.<BaseEntity<LiveSessionModel>> bindToLifecycle()))
                .subscribe(new BaseObserver<LiveSessionModel>(getContext(), dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(LiveSessionModel model) {
                        hideHub();

                        binding.activityEmptyView.setVisibility(View.GONE);

                        liveSessionModel = model;

                        if (model == null ||
                                model.getSessions() == null ||
                                model.getSessions().getSessions() == null ||
                                model.getSessions().getSessions().size() == 0) {
                            binding.activityEmptyView.setVisibility(View.VISIBLE);
                            return;
                        }

                        List<LiveSessionModel.Session> result = new ArrayList<>();
                        for (LiveSessionModel.Session s : model.getSessions().getSessions()) {
                            // 返回的数据有2种格式：hls/record，如果有record格式说明有录像
                            if ("hls".equals(s.getApplication())) {
                                result.add(s);
                            }
                        }

                        LiveAdapter adapter = new LiveAdapter(getContext());
                        binding.recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged(result);
                    }

                    @Override
                    protected void loginSuccess() {
                        getLiveSessions();
                    }
                });
    }

    private int index = 0;
    public void showSingleAlertDialog(LiveSessionModel.Session session) {
//        final String[] items = {"HLS", "FLV", "RTMP"};
        final String[] items = {"HLS", "FLV"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setTitle("播放类型");
        alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                index = i;
            }
        });

        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();

                Options options = new Options(getContext());
                String addr = "https://" + options.serverAddress + "/record";

                String url;
                if (index == 0) {
                    url = addr + session.getHls().replace("/hls", "");
                } else if (index == 1) {
                    url = addr + session.getHttpFlv().replace("/hls", "");
                } else {
                    url = session.getRtmp();
                }

                Intent intent = new Intent(getContext(), LiveActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });

        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        private  long millisInFutureC;

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
            this.millisInFutureC = millisInFuture;
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            try {
                if (mTimeCount != null) {
                    mTimeCount.cancel();
                    mTimeCount = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onTick(long millisUntilFinished){// 计时过程显示
            Log.i(TAG, millisUntilFinished / 1000 + "秒");
            getLiveSessions();
        }
    }
}
