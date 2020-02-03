//
//  RoomBean.h
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "BaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface RoomBean : BaseModel

@property (nonatomic, copy) NSString *roomNo;
@property (nonatomic, copy) NSString *roomName;
@property (nonatomic, copy) NSString *status;
@property (nonatomic, copy) NSString *createTime;

@property (nonatomic, assign) CGFloat height;

@end

NS_ASSUME_NONNULL_END
