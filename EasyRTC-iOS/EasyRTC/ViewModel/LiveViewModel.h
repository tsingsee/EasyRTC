//
//  LiveViewModel.h
//  EasyRTC
//
//  Created by liyy on 2020/2/2.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "BaseViewModel.h"
#import "LiveSessionModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface LiveViewModel : BaseViewModel

@property (nonatomic, strong) LiveSessionModel *model;

@property (nonatomic, strong) RACSubject *dataSubject;
@property (nonatomic, strong) RACCommand *dataCommand;

@end

NS_ASSUME_NONNULL_END
