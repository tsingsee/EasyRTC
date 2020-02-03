package com.tsingsee.easyrtc.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.activity.SettingActivity;
import com.tsingsee.easyrtc.adapter.VideotapeAdapter;
import com.tsingsee.easyrtc.databinding.FragmentRecordBinding;
import com.tsingsee.easyrtc.http.BaseEntity2;
import com.tsingsee.easyrtc.http.BaseObserver2;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.Devices;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;

import io.reactivex.Observable;

public class RecordFragment extends BaseFragment implements View.OnClickListener {
    private FragmentRecordBinding binding;
    private Devices devices;

    public RecordFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_record, container, false);
        binding.setOnClick(this);

        showHub("查询中");
        queryDevices();

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
            case R.id.iv_setting:{
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

//        // 添加侧滑菜单
//        binding.recyclerView.setSwipeMenuCreator(new SwipeMenuCreator() {
//            @Override
//            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
//                SwipeMenuItem item1 = new SwipeMenuItem(getContext());
//                item1.setText("播放");
//                item1.setTextSize(15);
//                item1.setTextColor(getResources().getColor(R.color.white_color));
//                item1.setBackgroundColor(getResources().getColor(R.color.colorTheme));
//                item1.setWidth(200);
//                item1.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
//
//                SwipeMenuItem item2 = new SwipeMenuItem(getContext());
//                item2.setText("删除");
//                item2.setTextSize(15);
//                item2.setTextColor(getResources().getColor(R.color.white_color));
//                item2.setBackgroundColor(getResources().getColor(R.color.color_f03d14));
//                item2.setWidth(200);
//                item2.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
//
//                swipeRightMenu.addMenuItem(item1);
//                swipeRightMenu.addMenuItem(item2);
//            }
//        });
//        binding.recyclerView.setSwipeMenuItemClickListener(new SwipeMenuItemClickListener() {
//            @Override
//            public void onItemClick(SwipeMenuBridge menuBridge) {
//                // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
//                menuBridge.closeMenu();
//
//                String item = devices.getDevices().get(menuBridge.getAdapterPosition());
//                if (menuBridge.getPosition() == 0) {
//                    // TODO 播放
//                } else {
//                    // TODO 删除
//                }
//            }
//        });
    }

    public void queryDevices() {
        Observable<BaseEntity2<Devices>> observable = RetrofitFactory.getRetrofitService().queryDevices();
        observable.compose(compose(this.<BaseEntity2<Devices>> bindToLifecycle()))
                .subscribe(new BaseObserver2<Devices>(getContext(), dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(Devices model) {
                        hideHub();

                        binding.activityEmptyView.setVisibility(View.GONE);
                        if (model == null || model.getDevices().size() == 0) {
                            binding.activityEmptyView.setVisibility(View.VISIBLE);
                            return;
                        }

                        devices = model;

                        initView();
                        VideotapeAdapter adapter = new VideotapeAdapter(getContext());
                        binding.recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged(model.getDevices());
                    }

                    @Override
                    protected void loginSuccess() {
                        queryDevices();
                    }
                });
    }
}
