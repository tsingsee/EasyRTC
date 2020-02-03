//
//  LiveModel.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/17.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseModel.h"

@interface LiveModel : BaseModel

@property (nonatomic, copy) NSString *channelName;
@property (nonatomic, copy) NSString *url;
@property (nonatomic, copy) NSString *deviceType;   // ONVIF可控制， RTSP不可控制

@end
