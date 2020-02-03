//
//  RoomRecordViewModel.h
//  EasyRTC
//
//  Created by liyy on 2020/2/2.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "BaseViewModel.h"
#import "Devices.h"

NS_ASSUME_NONNULL_BEGIN

@interface RoomRecordViewModel : BaseViewModel

@property (nonatomic, strong) Devices *model;

@property (nonatomic, strong) RACSubject *dataSubject;
@property (nonatomic, strong) RACCommand *dataCommand;

@end

NS_ASSUME_NONNULL_END
