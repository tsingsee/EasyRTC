package com.tsingsee.easyrtc.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.activity.CallActivity;
import com.tsingsee.easyrtc.activity.CreateRoomActivity;
import com.tsingsee.easyrtc.activity.SettingActivity;
import com.tsingsee.easyrtc.adapter.RoomAdapter;
import com.tsingsee.easyrtc.adapter.SpinnerAdapter;
import com.tsingsee.easyrtc.databinding.FragmentRoomBinding;
import com.tsingsee.easyrtc.http.BaseEntity3;
import com.tsingsee.easyrtc.http.BaseObserver3;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.Account;
import com.tsingsee.easyrtc.model.RoomBean;
import com.tsingsee.easyrtc.model.RoomModel;
import com.tsingsee.easyrtc.model.UserInfo;
import com.tsingsee.easyrtc.tool.Constant;
import com.tsingsee.easyrtc.tool.MD5Util;
import com.tsingsee.easyrtc.tool.SharedHelper;
import com.tsingsee.easyrtc.tool.ToastUtil;
import com.tsingsee.rtc.Options;
import com.tsingsee.rtc.Room;
import com.tsingsee.rtc.RoomStatus;
import com.tsingsee.rtc.StatusSink;
import com.tsingsee.rtc.XLog;

import java.util.Arrays;

import io.reactivex.Observable;
import pub.devrel.easypermissions.EasyPermissions;

public class RoomFragment extends BaseFragment implements View.OnClickListener, StatusSink {

    public static final String[] perms = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private FragmentRoomBinding binding;
    private RoomBean roomBeans;
    private RoomBean.Data roomBean;

    Room room;
    private Options options;
    public static boolean isConnecting = false;

    public RoomFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_room, container, false);
        binding.setOnClick(this);

        room = RoomModel.getInstance().getRoom();
        room.setContext(getContext());
        options = new Options(getContext());

        SpinnerAdapter adapter = new SpinnerAdapter(getContext(), Arrays.asList(Constant.STATUS_CONTENT));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roomStatusSp.setAdapter(adapter);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 1);
        binding.recyclerView.setLayoutManager(manager);

        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getConferences();
            }
        });

        showHub("查询中");
        getConferences();
        userInfo();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
//        updateView();
        room.setStatusSink(this);
        EasyPermissions.requestPermissions(this, null, 0, perms);
    }

    @Override
    public void onStop() {
        super.onStop();
        isConnecting = false;
        room.setStatusSink(null);
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
            case R.id.iv_add: {
                Intent intent = new Intent(getContext(), CreateRoomActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.iv_setting:{
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
            }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRoomStatusChange(RoomStatus roomStatus) {
        XLog.i("onRoomStatusChange: " + roomStatus);
        updateView();
    }

    private void updateView() {
        RoomStatus roomStatus = room.getRoomStatus();
        switch (roomStatus) {
            case ROOM_STATUS_SIGNOUT:

                break;
            case ROOM_STATUS_SIGNING:
            case ROOM_STATUS_SIGNIN:
            case ROOM_STATUS_CONNECTING:

                break;
            case ROOM_STATUS_CONNECTED:
                hideHub();
                isConnecting = false;

                Intent intent = new Intent(getContext(), CallActivity.class);
                intent.putExtra("roomBean", roomBean);
                startActivity(intent);
                break;
            case ROOM_STATUS_DISCONNECTING:

            default:
                XLog.e("Invalid state: " + roomStatus);
                break;
        }
    }

    private void connect(String no) {
        if (isConnecting) {
            ToastUtil.show("连接中");
            return;
        }

        showHub("连接中");
        isConnecting = true;
        room.setStatusSink(this);

        SharedHelper sp = new SharedHelper(getContext());
        Account account = sp.readAccount();
        UserInfo user = sp.readUserInfo();

        options.roomNumber = no;
        options.username = account.getUserName();
        options.password = MD5Util.md5(account.getPwd());
        options.serverAddress = account.getServerAddress();

        if (user != null) {
            options.displayName = user.getId() + "@" + user.getUserName();
        }

        room.setVideoEnable(roomBean.isVideoEnable());
        room.setOptions(options);
        room.join();
        options.save();
    }

    public void getConferences() {
        Observable<BaseEntity3<RoomBean>> observable = RetrofitFactory.getRetrofitService().getConferences(0, 100);
        observable.compose(compose(this.<BaseEntity3<RoomBean>> bindToLifecycle()))
                .subscribe(new BaseObserver3<RoomBean>(getContext(), dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(RoomBean model) {
                        hideHub();
                        binding.refreshLayout.setRefreshing(false);

                        binding.activityEmptyView.setVisibility(View.GONE);

                        roomBeans = model;

                        if (model == null && model.getDatas().size() == 0) {
                            binding.activityEmptyView.setVisibility(View.VISIBLE);
                            return;
                        }

                        RoomAdapter roomAdapter = new RoomAdapter(getContext(), roomBeans.getDatas());
                        binding.recyclerView.setAdapter(roomAdapter);
                        roomAdapter.setClickListener(new RoomAdapter.MyClickListener() {
                            @Override
                            public void onItemClick(int pos, boolean enable) {
                                roomBean = roomBeans.getDatas().get(pos);
                                roomBean.setVideoEnable(enable);
                                connect(roomBean.getId());
                            }
                        });
                    }

                    @Override
                    protected void loginSuccess() {
                        getConferences();
                    }
                });
    }

    public void userInfo() {
        final SharedHelper sp = new SharedHelper(getContext());
        Account account = sp.readAccount();

        Observable<BaseEntity3<UserInfo>> observable = RetrofitFactory.getRetrofitService().userInfo(account.getUserName());
        observable.compose(compose(this.<BaseEntity3<UserInfo>> bindToLifecycle()))
                .subscribe(new BaseObserver3<UserInfo>(getContext(), dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(UserInfo model) {
                        sp.saveUserInfo(model);
                    }

                    @Override
                    protected void loginSuccess() {

                    }
                });
    }
}
