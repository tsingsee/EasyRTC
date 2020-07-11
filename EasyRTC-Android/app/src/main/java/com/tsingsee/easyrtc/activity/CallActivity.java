package com.tsingsee.easyrtc.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.http.BaseEntity3;
import com.tsingsee.easyrtc.http.BaseObserver3;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.Account;
import com.tsingsee.easyrtc.model.RoomBean;
import com.tsingsee.easyrtc.model.RoomModel;
import com.tsingsee.easyrtc.model.UploadBean;
import com.tsingsee.easyrtc.model.UserInfo;
import com.tsingsee.easyrtc.tool.MD5Util;
import com.tsingsee.easyrtc.tool.SharedHelper;
import com.tsingsee.easyrtc.tool.ToastUtil;
import com.tsingsee.rtc.Options;
import com.tsingsee.rtc.Room;
import com.tsingsee.rtc.RoomStatus;
import com.tsingsee.rtc.StatusSink;
import com.tsingsee.rtc.XLog;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

public class CallActivity extends BaseActivity implements StatusSink {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0x111;

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
    @BindView(R.id.switch_camera)
    ImageView switchCamera;

    private long mExitTime;
    private boolean connected = true;
    private RoomBean.Data roomBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        ButterKnife.bind(this);

        roomBean = (RoomBean.Data) getIntent().getSerializableExtra("roomBean");
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
            connect(roomBean.getId());
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

    @OnClick(R.id.snap_camera)
    public void snapCamera() {
        // 获取实时图片
        onTakePicture();
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
        UserInfo user = sp.readUserInfo();

        options.roomNumber = no;
        options.username = account.getUserName();
        options.password = MD5Util.md5(account.getPwd());
        options.serverAddress = account.getServerAddress();

        if (user != null) {
            options.displayName = user.getId() + "@" + user.getUserName();
        }

        room.setOptions(options);
        room.join();
        options.save();
    }

    public void onTakePicture() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            screenshot();
        } else {
            requestWriteStorage();
        }
    }

    private void screenshot() {
        ToastUtil.show("截图完成");

        try {
            showHub("上传中");

            File file = saveBitmap(room.getCurrentBitmap());
            uploadImage(file);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            hideHub();
        }
    }

    private void requestWriteStorage() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            new AlertDialog.Builder(this).setMessage("EasyRTC需要使用写文件权限来抓拍").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(CallActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }).show();
        } else {
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the contacts-related task you need to do.

                    if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
                        screenshot();
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }

                return;
            }
        }
    }

    private File saveBitmap(Bitmap bitmap) throws IOException {
        String dir = Environment.getExternalStorageDirectory() + "/EasyRTC";
        File f = new File(dir);
        f.mkdirs();

        File file = new File(f, "snap.jpg");
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private void uploadImage(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        RequestBody id = RequestBody.create(MediaType.parse("multipart/form-data"), roomBean.getId());

        Observable<BaseEntity3<UploadBean>> observable = RetrofitFactory.getRetrofitService().uploadImages(id, part);
        observable.compose(compose(this.<BaseEntity3<UploadBean>>bindToLifecycle()))
                .subscribe(new BaseObserver3<UploadBean>(this, dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(UploadBean model) {
                        hideHub();
                        Log.i("CallActivity", model.getUrl());
                        ToastUtil.show("截图上传成功");
                    }

                    @Override
                    protected void loginSuccess() {

                    }
                });
    }
}
