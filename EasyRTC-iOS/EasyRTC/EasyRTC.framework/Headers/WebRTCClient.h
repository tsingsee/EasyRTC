//
//  WebRTCClient.h
//  ChatDemo
//
//  Created by Harvey on 16/5/30.
//  Copyright © 2016年 Mianshi Co., Ltd. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RTCView.h"

typedef NS_ENUM(NSInteger, ARDSignalingChannelState) {
    // State when disconnected.
    kARDSignalingChannelStateClosed,
    // State when connection is established but not ready for use.
    kARDSignalingChannelStateOpen,
    // State when connection is established and registered.
    kARDSignalingChannelStateRegistered,
    // State when connection encounters a fatal error.
    kARDSignalingChannelStateError
};

@protocol RtcDelegate <NSObject>
- (void)onIceCollectionDone;
- (void)onIceConnectFail;

@end

@interface WebRTCClient : NSObject

@property (nonatomic, copy)     NSString            *sessionID;
@property (nonatomic, copy)     NSString            *clientId;    /**< 客户端id, 用完需要清空 */
@property (nonatomic, weak)     id<RtcDelegate>     callback;
@property (nonatomic, assign)   BOOL                isSpeaker;
@property (nonatomic, strong)   RTCView             *rtcView;

+ (instancetype)sharedInstance;

+ (NSString *)randomRoomId;

//- (void)startEngine;
- (void)startEngineWithTurnServer:(NSString *)turnServer user:(NSString *)userName pwd:(NSString *)pwd;
- (void)stopEngine;

- (void)setRemoteDescription:(NSString *)sdp;

- (void)resizeViews;

- (NSString *)getLocalDescription;

- (void)audioEnable:(BOOL)on; // mute本地音频
- (void)loudspeakerClick; // speaker 开关
- (void) swapFrontAndBackCameras;

@end
