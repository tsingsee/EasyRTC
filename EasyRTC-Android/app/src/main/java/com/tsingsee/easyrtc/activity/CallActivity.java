package com.tsingsee.easyrtc.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.model.Account;
import com.tsingsee.easyrtc.model.RoomBean;
import com.tsingsee.easyrtc.model.RoomModel;
import com.tsingsee.easyrtc.tool.SharedHelper;
import com.tsingsee.easyrtc.tool.ToastUtil;
import com.tsingsee.rtc.Options;
import com.tsingsee.rtc.Room;
import com.tsingsee.rtc.RoomStatus;
import com.tsingsee.rtc.StatusSink;
import com.tsingsee.rtc.XLog;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

public class CallActivity extends BaseActivity implements StatusSink {
    Room room;
    private Options options;

    @BindView(R.id.local_video_view)
    SurfaceViewRenderer localRender;

    @BindView(R.id.remote_video_view)
    SurfaceViewRenderer remoteRender;

    @BindView(R.id.speaker)
    ImageView speaker;
    @BindView(R.id.hangup)
    ImageView hangup;

    private long mExitTime;
    private boolean connected = true;
    private RoomBean roomBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        ButterKnife.bind(this);

        roomBean = (RoomBean) getIntent().getSerializableExtra("roomBean");
        room = RoomModel.getInstance().getRoom();
        options = new Options(this);

        remoteRenderSize();
    }

    @Override
    public void onStart() {
        super.onStart();

        room.setStatusSink(this);

        updateView();

        localRender.init(room.getRootEglBase().getEglBaseContext(), null);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localRender.setEnableHardwareScaler(false);
        localRender.setZOrderMediaOverlay(true);

        remoteRender.init(room.getRootEglBase().getEglBaseContext(), null);
        remoteRender.setEnableHardwareScaler(false);
        remoteRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        if (room.isSpeakerOn()) {
            speaker.setImageResource(R.drawable.open_mute);
        } else {
            speaker.setImageResource(R.drawable.close_mute);
        }

        room.startVideo(localRender, remoteRender);
    }

    @Override
    public void onStop() {
        super.onStop();

        room.stopVideo();
        localRender.release();
        remoteRender.release();

        room.setStatusSink(null);
    }

    @Override
    public void onBackPressed() {
        int orientation = getRequestedOrientation();
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            remoteRenderSize();

            return;
        }

        //与上次点击返回键时刻作差
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            //大于2000ms则认为是误操作，使用Toast进行提示
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            //并记录下本次点击“返回键”的时刻，以便下次进行判断
            mExitTime = System.currentTimeMillis();
        } else {
            room.leave();
//            System.exit(0);
            super.onBackPressed();
        }
    }

    @OnClick(R.id.hangup)
    public void onHangupClick() {
        if (connected) {
//            hangup.setImageResource(R.drawable.start_call);
            room.leave();
            finish();
        } else {
            hangup.setImageResource(R.drawable.stop_call);
            connect(roomBean.getRoomNo());
        }
    }

    @OnClick(R.id.speaker)
    public void onSpeaker() {
        room.setSpeakerOn(!room.isSpeakerOn());
        if (room.isSpeakerOn()) {
            // 扬声器打开
            speaker.setImageResource(R.drawable.open_mute);
        } else {
            // 扬声器关闭
            speaker.setImageResource(R.drawable.close_mute);
        }
    }

    @OnClick(R.id.switch_camera)
    public void switchCamera() {
        room.switchCamera();
    }

    @OnClick(R.id.switch_orientation)
    public void switchOrientation() {
        int orientation = getRequestedOrientation();
        if (orientation == SCREEN_ORIENTATION_UNSPECIFIED || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            Display display = getWindowManager().getDefaultDisplay();
            remoteRender.getLayoutParams().width = display.getHeight();
            remoteRender.getLayoutParams().height = display.getWidth();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            remoteRenderSize();
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
                finish();
                break;
            case ROOM_STATUS_SIGNING:
            case ROOM_STATUS_SIGNIN:
            case ROOM_STATUS_CONNECTING:

                break;
            case ROOM_STATUS_CONNECTED:
                break;
            case ROOM_STATUS_DISCONNECTING:
                finish();
            default:
                XLog.e("Invalid state: " + roomStatus);
                break;
        }
    }

    private void remoteRenderSize() {
        Display display = getWindowManager().getDefaultDisplay();
        remoteRender.getLayoutParams().height = display.getWidth() * 3 / 4;
        remoteRender.getLayoutParams().width = display.getWidth();
    }

    private void connect(String no) {
//        if (isConnecting) {
//            ToastUtil.show("连接中");
//            return;
//        }

        showHub("连接中");
//        isConnecting = true;
        room.setStatusSink(this);

        SharedHelper sp = new SharedHelper(this);
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
