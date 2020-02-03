//
//  RecordTimeAxisViewController.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/23.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "RecordTimeAxisViewController.h"
#import <VideoToolbox/VideoToolbox.h>
#import <AudioToolbox/AudioToolbox.h>
#import <CommonCrypto/CommonDigest.h>
#import <QuartzCore/QuartzCore.h>
#import <AVFoundation/AVFoundation.h>
#import "RecordListViewController.h"
#import "CalendarViewController.h"
#import "DownloadViewController.h"
#import "YYScrollRulerView.h"
#import "VVeboImageView.h"
#import "RecordViewModel.h"
#import "LiveModel.h"
#import "Masonry.h"
#import "DateUtil.h"
#import "UIButton+EasyButton.h"

@interface RecordTimeAxisViewController ()<RulerDelegate>

@property (nonatomic, strong) RecordViewModel *viewModel;

@property (nonatomic, assign) CGRect playerFrame;
@property (nonatomic, assign) BOOL isFullScreen;

@property (nonatomic, strong) YYScrollRulerView *rulerView;
@property (nonatomic, strong) YYScrollRulerView *rulerView2;
@property (nonatomic, strong) VVeboImageView *loadIV;

@property (nonatomic, assign) BOOL isPushRecord;
@property (nonatomic, assign) BOOL isTouchBegin;
@property (nonatomic, assign) NSInteger currentValue;

@end

@implementation RecordTimeAxisViewController

- (instancetype) initWithStoryboard {
    return [[UIStoryboard storyboardWithName:@"Record" bundle:nil] instantiateViewControllerWithIdentifier:@"RecordTimeAxisViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationItem.title = self.channel.name;
    [self setView];
    
    NSData *data = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"loading.gif" ofType:nil]];
    self.loadIV = [[VVeboImageView alloc] initWithImage:[VVeboImage gifWithData:data]];
    [self.view addSubview:self.loadIV];
    [self.loadIV makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view.mas_centerX);
        make.top.equalTo(@180);
        make.width.equalTo(@50);
        make.height.equalTo(@50);
    }];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self installMovieNotificationObservers];
    
    if (self.isPushRecord) {
        self.isPushRecord = NO;
        
        if (!self.viewModel.curRecord) {
            self.viewModel.curRecord = self.viewModel.records.firstObject;
        }
        
        [self normarlScreen:nil];// 恢复竖屏
        [self playAgain];
        
        [self refreshMediaControl];
    }
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(refreshMediaControl) object:nil];
    
    [self.player shutdown];
    [self.player.view removeFromSuperview];
    self.player = nil;
    
    [self removeMovieNotificationObservers];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (BOOL)prefersStatusBarHidden {
    if (self.isFullScreen) {
        return YES;
    } else {
        return NO;
    }
}

#pragma mark - private method

- (void) setView {
    self.view.backgroundColor = UIColorFromRGB(0xECF0F6);
    
    self.connectBtn.hidden = YES;
    [[self.connectBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(id x) {
        [self playAgain];
    }];
    
    [self setDateBtnTitle];
    
    EasyViewBorderRadius(_dateView, 20, 1, UIColorFromRGB(0xb2b2b2));
    
    [_beforeBtn setImage:[UIImage imageNamed:@"time_before"] forState:UIControlStateNormal];
    [_beforeBtn setImage:[UIImage imageNamed:@"time_before_on"] forState:UIControlStateHighlighted];
    [_afterBtn setImage:[UIImage imageNamed:@"time_after"] forState:UIControlStateNormal];
    [_afterBtn setImage:[UIImage imageNamed:@"time_after_on"] forState:UIControlStateHighlighted];
    [_dateBtn setImage:[UIImage imageNamed:@"time_calendar"] forState:UIControlStateNormal];
    [_dateBtn setImage:[UIImage imageNamed:@"time_calendar_on"] forState:UIControlStateHighlighted];
    [_dateBtn setTitleColor:UIColorFromRGB(0x737373) forState:UIControlStateNormal];
    [_dateBtn setTitleColor:UIColorFromRGB(0x58b9fb) forState:UIControlStateHighlighted];
    
    [_downloadBtn setImage:[UIImage imageNamed:@"time_down"] forState:UIControlStateNormal];
    [_downloadBtn setImage:[UIImage imageNamed:@"time_down_on"] forState:UIControlStateHighlighted];
    [_downloadBtn setTitleColor:UIColorFromRGB(0x737373) forState:UIControlStateNormal];
    [_downloadBtn setTitleColor:UIColorFromRGB(0x58b9fb) forState:UIControlStateHighlighted];
    
    [_listBtn setImage:[UIImage imageNamed:@"time_list"] forState:UIControlStateNormal];
    [_listBtn setImage:[UIImage imageNamed:@"time_list_on"] forState:UIControlStateHighlighted];
    [_listBtn setTitleColor:UIColorFromRGB(0x737373) forState:UIControlStateNormal];
    [_listBtn setTitleColor:UIColorFromRGB(0x58b9fb) forState:UIControlStateHighlighted];
    [_listBtn2 setImage:[UIImage imageNamed:@"horizontal_list"] forState:UIControlStateNormal];
    [_listBtn2 setImage:[UIImage imageNamed:@"horizontal_list_on"] forState:UIControlStateHighlighted];
    
    [_pauseBtn setImage:[UIImage imageNamed:@"time_pause"] forState:UIControlStateNormal];
    [_pauseBtn setImage:[UIImage imageNamed:@"time_pause_on"] forState:UIControlStateSelected];
    [_pauseBtn setTitleColor:UIColorFromRGB(0x737373) forState:UIControlStateNormal];
    [_pauseBtn setTitleColor:UIColorFromRGB(0x58b9fb) forState:UIControlStateSelected];
    [_pauseBtn2 setImage:[UIImage imageNamed:@"horizontal_pause"] forState:UIControlStateNormal];
    [_pauseBtn2 setImage:[UIImage imageNamed:@"horizontal_play"] forState:UIControlStateSelected];
    
    [_delBtn setImage:[UIImage imageNamed:@"time_delete"] forState:UIControlStateNormal];
    [_delBtn setImage:[UIImage imageNamed:@"time_delete_on"] forState:UIControlStateHighlighted];
    [_delBtn setTitleColor:UIColorFromRGB(0x737373) forState:UIControlStateNormal];
    [_delBtn setTitleColor:UIColorFromRGB(0x58b9fb) forState:UIControlStateHighlighted];
    
    [_downloadBtn verticalImageAndTitle:4];
    [_listBtn verticalImageAndTitle:4];
    [_pauseBtn verticalImageAndTitle:4];
    [_delBtn verticalImageAndTitle:4];
    
    self.btnView2.hidden = YES;
    self.btnView2.backgroundColor = UIColorFromRGBA(0xffffff, 0.5);
    self.btnView2Height.constant = EasyScreenWidth;
    self.btnViewMarginBottom.constant = -EasyScreenWidth / 2 + 20;
}

- (void) setDateBtnTitle {
    NSString *dateStr = [NSString stringWithFormat:@"  %@", [DateUtil dateYYYY_MM_DD:self.viewModel.selectDate]];
    [self.dateBtn setTitle:dateStr forState:UIControlStateNormal];
}

- (void) playAgain {
    self.connectBtn.hidden = YES;
    [self.loadIV playGif];
    self.loadIV.hidden = NO;
    if (self.isFullScreen) {
        [self.loadIV updateConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(@((EasyScreenHeight-50)/2));
        }];
    } else {
        [self.loadIV updateConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(@180);
        }];
    }
    
    if (self.player) {
        [self.player shutdown];
        [self.player.view removeFromSuperview];
        self.player = nil;
    }
    
    [self play];
}

- (void) play {
    if (!self.viewModel.curRecord || !self.viewModel.curRecord.hls) {
        [self noRecord];
        
        return;
    }
    
#ifdef DEBUG
    [IJKFFMoviePlayerController setLogReport:YES];
    [IJKFFMoviePlayerController setLogLevel:k_IJK_LOG_DEBUG];
#else
    [IJKFFMoviePlayerController setLogReport:NO];
    [IJKFFMoviePlayerController setLogLevel:k_IJK_LOG_INFO];
#endif
    
    [IJKFFMoviePlayerController checkIfFFmpegVersionMatch:YES];
    
    IJKFFOptions *options = [IJKFFOptions optionsByDefault];
    [options setFormatOptionValue:@"tcp" forKey:@"rtsp_transport"];
    [options setFormatOptionIntValue:1000000 forKey:@"analyzeduration"];    // 21s
    [options setFormatOptionIntValue:204800 forKey:@"probesize"];
    [options setFormatOptionIntValue:0 forKey:@"auto_convert"];
    [options setFormatOptionIntValue:1 forKey:@"reconnect"];
    [options setFormatOptionIntValue:10 forKey:@"timeout"];
    [options setPlayerOptionIntValue:0 forKey:@"packet-buffering"];
    [options setFormatOptionValue:@"nobuffer" forKey:@"fflags"];
    
    // RTSP的话,iformat是rtsp,rtmp是flv,m3u8是hls
    if ([[self.viewModel.curRecord.hls substringToIndex:4] isEqualToString:@"rtmp"]) {
        [options setFormatOptionValue:@"flv" forKey:@"iformat"];
    } else if ([[self.viewModel.curRecord.hls substringToIndex:4] isEqualToString:@"m3u8"]) {
        [options setFormatOptionValue:@"hls" forKey:@"iformat"];
    } else {
        [options setFormatOptionValue:@"rtsp" forKey:@"iformat"];
    }
    
    NSURL *url = [NSURL URLWithString:self.viewModel.curRecord.hls];
    self.player = [[IJKFFMoviePlayerController alloc] initWithContentURL:url withOptions:options key:PlayerKey];
    
    if (self.player) {
        self.player.view.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        self.player.view.frame = self.view.bounds;
        
        self.player.scalingMode = IJKMPMovieScalingModeAspectFit;
        self.player.shouldAutoplay = YES;
        self.view.autoresizesSubviews = YES;
        [self.view insertSubview:self.player.view atIndex:0];
        
        [self.player prepareToPlay];
        
        // 点击画面
        UITapGestureRecognizer *gesture = [[UITapGestureRecognizer alloc] initWithActionBlock:^(id  _Nonnull sender) {
            if (self.isFullScreen) {
                self.btnView2.hidden = !self.btnView2.hidden;
                self.rulerView2.hidden = !self.rulerView2.hidden;
            }
        }];
        [self.player.view addGestureRecognizer:gesture];
    } else {
        [self showTextHubWithContent:@"播放器初始化失败"];// Key不合法或者已过期
    }
}

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    [self.viewModel.tokenSubject subscribeNext:^(RACCommand *command) {
        [self loginFirstWithCommend:command];
    }];
    
    [self.viewModel.querydailyCommand execute:self.channel.channel];
    
    [self.viewModel.querydailySubject subscribeNext:^(id x) {
        if (self.isPushRecord) {
            [self addRulerView];
        } else {
            if (x) {
                [self playAgain];
                [self addRulerView];
            } else {
                [self noRecord];
            }
        }
    }];
    
    [self.viewModel.removeSubject subscribeNext:^(NSString *res) {
        [self hideHub];
        
        if (res) {
//            [self showTextHubWithContent:res];
        }
        
        // 删除录像后 刷新数据
        [self.viewModel.querydailyCommand execute:self.channel.channel];
    }];
}

- (void) noRecord {
    [self showTextHubWithContent:@"暂无录像"];
    
    self.view.backgroundColor = UIColorFromRGB(0x111111);
    self.controlView.hidden = NO;
    [self.loadIV pauseGif];
    self.loadIV.hidden = YES;
    
    if (self.player) {
        [self.player shutdown];
        [self.player.view removeFromSuperview];
        self.player = nil;
    }
    
    if (self.rulerView) {
        self.rulerView.hidden = YES;
    }
}

#pragma mark - getter

- (RecordViewModel *) viewModel {
    if (!_viewModel) {
        _viewModel = [[RecordViewModel alloc] init];
    }
    
    return _viewModel;
}

#pragma mark - click

// 竖屏
- (IBAction)normarlScreen:(id)sender {
    self.isFullScreen = NO;
    
    [UIView animateWithDuration:0.5 animations:^{
        self.navigationController.navigationBarHidden = NO;
        if ([self respondsToSelector:@selector(setNeedsStatusBarAppearanceUpdate)]) {
            [self prefersStatusBarHidden];
            [self performSelector:@selector(setNeedsStatusBarAppearanceUpdate)];
        }
        
        self.controlView.hidden = NO;
        self.btnView2.hidden = YES;
        self.btnView2.transform = CGAffineTransformIdentity;
        
        self.player.view.bounds = CGRectMake(0, 0, self.playerFrame.size.width, self.playerFrame.size.height);
        self.player.view.center = CGPointMake(EasyScreenWidth / 2, EasyBarHeight + EasyNavHeight + self.playerFrame.size.height / 2);
        self.player.view.transform = CGAffineTransformIdentity;
        
        self.rulerView2.transform = CGAffineTransformIdentity;
        self.rulerView2.hidden = YES;
    }];
}

// 横屏
- (IBAction)fullScreen:(id)sender {
    if (!self.loadIV.isHidden) {
        return;
    }
    
    self.isFullScreen = YES;
    
    [UIView animateWithDuration:0.5 animations:^{
        self.navigationController.navigationBarHidden = YES;
        if ([self respondsToSelector:@selector(setNeedsStatusBarAppearanceUpdate)]) {
            [self prefersStatusBarHidden];
            [self performSelector:@selector(setNeedsStatusBarAppearanceUpdate)];
        }
        
        self.controlView.hidden = YES;
        self.btnView2.hidden = NO;
        self.btnView2.transform = CGAffineTransformMakeRotation(M_PI_2);
        
        self.player.view.bounds = CGRectMake(0, 0, EasyScreenHeight, EasyScreenWidth);
        self.player.view.center = CGPointMake(EasyScreenWidth / 2, EasyScreenHeight / 2);
        self.player.view.transform = CGAffineTransformMakeRotation(M_PI_2);
        
        self.rulerView2.transform = CGAffineTransformMakeRotation(M_PI_2);
        self.rulerView2.hidden = NO;
    }];
}

// 返回直播
- (IBAction)live:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

// 前一天
- (IBAction)beforeDate:(id)sender {
    if (!self.loadIV.isHidden) {
        return;
    }
    
    self.viewModel.selectDate = [[NSDate alloc] initWithTimeIntervalSinceReferenceDate:([self.viewModel.selectDate timeIntervalSinceReferenceDate] - 24 * 3600)];
    [self.viewModel.querydailyCommand execute:self.channel.channel];
    
    [self setDateBtnTitle];
}

// 后一天
- (IBAction)afterDate:(id)sender {
    if (!self.loadIV.isHidden) {
        return;
    }
    
    self.viewModel.selectDate = [[NSDate alloc] initWithTimeIntervalSinceReferenceDate:([self.viewModel.selectDate timeIntervalSinceReferenceDate] + 24 * 3600)];
    [self.viewModel.querydailyCommand execute:self.channel.channel];
    
    [self setDateBtnTitle];
}

// 打开日历
- (IBAction)selectDate:(id)sender {
    self.isPushRecord = YES;
    
    CalendarViewController *controller = [[CalendarViewController alloc] initWithStoryborad];
    controller.channelID = self.channel.channel;
    controller.chooseMonth = self.viewModel.selectDate;
    [controller.subject subscribeNext:^(NSDate *date) {
        self.viewModel.selectDate = date;
        self.isPushRecord = NO;
        
        [self setDateBtnTitle];
        
        [self.viewModel.querydailyCommand execute:self.channel.channel];
    }];
    [self basePushViewController:controller];
}

// 下载
- (IBAction)downloadVideo:(id)sender {
    if (!self.viewModel.curRecord) {
        return;
    }
    
    [self downloadRecord];
}

// 视图列表
- (IBAction)reocrdList:(id)sender {
    self.isPushRecord = YES;
    
    RecordListViewController *controller = [[RecordListViewController alloc] initWithStoryboard];
    controller.channel = self.channel;
    controller.selectDate = self.viewModel.selectDate;
    [controller.subject subscribeNext:^(id x) {
        // 删除录像了，则需要更新数据
        [self.viewModel.querydailyCommand execute:self.channel.channel];
    }];
    [self basePushViewController:controller];
}

// 暂停
- (IBAction)pause:(id)sender {
    if (!self.loadIV.isHidden) {
        return;
    }
    
    if (!self.viewModel.curRecord) {
        return;
    }
    
    _pauseBtn.selected = !_pauseBtn.selected;
    _pauseBtn2.selected = !_pauseBtn2.selected;
    
    if (_pauseBtn.selected) {
        [self.player pause];
    } else {
        [self.player play];
    }
}

// 删除
- (IBAction)delete:(id)sender {
    if (!self.viewModel.curRecord) {
        return;
    }
    
    [self deleteRecord];
}

- (void) deleteRecord {
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:@"提示"
                                                                        message:@"确认删除该时段的录像吗？"
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self showHubWithLoadText:@"删除中"];
        [self.viewModel.removeCommand execute:self.channel.channel];
    }];
    [controller addAction:cancelAction];
    [controller addAction:okAction];
    
    [self presentViewController:controller animated:YES completion:nil];
}

- (void) downloadRecord {
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:@"提示"
                                                                        message:@"确认下载该时段的录像吗？"
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        DownloadViewController *controller = [[DownloadViewController alloc] initWithStoryborad];
        controller.channel = self.channel;
        controller.curRecord = self.viewModel.curRecord;
        controller.modalPresentationStyle = UIModalPresentationOverCurrentContext;
        [self presentViewController:controller animated:YES completion:nil];
    }];
    [controller addAction:cancelAction];
    [controller addAction:okAction];
    
    [self presentViewController:controller animated:YES completion:nil];
}

#pragma mark - Notification

-(void)removeMovieNotificationObservers {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

// Register observers for the various movie object notifications
-(void)installMovieNotificationObservers {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(loadStateDidChange:)
                                                 name:IJKMPMoviePlayerLoadStateDidChangeNotification
                                               object:_player];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(moviePlayBackDidFinish:)
                                                 name:IJKMPMoviePlayerPlaybackDidFinishNotification
                                               object:_player];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(mediaIsPreparedToPlayDidChange:)
                                                 name:IJKMPMediaPlaybackIsPreparedToPlayDidChangeNotification
                                               object:_player];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(moviePlayBackStateDidChange:)
                                                 name:IJKMPMoviePlayerPlaybackStateDidChangeNotification
                                               object:_player];
}

- (void)loadStateDidChange:(NSNotification*)notification {
    IJKMPMovieLoadState loadState = _player.loadState;
    if ((loadState & IJKMPMovieLoadStatePlaythroughOK) != 0) {
        NSLog(@"loadStateDidChange: IJKMPMovieLoadStatePlaythroughOK: %d\n", (int)loadState);
        
        // 更新UI位置
        CGSize s = self.player.naturalSize;
        if (s.width == 0) {
            self.connectBtn.hidden = NO;
        } else {
            CGFloat height = EasyScreenWidth * s.height / s.width;
            self.playerFrame = CGRectMake(0, EasyBarHeight + EasyNavHeight, EasyScreenWidth, height);
            self.player.view.frame = self.playerFrame;
            self.controlViewTop.constant = height;
            self.controlView.hidden = NO;
        }
        
        if (self.isFullScreen) {
            self.player.view.bounds = CGRectMake(0, 0, EasyScreenHeight, EasyScreenWidth);
            self.player.view.center = CGPointMake(EasyScreenWidth / 2, EasyScreenHeight / 2);
            self.player.view.transform = CGAffineTransformMakeRotation(M_PI_2);
            self.controlView.hidden = YES;
        }
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self.loadIV pauseGif];
            self.loadIV.hidden = YES;
        });
    } else if ((loadState & IJKMPMovieLoadStateStalled) != 0) {
        NSLog(@"loadStateDidChange: IJKMPMovieLoadStateStalled: %d\n", (int)loadState);
    } else {
        NSLog(@"loadStateDidChange: ???: %d\n", (int)loadState);
    }
}

- (void)moviePlayBackDidFinish:(NSNotification*)notification {
    int reason = [[[notification userInfo] valueForKey:IJKMPMoviePlayerPlaybackDidFinishReasonUserInfoKey] intValue];
    switch (reason) {
        case IJKMPMovieFinishReasonPlaybackEnded:
            NSLog(@"playbackStateDidChange: IJKMPMovieFinishReasonPlaybackEnded: %d\n", reason);
//            [self stopSpeedTime];[startHUD hideAnimated:YES];
            break;
        case IJKMPMovieFinishReasonUserExited:
            NSLog(@"playbackStateDidChange: IJKMPMovieFinishReasonUserExited: %d\n", reason);
//            [self stopSpeedTime];[startHUD hideAnimated:YES];
            break;
        case IJKMPMovieFinishReasonPlaybackError:
            NSLog(@"playbackStateDidChange: IJKMPMovieFinishReasonPlaybackError: %d (视频无法播放)\n", reason);
            [self play];// 断开重连
            break;
        default:
            NSLog(@"playbackPlayBackDidFinish: ???: %d\n", reason);
            break;
    }
}

- (void)mediaIsPreparedToPlayDidChange:(NSNotification*)notification {
    NSLog(@"mediaIsPreparedToPlayDidChange\n");
}

- (void)moviePlayBackStateDidChange:(NSNotification*)notification {
    switch (_player.playbackState) {
        case IJKMPMoviePlaybackStateStopped: {
            NSLog(@"IJKMPMoviePlayBackStateDidChange %d: stoped", (int)_player.playbackState);
            break;
        }
        case IJKMPMoviePlaybackStatePlaying: {
            NSLog(@"IJKMPMoviePlayBackStateDidChange %d: playing", (int)_player.playbackState);
            
            if (self.viewModel.curRecord.currentPlayTime > 0) {
                self.player.currentPlaybackTime = self.viewModel.curRecord.currentPlayTime;
                self.viewModel.curRecord.currentPlayTime = -1;
            }
            
            break;
        }
        case IJKMPMoviePlaybackStatePaused: {
            NSLog(@"IJKMPMoviePlayBackStateDidChange %d: paused", (int)_player.playbackState);
            break;
        }
        case IJKMPMoviePlaybackStateInterrupted: {
            NSLog(@"IJKMPMoviePlayBackStateDidChange %d: interrupted", (int)_player.playbackState);
            break;
        }
        case IJKMPMoviePlaybackStateSeekingForward:
        case IJKMPMoviePlaybackStateSeekingBackward: {
            NSLog(@"IJKMPMoviePlayBackStateDidChange %d: seeking", (int)_player.playbackState);
            break;
        }
        default: {
            NSLog(@"IJKMPMoviePlayBackStateDidChange %d: unknown", (int)_player.playbackState);
            break;
        }
    }
}

#pragma mark - RulerView

- (YYScrollRulerView *) scrollRulerView:(CGRect)frame {
    YYScrollRulerView *rulerView = [[YYScrollRulerView alloc] initWithFrame:frame];
    rulerView.rulerDelegate = self;
    rulerView.lockMax = 24 * 60 * 60;
    rulerView.unitValue = 60;
    rulerView.step = 5;
    rulerView.isTimeAlias = YES;
    rulerView.rulerBackgroundColor = YYUIColorFromRGB(0XF2F2F2);
    rulerView.segmentColor = customColorMake(88, 185, 251);
    
    return rulerView;
}

- (void) addRulerView {
    if (!self.rulerView) {
        CGRect f = CGRectMake(0, self.dateView.frame.origin.y + self.dateView.frame.size.height + 12, cy_ScreenW, 75);
        self.rulerView = [self scrollRulerView:f];
        [self.controlView addSubview:self.rulerView];
        
        CGFloat w = EasyScreenHeight-16, h = 76;
        CGRect f1 = CGRectMake(-w / 2 + h/2, cy_ScreenW/2+h, w, h);
        self.rulerView2 = [self scrollRulerView:f1];
        [self.view addSubview:self.rulerView2];
    }
    
    // 填充值
    NSMutableArray *segments = [[NSMutableArray alloc] init];
    for (RecordModel *record in self.viewModel.records) {
        TimeSegment *t = [[TimeSegment alloc] init];
        t.startAt = [record startAtSecond];
        t.duration = record.duration;
        
        [segments addObject:t];
    }
    
    self.rulerView.hidden = NO;
    self.rulerView2.hidden = YES;
    
    self.rulerView.segments = segments;
    self.rulerView2.segments = segments;
    
    [self.rulerView customRulerWithLineColor:customColorMake(153, 153, 153)
                                    numColor:YYUIColorFromRGB(0x737373)
                                scrollEnable:YES];
    [self.rulerView2 customRulerWithLineColor:customColorMake(153, 153, 153)
                                    numColor:YYUIColorFromRGB(0x737373)
                                scrollEnable:YES];
    
    [self.rulerView scrollToValue:[self.viewModel.curRecord startAtSecond] animation:NO];
    [self.rulerView2 scrollToValue:[self.viewModel.curRecord startAtSecond] animation:NO];
    
    [self refreshMediaControl];
}

- (void)refreshMediaControl {
    if (!self.isTouchBegin) {
        NSTimeInterval position = self.player.currentPlaybackTime;
        NSLog(@"position --> %f", position);
        if (position > 0) {
            NSInteger value = [self.viewModel.curRecord startAtSecond] + position;
            [self.rulerView scrollToValue:value animation:NO];
            [self.rulerView2 scrollToValue:value animation:NO];
        }
    }
    
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(refreshMediaControl) object:nil];
    [self performSelector:@selector(refreshMediaControl) withObject:nil afterDelay:1];
}

#pragma mark - 标尺代理方法

// 即时打印出标尺滑动位置的数值
- (void)rulerValue:(NSInteger)value {
    NSLog(@"当前刻度值：%ld 秒", (long)value);
    self.currentValue = value;
}

- (void)rulerRunEndValue:(NSInteger)value {
    NSLog(@"停止刻度值：%ld 秒", (long)value);
    
    int count = (int) self.viewModel.records.count;
    for (int i = (count - 1); i >= 0; i--) {
        RecordModel *curRecord = self.viewModel.records[i];
        NSInteger pos = value - [curRecord startAtSecond];
        if (pos >= 0 && pos < curRecord.duration) {
            NSLog(@"pos---> %ld", pos);
            
            if ([self.viewModel.curRecord.hls isEqualToString:curRecord.hls]) {
                self.player.currentPlaybackTime = pos;
                [self.player play];
            } else {
                self.viewModel.curRecord = curRecord;
                self.viewModel.curRecord.currentPlayTime = pos;
                
                [self playAgain];
            }
            
            self.isTouchBegin = NO;
            return;
        }
    }
    
    self.isTouchBegin = NO;
    [self.rulerView scrollToValue:self.currentValue animation:NO];
    [self.rulerView2 scrollToValue:self.currentValue animation:NO];
}

- (void) touchesBegan {
    self.isTouchBegin = YES;
}

@end
