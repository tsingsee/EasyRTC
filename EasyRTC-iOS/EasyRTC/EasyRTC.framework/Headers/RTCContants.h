//
//  RTCContants.h
//  ChatDemo
//
//  Created by Harvey on 16/6/2.
//  Copyright © 2016年 Mianshi Co., Ltd. All rights reserved.
//

#ifndef RTCContants_h
#define RTCContants_h

#import <Foundation/Foundation.h>

//STUN服务器
//NSString *const RTCSTUNServerURL = @"stun:23.83.240.109:3478?transport=udp";

NSString *const RTCSTUNServerURL = @"stun:106.14.76.167:3478?transport=udp";

//TURN服务器
//NSString *const RTCTURNServerURL = @"turn:23.83.240.109:3478?transport=udp";
NSString *const RTCTURNServerURL = @"turn:106.14.76.167:3478?transport=udp";

static NSString const *kARDJoinResultKey = @"result";
static NSString const *kARDJoinResultParamsKey = @"params";
static NSString const *kARDJoinInitiatorKey = @"is_initiator";
static NSString const *kARDJoinRoomIdKey = @"room_id";
static NSString const *kARDJoinClientIdKey = @"client_id";
static NSString const *kARDJoinMessagesKey = @"messages";
static NSString const *kARDJoinWebSocketURLKey = @"wss_url";
static NSString const *kARDJoinWebSocketRestURLKey = @"wss_post_url";

#endif /* RTCContants_h */
