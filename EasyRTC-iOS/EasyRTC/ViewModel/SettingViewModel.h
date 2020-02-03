//
//  SettingViewModel.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewModel.h"

@interface SettingViewModel : BaseViewModel

@property (nonatomic, strong) RACSubject *infoSubject;
@property (nonatomic, strong) RACCommand *infoCommand;

@property (nonatomic, strong) RACSubject *keySubject;
@property (nonatomic, strong) RACCommand *keyCommand;

@property (nonatomic, strong) RACSubject *submitSubject;
@property (nonatomic, strong) RACCommand *submitCommand;

@end
