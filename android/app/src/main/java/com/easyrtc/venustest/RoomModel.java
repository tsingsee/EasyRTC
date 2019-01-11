package com.easyrtc.venustest;

import com.easyrtc.venus.Room;

public class RoomModel {
    private static RoomModel instance;

    private Room room;

    public Room getRoom() {
        if (null == room)
            room = Room.createInstance(VenusApp.context);
        return room;
    }

    public static RoomModel getInstance() {
        if (null == instance)
            instance = new RoomModel();
        return instance;
    }
}
