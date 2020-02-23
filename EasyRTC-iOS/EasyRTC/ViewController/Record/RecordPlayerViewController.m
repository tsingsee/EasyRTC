//
//  RecordPlayerViewController.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/9/1.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "RecordPlayerViewController.h"
#import <IJKMediaFramework/IJKMediaFramework.h>
#import "VVeboImageView.h"
#import "Masonry.h"

@interface RecordPlayerViewController ()

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *marginCenterY;
@property (weak, nonatomic) IBOutlet UIButton *controlBtn;
@property (weak, nonatomic) IBOutlet UILabel *startLabel;
@property (weak, nonatomic) IBOutlet UILabel *endLabel;
@property (weak, nonatomic) IBOutlet UISlider *slider;

@property (nonatomic, strong) VVeboImageView *loadIV;
@property (nonatomic, strong) UIButton *playBtn;

@property (atomic, retain) id<IJKMediaPlayback> player;

@property (atomic, assign) BOOL isMediaSliderBeingDragged;

@end

@implementation RecordPlayerViewController

- (instancetype) initWithStoryboard {
    return [[UIStoryboard storyboardWithName:@"Record" bundle:nil] instantiateViewControllerWithIdentifier:@"RecordPlayerViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = UIColorFromRGBA(0x000000, 1.0);
    
//    UITapGestureRecognizer *gesture = [[UITapGestureRecognizer alloc] initWithActionBlock:^(id  _Nonnull sender) {
//        [self dismissViewControllerAnimated:YES completion:nil];
//    }];
//    [self.view addGestureRecognizer:gesture];
    
    NSData *data = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"loading.gif" ofType:nil]];
    self.loadIV = [[VVeboImageView alloc] initWithImage:[VVeboImage gifWithData:data]];
    [self.view addSubview:self.loadIV];
    [self.loadIV makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view.mas_centerX);
        make.centerY.equalTo(self.view.mas_centerY);
        make.width.equalTo(@50);
        make.height.equalTo(@50);
    }];
    
    [self installMovieNotificationObservers];
    [self play];
    
    [self addItemView];
    
    [_controlBtn setImage:[UIImage imageNamed:@"record_list_bottom_play"] forState:UIControlStateNormal];
    [_controlBtn setImage:[UIImage imageNamed:@"record_list_bottom_pause"] forState:UIControlStateSelected];
    
    [_slider addTarget:self action:@selector(beginDragMediaSlider) forControlEvents:UIControlEventTouchDown];
    [_slider addTarget:self action:@selector(endDragMediaSlider) forControlEvents:UIControlEventTouchCancel];
    [_slider addTarget:self action:@selector(didSliderTouchUpInside) forControlEvents:UIControlEventTouchUpInside];
    [_slider addTarget:self action:@selector(didSliderTouchUpOutside) forControlEvents:UIControlEventTouchUpOutside];
    [_slider addTarget:self action:@selector(continueDragMediaSlider) forControlEvents:UIControlEventValueChanged];
    
    [self refreshMediaControl];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
//    [self.player shutdown];
    [self.player.view removeFromSuperview];
    self.player = nil;
    
    [self removeMovieNotificationObservers];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void) playAgain {
    [self.loadIV playGif];
    self.loadIV.hidden = NO;
    
    if (self.player) {
//        [self.player shutdown];
        [self.player.view removeFromSuperview];
        self.player = nil;
    }
    
    [self play];
}

- (void) play {
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
//    [options setFormatOptionIntValue:1 forKey:@"opensles"];
//    [options setFormatOptionIntValue:1 forKey:@"mediacodec"];
//    [options setFormatOptionIntValue:1 forKey:@"mediacodec-auto-rotate"];
//    [options setFormatOptionIntValue:1 forKey:@"mediacodec-handle-resolution-change"];
    
    // RTSP的话,iformat是rtsp,rtmp是flv,m3u8是hls
    if ([[self.model.hls substringToIndex:4] isEqualToString:@"rtmp"]) {
        [options setFormatOptionValue:@"flv" forKey:@"iformat"];
    } else if ([[self.model.hls substringToIndex:4] isEqualToString:@"m3u8"]) {
        [options setFormatOptionValue:@"hls" forKey:@"iformat"];
    } else {
        [options setFormatOptionValue:@"rtsp" forKey:@"iformat"];
    }
    
    NSURL *url = [NSURL URLWithString:self.model.hls];
    self.player = [[IJKFFMoviePlayerController alloc] initWithContentURL:url withOptions:options key:PlayerKey];
    
    if (self.player) {
        self.player.view.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        self.player.view.frame = CGRectMake(0, (EasyScreenHeight - 220) / 2, EasyScreenWidth, 220);
        
        self.player.scalingMode = IJKMPMovieScalingModeAspectFit;
        self.player.shouldAutoplay = YES;
        self.view.autoresizesSubviews = YES;
        [self.view insertSubview:self.player.view atIndex:0];
        
        [self.player prepareToPlay];
        
        // 点击画面
        UITapGestureRecognizer *gesture = [[UITapGestureRecognizer alloc] initWithActionBlock:^(id  _Nonnull sender) {
            
        }];
        [self.player.view addGestureRecognizer:gesture];
    } else {
        [self showTextHubWithContent:@"Key不合法或者已过期"];
    }
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
        CGFloat height = EasyScreenWidth * s.height / s.width;
        self.player.view.frame = CGRectMake(0, (EasyScreenHeight - height) / 2, EasyScreenWidth, height);
        self.marginCenterY.constant = height / 2 - 20;
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
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
            [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(refreshMediaControl) object:nil];
            self.controlBtn.selected = YES;
            break;
        case IJKMPMovieFinishReasonUserExited:
            NSLog(@"playbackStateDidChange: IJKMPMovieFinishReasonUserExited: %d\n", reason);
            [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(refreshMediaControl) object:nil];
            self.controlBtn.selected = YES;
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
            _playBtn.hidden = YES;
            NSLog(@"IJKMPMoviePlayBackStateDidChange %d: playing", (int)_player.playbackState);
            break;
        }
        case IJKMPMoviePlaybackStatePaused: {
            _playBtn.hidden = NO;
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

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    
}

#pragma mark - item view

- (void) addItemView {
    UIButton *closeBtn = [[UIButton alloc] init];
    [closeBtn setImage:[UIImage imageNamed:@"list_close"] forState:UIControlStateNormal];
    [closeBtn setTarget:self action:@selector(close) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:closeBtn];
    [closeBtn makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.player.view.mas_top);
        make.right.equalTo(self.player.view.mas_right);
        make.width.height.equalTo(@44);
    }];
    
    _playBtn = [[UIButton alloc] init];
    [_playBtn setImage:[UIImage imageNamed:@"list_pause"] forState:UIControlStateNormal];
    [_playBtn setTarget:self action:@selector(play_pause) forControlEvents:UIControlEventTouchUpInside];
    _playBtn.hidden = YES;
    [self.view addSubview:_playBtn];
    [_playBtn makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view.mas_centerX);
        make.centerY.equalTo(self.view.mas_centerY);
    }];
}

#pragma mark - click listener

- (void) close {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void) play_pause {
    [self.player play];
    [self refreshMediaControl];
    _controlBtn.selected = NO;
    _playBtn.hidden = YES;
}

- (IBAction)control:(id)sender {
    _controlBtn.selected = !_controlBtn.selected;
    
    if (_controlBtn.selected) {
        [self.player pause];
        [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(refreshMediaControl) object:nil];
    } else {
        [self.player play];
        [self refreshMediaControl];
        _playBtn.hidden = YES;
    }
}

- (void)beginDragMediaSlider {
    _isMediaSliderBeingDragged = YES;
}

- (void)endDragMediaSlider {
    _isMediaSliderBeingDragged = NO;
}

- (void)didSliderTouchUpOutside {
    [self endDragMediaSlider];
}

- (void)didSliderTouchUpInside {
    self.player.currentPlaybackTime = self.slider.value;
    [self endDragMediaSlider];
}

- (void)continueDragMediaSlider {
    [self refreshMediaControl];
}

- (void)refreshMediaControl {
    NSTimeInterval duration = self.player.duration;
    NSInteger intDuration = duration + 0.5;
    if (intDuration > 0) {
        _slider.maximumValue = duration;
        _endLabel.text = [NSString stringWithFormat:@"%02d:%02d", (int)(intDuration / 60), (int)(intDuration % 60)];
    }
    
    NSTimeInterval position;
    if (_isMediaSliderBeingDragged) {
        position = self.slider.value;
    } else {
        position = self.player.currentPlaybackTime;
    }
    
    NSInteger intPosition = position + 0.5;
    if (intDuration > 0) {
        self.slider.value = position;
    } else {
        self.slider.value = 0.0f;
    }
    
    _startLabel.text = [NSString stringWithFormat:@"%02d:%02d", (int)(intPosition / 60), (int)(intPosition % 60)];
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(refreshMediaControl) object:nil];
    [self performSelector:@selector(refreshMediaControl) withObject:nil afterDelay:0.5];
}

@end
