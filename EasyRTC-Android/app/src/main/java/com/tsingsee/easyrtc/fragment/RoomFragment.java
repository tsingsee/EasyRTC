package com.tsingsee.easyrtc.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.activity.CallActivity;
import com.tsingsee.easyrtc.activity.CreateRoomActivity;
import com.tsingsee.easyrtc.activity.RtcActivity;
import com.tsingsee.easyrtc.activity.SettingActivity;
import com.tsingsee.easyrtc.adapter.RoomAdapter;
import com.tsingsee.easyrtc.adapter.SpinnerAdapter;
import com.tsingsee.easyrtc.databinding.FragmentRoomBinding;
import com.tsingsee.easyrtc.model.Account;
import com.tsingsee.easyrtc.model.RoomBean;
import com.tsingsee.easyrtc.model.RoomModel;
import com.tsingsee.easyrtc.tool.Constant;
import com.tsingsee.easyrtc.tool.SharedHelper;
import com.tsingsee.easyrtc.tool.ToastUtil;
import com.tsingsee.rtc.Options;
import com.tsingsee.rtc.Room;
import com.tsingsee.rtc.RoomStatus;
import com.tsingsee.rtc.StatusSink;
import com.tsingsee.rtc.XLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class RoomFragment extends BaseFragment implements View.OnClickListener, StatusSink {

    public static final String[] perms = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private FragmentRoomBinding binding;
    private List<RoomBean> roomBeans;
    private RoomBean roomBean;

    Room room;
    private Options options;
    public static boolean isConnecting = false;

    public RoomFragment() {
        roomBeans = new ArrayList<>();

        roomBeans.add(getRoomBean("3580", "2019年公司年度总结会议", "在线", "2020-1-1 12:12:12"));
        roomBeans.add(getRoomBean("3581", "2020年市场营销部业务规划会议", "在线", "2020-1-1 12:12:12"));
        roomBeans.add(getRoomBean("3582", "华为5G项目推进会", "在线", "2020-1-1 12:12:12"));
        roomBeans.add(getRoomBean("3583", "安徽省高速取消边界收费项目需求会", "在线", "2020-1-1 12:12:12"));
        roomBeans.add(getRoomBean("3584", "研发部例会", "在线", "2020-1-1 12:12:12"));
        roomBeans.add(getRoomBean("3585", "关于安防互联网直播项目的培训", "在线", "2020-1-1 12:12:12"));
        roomBeans.add(getRoomBean("3586", "EasyRTC新功能发布会", "在线", "2020-1-1 12:12:12"));
        roomBeans.add(getRoomBean("3587", "在线教育培训", "在线", "2020-1-1 12:12:12"));
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

        binding.activityEmptyView.setVisibility(View.GONE);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 1);
        binding.recyclerView.setLayoutManager(manager);
        RoomAdapter roomAdapter = new RoomAdapter(getContext(), roomBeans);
        binding.recyclerView.setAdapter(roomAdapter);
        roomAdapter.setClickListener(new RoomAdapter.MyClickListener() {
            @Override
            public void onItemClick(int pos) {
                roomBean = roomBeans.get(pos);
                connect(roomBean.getRoomNo());
            }
        });

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

    private RoomBean getRoomBean(String no, String name, String status, String time) {
        RoomBean room = new RoomBean();
        room.setRoomNo(no);
        room.setRoomName(name);
        room.setStatus(status);
        room.setCreateTime(time);

        return room;
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

//        options.displayName = userName.getEditableText().toString();
//        options.userEmail = email.getEditableText().toString();
        options.roomNumber = no;
        options.username = account.getUserName();
        options.password = account.getPwd();
//        options.serverAddress = serverEditText.getEditableText().toString();
//        options.displayName = options.username;
//        options.userEmail = options.username + "@easydarwin.org";

        room.setOptions(options);
        room.join();
        options.save();
    }
}
