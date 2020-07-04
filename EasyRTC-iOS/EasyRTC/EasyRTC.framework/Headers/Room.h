//
//  Room.h
//  venus
//
//  Created by Jac Chen on 2018/8/1.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Options.h"
#import "SignalChannel.h"
#import "InviteParameter.h"
#import "WebRTCClient.h"
#import "UserInfo.h"

typedef NS_ENUM(NSUInteger, RoomStatus) {
    ROOM_STATUS_SIGNOUT,
    ROOM_STATUS_SIGNING,
    ROOM_STATUS_SIGNIN,
    ROOM_STATUS_CONNECTING,
    ROOM_STATUS_CONNECTED,
    ROOM_STATUS_DISCONNECTING,
};

@protocol RoomStatusDelegate <NSObject>

- (void)onRoomStatusChange:(RoomStatus)roomStatus;
- (void)onRoomUserJoin:(UserInfo *)info;
- (void)onRoomUserLeave:(UserInfo *)info;
- (void)onRoomUserModify:(UserInfo *)info;

@end

@interface Room : NSObject

@property (nonatomic, strong) Options *options;
@property (nonatomic, assign, readonly) RoomStatus roomStatus;
@property (nonatomic, weak) id<RoomStatusDelegate> roomDelegate;

- (NSInteger)join;
- (void)leave;
- (NSMutableArray *)getUserInfoList;

- (void)muteClick:(UserInfo *)info;
- (void)loudspeakerClick;

- (BOOL)isSpeakerEnable;
- (UIView *)getRenderView;
- (void) swapFrontAndBackCameras;

@end
