
# Android API

## Room

会议室类

1. 设置登录参数，登录会议室
2. 加入会议室，获取成员信息，展示媒体信息

### Options

会议室参数。包括用户名，密码，服务器地址等信息。通过`Room.setOptions`接口设置给`Room`对象

`Options`定义如下：

```java
public class Options {
    // ...
    // 服务器地址
    public String serverAddress = "easyrtc.easydss.com";
    // 用户名
    public String username = "1008";
    // 密码
    public String password = "1111";

    // 房间号
    public String roomNumber = "3581";
    // 用户名称
    public String displayName = ""; // optional
    // 用户邮箱
    public String userEmail = ""; // optional
    // ...
}
```

### StatusSink

状态回调。会议室状态或者登录状态发生更改时，通过该接口将状态回调回来。通过`Room.setStatusSink(StatusSink sink)`来设置回调。
接口定义如下：
```
public interface StatusSink {
    void onRoomStatusChange(RoomStatus roomStatus);
}
```
其中RoomStatus状态有如下几种：
```
public enum RoomStatus {
    ROOM_STATUS_SIGNOUT,
    ROOM_STATUS_SIGNING,
    ROOM_STATUS_SIGNIN,
    ROOM_STATUS_CONNECTING,
    ROOM_STATUS_CONNECTED,
    ROOM_STATUS_DISCONNECTING,
}
```

### Room.join

加入会议室.该接口会启动与服务器之间的登入操作，并在成功后开启会议室。


### Room.leave

离开会议室，并做清理工作

### SurfaceViewRenderer

视频渲染类。可提供本地视频渲染和远端视频渲染。

### Room.startVideo(SurfaceViewRenderer localRender, SurfaceViewRenderer remoteRender)

启动视频渲染。第一个参数为本地摄像头视频，第二个参数为服务器端合成的视频。


### Room.stopVideo

停止视频播放


### UserInfo

会议室内的用户信息

```
public class UserInfo {
    // ...
    private String id;      //  用户ID
    private String displayName; // 用户显示名称
    private String userEmail;   // 邮箱
    private boolean isMute;     // 是否为静音
    private int energy;     // 用户说话声音能量
    private String callId;
    // ...
}
```

### List<`UserInfo`> Room.getUserInfo()

获取会议室内的用户信息


### Room.mute(`UserInfo`)

使某个用户静音


### Room.unmute(`UserInfo`)

使某个用户取消静音


### Room.setSpeakerOn

打开、关闭扬声器

### Room.isSpeakerOn

判断扬声器是否启动

