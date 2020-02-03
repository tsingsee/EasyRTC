package com.easydarwin.easyrtc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easydarwin.easyrtc.R;
import com.easydarwin.easyrtc.RoomModel;
import com.tsingsee.rtc.Room;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConnectingFragment extends Fragment {
    Room room;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connecting, container, false);
        ButterKnife.bind(this, view);

        room = RoomModel.getInstance().getRoom();

        return view;
    }

    @OnClick(R.id.hangup)
    public void onHangupClick() {
        room.leave();
    }
}
