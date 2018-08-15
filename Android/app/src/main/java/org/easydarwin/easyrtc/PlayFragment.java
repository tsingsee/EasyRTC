package org.easydarwin.easyrtc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.SurfaceTexture;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.easydarwin.easyrtc.databinding.FragmentPlayBinding;
import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTSPClient;

import static org.easydarwin.easyrtc.TheApp.aio;
import static org.easydarwin.easyrtc.TheApp.bus;


public class PlayFragment extends Fragment implements TextureView.SurfaceTextureListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";
    public static final int RESULT_REND_STARTED = 1;
    public static final int RESULT_REND_VIDEO_DISPLAYED = 2;
    public static final int RESULT_REND_STOPED = -1;

    protected static final String TAG = "PlayFragment";

    // TODO: Rename and change types of parameters
    protected String mUrl;
    protected int mType = RTSPClient.TRANSTYPE_TCP;

    protected EasyRTSPClient mStreamRender;
    protected ResultReceiver mResultReceiver;
    protected int mWidth;
    protected int mHeight;
    private TextureView mSurfaceView;
    private MediaScannerConnection mScanner;
    private FragmentPlayBinding binding;
    private SurfaceTexture mTexture;
    private String peer_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_PARAM1);
            mType = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_play, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSurfaceView = (TextureView) view.findViewById(R.id.player);
        mSurfaceView.setOpaque(false);
        mSurfaceView.setSurfaceTextureListener(this);
        mResultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                Activity activity = getActivity();
                if (activity == null) return;
                if (resultCode == EasyRTSPClient.RESULT_VIDEO_DISPLAYED) {

                    onVideoDisplayed();
                } else if (resultCode == EasyRTSPClient.RESULT_VIDEO_SIZE) {
                    mWidth = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_WIDTH);
                    mHeight = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_HEIGHT);
                } else if (resultCode == EasyRTSPClient.RESULT_TIMEOUT) {
                    new AlertDialog.Builder(getActivity()).setMessage("试播时间到").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
                } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO) {
                    new AlertDialog.Builder(getActivity()).setMessage("音频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
                } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO) {
                    new AlertDialog.Builder(getActivity()).setMessage("视频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
                } else if (resultCode == EasyRTSPClient.RESULT_EVENT) {
                    int errorcode = resultData.getInt("errorcode");
                    if (errorcode != 0) {
                        stopRending();
                        binding.retry.setVisibility(View.VISIBLE);
                        binding.stop.setVisibility(View.GONE);
                    }binding.info.append("\n");
                    binding.info.append(resultData.getString("event-msg"));
                }
            }
        };

        binding.retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRending();
                startRending(mTexture);
                binding.stop.setVisibility(View.VISIBLE);
                binding.retry.setVisibility(View.GONE);
            }
        });

        binding.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRending();
                binding.stop.setVisibility(View.GONE);
                binding.retry.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String peer_id = preferences.getString("peer_id", null);
        if (peer_id != null){
            this.peer_id = peer_id;
            if (mTexture != null)
            startRending(mTexture);
        }else{
            stopRending();
        }
    }

    private void onVideoDisplayed() {
        Log.i(TAG, String.format("VIDEO DISPLAYED!!!!%d*%d", mWidth, mHeight));
        binding.progress.setVisibility(View.GONE);
    }

    protected void startRending(SurfaceTexture surface) {
        binding.retry.setVisibility(View.GONE);
        binding.stop.setVisibility(View.VISIBLE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String ip = preferences.getString("ip", getString(R.string.default_value));
        String port = preferences.getString("port", getString(R.string.default_port));
        peer_id = preferences.getString("peer_id", null);
        if (TextUtils.isEmpty(peer_id)){
            binding.info.append("\n");
            binding.info.append("请在设置里输入对方的ID");
            return;
        }
        if (mStreamRender != null){
            mStreamRender.stop();
        }
        String key = "79393674363536526D34324147546862704D666C792B6C76636D63755A57467A65575268636E64706269356C59584E35636E526A766C634D5671442F532B4E4659584E355247467964326C755647566862556C7A5647686C516D567A644541794D4445345A57467A65513D3D";
        mStreamRender = new EasyRTSPClient(getContext(), key ,surface,bus, mResultReceiver);
        try {
            mStreamRender.start(String.format("rtsp://%s:%s/%s.sdp", ip, port, peer_id), mType, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", "");
//            mStreamRender.start("rtsp://114.55.107.180:10554/001001000kim/1.sdp", mType, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", "");
            binding.progress.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mTexture = surface;
        startRending(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopRending();
        return true;
    }

    private void stopRending() {
        if (mStreamRender != null) {
            mStreamRender.stop();
            mStreamRender = null;
        }
        binding.retry.setVisibility(View.VISIBLE);
        binding.stop.setVisibility(View.GONE);
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        stopRending();
        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
