//
//  LiveDetailViewController.h
//  EasyRTC
//
//  Created by liyy on 2020/2/7.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "BaseViewController.h"
#import "LiveSessionModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface LiveDetailViewController : BaseViewController

@property (nonatomic, strong) Session *session;

- (instancetype) initWithStoryborad;

@end

NS_ASSUME_NONNULL_END
