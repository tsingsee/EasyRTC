//
//  VideoModel.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseModel.h"

@interface Channel : BaseModel

@property (nonatomic, copy) NSString *channel; // 通道号
@property (nonatomic, copy) NSString *name;    // 通道名称
@property (nonatomic, copy) NSString *online;  // 是否在线 1在线/0离线
@property (nonatomic, copy) NSString *snapUrl; // 快照地址 返回为快照的相对网络地址
@property (nonatomic, copy) NSString *errorString;

/*
 动作命令:stop停止、up向上移动、down向下移动、left向左移动、right向右移动、zoomin、zoomout、focusin、focusout、aperturein、apertureout
 */
@property (nonatomic, copy) NSString *command;

@end

@interface VideoModel : BaseModel

@property (nonatomic, copy) NSString *channelCount;
@property (nonatomic, strong) NSMutableArray<Channel *> *channels;

@end
