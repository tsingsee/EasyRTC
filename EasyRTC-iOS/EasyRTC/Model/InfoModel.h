//
//  InfoModel.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseModel.h"

@interface InfoModel : BaseModel

@property (nonatomic, copy) NSString *hardware;
@property (nonatomic, copy) NSString *interfaceVersion;
@property (nonatomic, copy) NSString *liveCount;
@property (nonatomic, copy) NSString *productType;
@property (nonatomic, copy) NSString *runningTime;
@property (nonatomic, copy) NSString *server;
@property (nonatomic, copy) NSString *validity;
@property (nonatomic, copy) NSString *virtualLiveCount;

@end
