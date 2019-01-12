
# iOS API

## Room

会议室类

1. 设置登录参数，登录会议室
2. 加入会议室，获取成员信息，展示媒体信息

### - (NSInteger)join;
加入会议室.该接口会启动与服务器之间的登入操作，并在成功后开启会议室。

### - (void)leave;
离开会议室，并做清理工作

### - (UIView *)getRenderView;
获取直播画面

### - (NSMutableArray *)getUserInfoList
获取当前房间的所有用户

### - (void)muteClick:(UserInfo *)info;
设置外放声音开关

### - (void)loudspeakerClick;
外放声音开关的状态取反

### - (BOOL)isSpeakerEnable;
是否有外放声音

## Options

会议室参数。包括用户名，密码，服务器地址等信息。通过`Room.options`接口设置给`Room`对象

`Options`定义如下：

```objective-c
@interface Options : NSObject

// 服务器地址
@property (nonatomic, copy) NSString *serverAddress;
// 用户名
@property (nonatomic, copy) NSString *username;
// 密码
@property (nonatomic, copy) NSString *password;
// 用户名称
@property (nonatomic, copy) NSString *displayName;
// 用户邮箱
@property (nonatomic, copy) NSString *userEmail;
// 房间号
@property (nonatomic, copy) NSString *roomNumber;
```

## RoomStatusDelegate

状态回调。会议室状态或者登录状态发生更改时，通过该接口将状态回调回来。通过`self.room.roomDelegate = self;`来设置回调。
接口定义如下：

```objective-c
@protocol RoomStatusDelegate <NSObject>

- (void)onRoomStatusChange:(RoomStatus)roomStatus;
- (void)onRoomUserJoin:(UserInfo *)info;
- (void)onRoomUserLeave:(UserInfo *)info;
- (void)onRoomUserModify:(UserInfo *)info;

@end
```

其中RoomStatus状态有如下几种：

```objective-c
typedef NS_ENUM(NSUInteger, RoomStatus) {
    ROOM_STATUS_SIGNOUT,
    ROOM_STATUS_SIGNING,
    ROOM_STATUS_SIGNIN,
    ROOM_STATUS_CONNECTING,
    ROOM_STATUS_CONNECTED,
    ROOM_STATUS_DISCONNECTING,
};
```

## UserInfo
会议室内的用户信息

```objective-c
@interface UserInfo : NSObject

//  用户ID
@property (nonatomic, copy) NSString *userId;
// 用户显示名称
@property (nonatomic, copy) NSString *displayName;
// 邮箱
@property (nonatomic, copy) NSString *userEmail;
// 是否为静音
@property (nonatomic, assign) BOOL isMute;
// 用户说话声音能量
@property (nonatomic, assign) NSInteger energy;
@property (nonatomic, copy) NSString *callId;

@end
```

### **由于webrtc.a太大，请到 https://pan.baidu.com/s/1_UJU7u2QqC7I4EmAQJeN6g 提取码: fd21 下载。 放在 venus/venus/Vendors/WebRTC 下**
