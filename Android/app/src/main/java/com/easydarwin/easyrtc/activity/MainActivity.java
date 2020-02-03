package com.easydarwin.easyrtc.activity;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.easydarwin.easyrtc.R;
import com.easydarwin.easyrtc.RoomModel;
import com.easydarwin.easyrtc.fragment.ConnectingFragment;
import com.easydarwin.easyrtc.fragment.DisconnectingFragment;
import com.easydarwin.easyrtc.fragment.LoginFragment;
import com.tsingsee.rtc.Room;
import com.tsingsee.rtc.RoomStatus;
import com.tsingsee.rtc.StatusSink;
import com.tsingsee.rtc.XLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements StatusSink {
    public static final String[] perms = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    Room room;

    @BindView(R.id.mainFrame)
    View mainFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        room = RoomModel.getInstance().getRoom();
        room.setContext(getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();

        room.setStatusSink(this);

        updateView();

        EasyPermissions.requestPermissions(this, null, 0, perms);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        updateView();

        if (!EasyPermissions.hasPermissions(this, perms)) {
            finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        room.setStatusSink(null);
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
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainFrame, new LoginFragment()).commitAllowingStateLoss();
                break;
            case ROOM_STATUS_SIGNING:
            case ROOM_STATUS_SIGNIN:
            case ROOM_STATUS_CONNECTING:
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainFrame, new ConnectingFragment()).commitAllowingStateLoss();
                break;
            case ROOM_STATUS_CONNECTED:
                Intent intent = new Intent(MainActivity.this, CallActivity.class);
                startActivity(intent);
                break;
            case ROOM_STATUS_DISCONNECTING:
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainFrame, new DisconnectingFragment()).commitAllowingStateLoss();
            default:
                XLog.e("Invalid state: " + roomStatus);
                break;
        }

        mainFrame.setVisibility(EasyPermissions.hasPermissions(this, perms) ? View.VISIBLE : View.INVISIBLE);
    }
}
