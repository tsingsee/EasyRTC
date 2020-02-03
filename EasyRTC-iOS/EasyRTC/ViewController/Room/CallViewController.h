//
//  CallViewController.h
//  VenusApp
//
//  Created by leo on 2019/1/12.
//  Copyright © 2019年 easydarwin. All rights reserved.
//

#import "BaseViewController.h"
#import <EasyRTC/Options.h>

@interface CallViewController : BaseViewController

@property (nonatomic, strong) Options *options;

- (instancetype) initWithStoryborad;

@end
