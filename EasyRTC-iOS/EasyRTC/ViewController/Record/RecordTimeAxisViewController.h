//
//  RecordTimeAxisViewController.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/23.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewController.h"
#import "VideoModel.h"
#import <IJKMediaFramework/IJKMediaFramework.h>

/**
 时间轴的录像
 */
@interface RecordTimeAxisViewController : BaseViewController

@property (nonatomic, strong) Channel *channel;
@property (atomic, retain) id<IJKMediaPlayback> player;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *controlViewTop;
@property (weak, nonatomic) IBOutlet UIView *controlView;
@property (weak, nonatomic) IBOutlet UIButton *connectBtn;

@property (weak, nonatomic) IBOutlet UIView *dateView;
@property (weak, nonatomic) IBOutlet UIButton *dateBtn;
@property (weak, nonatomic) IBOutlet UIButton *afterBtn;
@property (weak, nonatomic) IBOutlet UIButton *beforeBtn;

@property (weak, nonatomic) IBOutlet UIView *btnView;
@property (weak, nonatomic) IBOutlet UIButton *downloadBtn;
@property (weak, nonatomic) IBOutlet UIButton *listBtn;
@property (weak, nonatomic) IBOutlet UIButton *pauseBtn;
@property (weak, nonatomic) IBOutlet UIButton *delBtn;

@property (weak, nonatomic) IBOutlet UIStackView *btnView2;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btnView2Height;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btnViewMarginBottom;
@property (weak, nonatomic) IBOutlet UIButton *liveBtn2;
@property (weak, nonatomic) IBOutlet UIButton *listBtn2;
@property (weak, nonatomic) IBOutlet UIButton *pauseBtn2;
@property (weak, nonatomic) IBOutlet UIButton *verticalBtn2;

- (instancetype) initWithStoryboard;

@end
