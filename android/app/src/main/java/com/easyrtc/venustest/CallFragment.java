package com.easyrtc.venustest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.easyrtc.venus.Room;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CallFragment extends Fragment {
    Room room;

    @BindView(R.id.local_video_view)
    SurfaceViewRenderer localRender;

    @BindView(R.id.remote_video_view)
    SurfaceViewRenderer remoteRender;

    @BindView(R.id.speaker)
    ImageView speaker;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);
        ButterKnife.bind(this, view);

        room = RoomModel.getInstance().getRoom();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int width = 0;
        int height = 0;
        if (display.getWidth() * 3 / 4 < display.getHeight()) {
            width = display.getWidth();
            height = display.getWidth() * 3 / 4;
        } else {
            height = display.getHeight();
            width = display.getHeight() * 4 / 3;
        }
//        RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(width,height);
//        parms.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        remoteRender.setLayoutParams(parms);

        remoteRender.getLayoutParams().height = height;

        getChildFragmentManager().beginTransaction().add(R.id.user_fragment_holder, new UserInfoDialogFragment()).commit();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        localRender.init(room.getRootEglBase().getEglBaseContext(), null);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localRender.setEnableHardwareScaler(false);
        localRender.setZOrderMediaOverlay(true);

        remoteRender.init(room.getRootEglBase().getEglBaseContext(), null);
        remoteRender.setEnableHardwareScaler(false);
        remoteRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        if (room.isSpeakerOn()){
            speaker.setImageResource(R.mipmap.volume);
        }else{
            speaker.setImageResource(R.mipmap.mute);
        }
        room.startVideo(localRender, remoteRender);
    }

    @Override
    public void onStop() {
        super.onStop();

        room.stopVideo();
        localRender.release();
        remoteRender.release();

//        leave
        room.leave();
    }

    @OnClick(R.id.hangup)
    public void onHangupClick() {
        room.leave();
    }

    @OnClick(R.id.speakerButton)
    public void onSpeakerClick() {
        room.setSpeakerOn(!room.isSpeakerOn());
        Toast.makeText(getContext(), "Speaker: " + (room.isSpeakerOn() ? "ON" : "OFF"),
                Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.speaker)
    public void onSpeaker(){
        room.setSpeakerOn(!room.isSpeakerOn());
        if (room.isSpeakerOn()){
            Toast.makeText(getContext(), "扬声器打开",Toast.LENGTH_SHORT).show();
            speaker.setImageResource(R.mipmap.volume);
        }else{
            Toast.makeText(getContext(), "扬声器关闭",Toast.LENGTH_SHORT).show();
            speaker.setImageResource(R.mipmap.mute);
        }
    }

//    @OnClick(R.id.userInfoButton)
//    public void onUserInfoClick() {
//
//        dialog.show(getActivity().getSupportFragmentManager(),
//                UserInfoDialogFragment.class.getSimpleName());
//    }
}
