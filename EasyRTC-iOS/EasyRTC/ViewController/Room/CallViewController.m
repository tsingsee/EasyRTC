//
//  CallViewController.m
//  VenusApp
//
//  Created by leo on 2019/1/12.
//  Copyright © 2019年 easydarwin. All rights reserved.
//

#import "CallViewController.h"
#import "ListViewController.h"
#import "ListTableViewCell.h"

@interface CallViewController () <RoomStatusDelegate, UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITableView *userTable;
@property (weak, nonatomic) IBOutlet UIImageView *liveView;
@property (weak, nonatomic) IBOutlet UIButton *returnBtn;
@property (weak, nonatomic) IBOutlet UIButton *speakBtn;
@property (weak, nonatomic) IBOutlet UIButton *flipBtn;
//@property (weak, nonatomic) IBOutlet UIButton *userListBtn;
//@property (nonatomic, strong) ListViewController *listVC;

@property (nonatomic, strong) Room *room;

@property (atomic, strong) NSMutableArray *userList;

@end

@implementation CallViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"CallViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.navigationController setNavigationBarHidden:YES];
    self.view.backgroundColor = [UIColor blackColor];
    
    [self showWaitingView];
    [self hideControlPanel];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(userMute:) name:@"kMuteBtnClicked" object:nil];
    
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onDeviceOrientationDidChange)
//                                                 name:UIDeviceOrientationDidChangeNotification
//                                               object:nil];
    
    self.userTable.delegate = self;
    self.userTable.dataSource = self;
}

- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self callRoom];
}

//- (void)viewWillDisappear:(BOOL)animated {
//    [super viewWillDisappear:animated];
//    
//    [self.room leave];
//    [self hideWaitingView];
//}
//
//- (void)viewDidDisappear:(BOOL)animated {
//    [super viewDidDisappear:animated];
//    [self.navigationController setNavigationBarHidden:NO];
//}

- (void)viewWillLayoutSubviews {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        CGRect frame = self.liveView.frame;
        UIView *renderView = [self.room getRenderView];
        renderView.frame = CGRectMake(0, 0, frame.size.width, frame.size.height);
    });
}

//- (BOOL)onDeviceOrientationDidChange {
//    //获取当前设备Device
//    UIDevice *device = [UIDevice currentDevice];
//    //识别当前设备的旋转方向
//    switch (device.orientation) {
//        case UIDeviceOrientationFaceUp: {
//            CGRect frame = self.liveView.frame;
//            UIView *renderView = [self.room getRenderView];
//            renderView.frame = CGRectMake(0, 0, frame.size.width, frame.size.height);
//        }
//            break;
//        case UIDeviceOrientationLandscapeLeft: {
//            UIView *renderView = [self.room getRenderView];
//            renderView.frame = CGRectMake(0, 0, EasyScreenHeight, EasyScreenWidth);
//        }
//            break;
//        case UIDeviceOrientationLandscapeRight: {
//            UIView *renderView = [self.room getRenderView];
//            renderView.frame = CGRectMake(0, 0, EasyScreenHeight, EasyScreenWidth);
//        }
//            break;
//        case UIDeviceOrientationPortrait: {
//            CGRect frame = self.liveView.frame;
//            UIView *renderView = [self.room getRenderView];
//            renderView.frame = CGRectMake(0, 0, frame.size.width, frame.size.height);
//        }
//            break;
//        default:
//            NSLog(@"無法识别");
//            break;
//    }
//    return YES;
//}

- (void)callRoom {
    if (!self.room) {
        self.room = [[Room alloc] init];
        self.room.options = self.options;
        self.room.roomDelegate = self;
        
        [self.room join];
    } else {
        [self.room leave];
    }
}

- (void)showWaitingView {
    [self showHubWithLoadText:@"连接中"];
}

- (void)hideWaitingView {
    [self hideHub];
}

- (void)showControlPanel {
    self.returnBtn.hidden = NO;
    self.speakBtn.hidden = NO;
    self.flipBtn.hidden = NO;
//    self.userListBtn.hidden = NO;
//    self.userTable.hidden = NO;
}

- (void)hideControlPanel {
    self.returnBtn.hidden = YES;
    self.speakBtn.hidden = YES;
    self.flipBtn.hidden = YES;
//    self.userListBtn.hidden = YES;
//    self.userTable.hidden = YES;
}

- (void)showLiveView {
    CGRect frame = self.liveView.frame;
    UIView *renderView = [self.room getRenderView];
    renderView.frame = CGRectMake(0, 0, frame.size.width, frame.size.height);
    [self.liveView addSubview:renderView];
    self.liveView.hidden = NO;
}

- (void)hideLiveView {
    self.liveView.hidden = YES;
}

#pragma mark - room delegate

- (void)onRoomStatusChange:(RoomStatus)roomStatus {
    NSLog(@"new status :%@", @(roomStatus));
    
    switch (roomStatus) {
        case ROOM_STATUS_SIGNOUT: {
//            [self hideWaitingView];
            [self hideLiveView];
//            [self.room join];
        }
            break;
        case ROOM_STATUS_SIGNING:
        case ROOM_STATUS_SIGNIN:
        case ROOM_STATUS_DISCONNECTING: {
            [self hideLiveView];
            [self showWaitingView];
        }
            break;
        case ROOM_STATUS_CONNECTING: {
            [self hideWaitingView];
            [self showControlPanel];
            [self showLiveView];
        }
            break;
        case ROOM_STATUS_CONNECTED:
            break;
    }
}

#pragma mark - room status delegate

- (void)onRoomUserJoin:(UserInfo *)info; {
    NSLog(@"join: %@", info);
//    if (self.listVC) {
//        self.listVC.userList = [[self.room getUserInfoList] copy];
//        [self.listVC updateTable];
//    }
    
    self.userList = [[self.room getUserInfoList] copy];
    [self.userTable reloadData];
}

- (void)onRoomUserLeave:(UserInfo *)info; {
    NSLog(@"leave: %@", info);
    
//    if (self.listVC) {
//        self.listVC.userList = [[self.room getUserInfoList] copy];
//        [self.listVC updateTable];
//    }
    
    self.userList = [[self.room getUserInfoList] copy];
    [self.userTable reloadData];
}

- (void)onRoomUserModify:(UserInfo *)info; {
    NSLog(@"modify: %@", info);
    
//    if (self.listVC) {
//        self.listVC.userList = [[self.room getUserInfoList] copy];
//        [self.listVC updateTable];
//    }
    
    self.userList = [[self.room getUserInfoList] copy];
    [self.userTable reloadData];
}

#pragma mark - action

// 切换设摄像头
- (IBAction)flip:(id)sender {
    [self.room swapFrontAndBackCameras];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:kSwitchCameraNotification object:nil];
}

- (IBAction)cancelBtnClicked:(id)sender {
    [self.room leave];
    [self hideWaitingView];
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)speakerBtnClicked:(id)sender {
    [self.room loudspeakerClick];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if ([self.room isSpeakerEnable]) {
            [self.speakBtn setImage:[UIImage imageNamed:@"open_mute"] forState:UIControlStateNormal];
        } else {
            [self.speakBtn setImage:[UIImage imageNamed:@"close_mute"] forState:UIControlStateNormal];
        }
    });
}

//- (IBAction)userListBtnClicked:(id)sender {
//    UIStoryboard *mainStoryboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
//    self.listVC  = [mainStoryboard instantiateViewControllerWithIdentifier:@"ListViewController"];
//    self.listVC.userList = [[self.room getUserInfoList] copy];
//    [self.navigationController pushViewController:self.listVC animated:YES];
//}

- (void)userMute:(NSNotification *)notification {
    UserInfo *info = notification.object;
    [self.room muteClick:info];
}

#pragma mark - UITableViewDataSource

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 55.0;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.userList count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UserInfo *user = [self.userList objectAtIndex:indexPath.row];
    
    ListTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ListTableViewCell" forIndexPath:indexPath];
    [cell configWithUserInfo:user];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    return cell;
}

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    
}

@end
