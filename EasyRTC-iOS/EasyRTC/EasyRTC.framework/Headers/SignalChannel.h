//
//  SignalChannel.h
//  venus
//
//  Created by Jac Chen on 2018/8/1.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Options.h"
#import "SignalRequest.h"
#import "SRWebSocket.h"

typedef NS_ENUM(NSUInteger, RequestType) {
    RequestType_Invite,
    RequestType_Bye,
    RequestType_Login,
    RequestType_Subscribe,
    RequestType_GetUserList,
    RequestType_Broadcast,
};


#define     kACTION_LIVE_ARRAY_JOIN @"ACTION_LIVE_ARRAY_JOIN"
#define     kACTION_USER_INFO_CHANGE @"ACTION_USER_INFO_CHANGE"

@protocol SignalChannelDelegate <NSObject>
- (void)onChannelConnect;
- (void)onChannelDisconnect:(NSString *)callID;
- (void)onChannelError;
- (void)onChannelAnswer:(NSString *)callId andSdp:(NSString *)sdp;

@end

@interface SignalChannel : NSObject
@property (nonatomic, strong) SRWebSocket *webSocket;
@property (nonatomic, strong) Options *options;
@property (nonatomic, strong) NSString *sessionId;
@property (nonatomic, strong) NSMutableArray *userInfoList;

@property (nonatomic, weak) id<SignalChannelDelegate> callBack;

- (void)reconnectWithSessionID:(NSString *)sessionId;
- (void)sendSignalRequest:(SignalRequest *)request andType:(RequestType)type;
@end
