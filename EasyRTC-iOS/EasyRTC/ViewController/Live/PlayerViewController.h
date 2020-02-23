//
//  RecordPlayerViewController.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/9/1.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewController.h"

@interface PlayerViewController : BaseViewController

@property (nonatomic, copy) NSString *urlStr;

- (instancetype) initWithStoryboard;

@end
