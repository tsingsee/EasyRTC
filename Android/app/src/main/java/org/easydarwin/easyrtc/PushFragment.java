package org.easydarwin.easyrtc;


import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.adapters.DatePickerBindingAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import org.easydarwin.easyrtc.databinding.FragmentPushBinding;
import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.MediaStream;

import java.io.File;
import java.io.IOException;

import static org.easydarwin.easyrtc.TheApp.aio;

public class PushFragment extends Fragment implements SurfaceHolder.Callback {

    MediaStream mStream;
    FragmentPushBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_push, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SurfaceView sv = (SurfaceView) view.findViewById(R.id.pusher);
        sv.getHolder().addCallback(this);
        sv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStream != null) mStream.autoFocus();
            }
        });

        binding.switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStream.switchCamera();
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mStream = new MediaStream(getContext().getApplicationContext(), holder, aio);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            String id = preferences.getString("id", "");

            if(id.isEmpty() || id.length() <= 0)
            {
                id = String.valueOf((int)(10000 * Math.random()));
                preferences.edit().putString("id", id).apply();
            }

            mStream.startStream(preferences.getString("ip", getString(R.string.default_value)),
                    preferences.getString("port", getString(R.string.default_port)),
                    id,
                    new EasyPusher.OnInitPusherCallback() {
                        @Override
                        public void onCallback(int code) {
                            switch (code) {
                                case CODE.EASY_ACTIVATE_INVALID_KEY:
                                    sendMessage("无效Key");
                                    break;
                                case CODE.EASY_ACTIVATE_SUCCESS:
                                    sendMessage("激活成功");
                                    break;
                                case CODE.EASY_PUSH_STATE_CONNECTING:
                                    sendMessage("连接中");
                                    break;
                                case CODE.EASY_PUSH_STATE_CONNECTED:
                                    sendMessage("连接成功");
                                    break;
                                case CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                                    sendMessage("连接失败");
                                    break;
                                case CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                                    sendMessage("连接异常中断");
                                    break;
                                case CODE.EASY_PUSH_STATE_PUSHING:
                                    sendMessage("推流中");
                                    break;
                                case CODE.EASY_PUSH_STATE_DISCONNECTED:
                                    sendMessage("断开连接");
                                    break;
                                case CODE.EASY_ACTIVATE_PLATFORM_ERR:
                                    sendMessage("平台不匹配");
                                    break;
                                case CODE.EASY_ACTIVATE_COMPANY_ID_LEN_ERR:
                                    sendMessage("断授权使用商不匹配");
                                    break;
                                case CODE.EASY_ACTIVATE_PROCESS_NAME_LEN_ERR:
                                    sendMessage("进程名称长度不匹配");
                                    break;
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String msg) {
        binding.info.append("\n");
        binding.info.append(msg);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mStream.release();
    }
}
