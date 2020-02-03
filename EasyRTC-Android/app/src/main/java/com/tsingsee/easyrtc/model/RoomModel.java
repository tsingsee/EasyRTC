package com.tsingsee.easyrtc.model;

import com.tsingsee.easyrtc.RTCApplication;
import com.tsingsee.rtc.Room;

public class RoomModel {
    private static RoomModel instance;

    private Room room;

    public Room getRoom() {
        if (null == room)
            room = Room.createInstance(RTCApplication.getContext());
        return room;
    }

    public static RoomModel getInstance() {
        if (null == instance)
            instance = new RoomModel();
        return instance;
    }
}
