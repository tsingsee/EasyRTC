//
//  MainViewController.m
//  VenusApp
//
//  Created by liyy on 2019/1/12.
//  Copyright © 2019年 easydarwin. All rights reserved.
//

#import "MainViewController.h"
#import "ListViewController.h"
#import "SVProgressHUD.h"
#import "Toast/UIView+Toast.h"
#import "ListTableViewCell.h"

#define SCREENWIDTH       ([[UIScreen mainScreen] bounds].size.width)
#define SCREENHEIGHT      ([[UIScreen mainScreen] bounds].size.height)

@interface MainViewController () <RoomStatusDelegate, UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITableView *userTable;

@property (weak, nonatomic) IBOutlet UIButton *cancelBtn;
@property (weak, nonatomic) IBOutlet UIImageView *liveView;
@property (weak, nonatomic) IBOutlet UIButton *returnBtn;
@property (weak, nonatomic) IBOutlet UIButton *speakBtn;
//@property (weak, nonatomic) IBOutlet UIButton *userListBtn;

//@property (nonatomic, strong) ListViewController *listVC;
@property (nonatomic, strong) Room *room;

@property (atomic, strong) NSMutableArray *userList;

@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.cancelBtn.layer.cornerRadius = 3.0;
    self.cancelBtn.layer.shadowColor = [UIColor blackColor].CGColor;
    self.cancelBtn.layer.shadowOffset = CGSizeMake(0, 2);
    self.cancelBtn.layer.shadowOpacity = 0.8;
    self.cancelBtn.layer.shadowRadius = 1;
    
    [SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
    [SVProgressHUD setMinimumDismissTimeInterval:1.0];
    
    [self showWaitingView];
    [self hideControlPanel];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(userMute:) name:@"kMuteBtnClicked" object:nil];
    
    [self callRoom];
    
    self.userTable.delegate = self;
    self.userTable.dataSource = self;
}

- (void)viewWillLayoutSubviews {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        CGRect frame = self.liveView.frame;
        UIView *renderView = [self.room getRenderView];
        renderView.frame = CGRectMake(0, 0, frame.size.width, frame.size.height);
    });
}

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
    if (![SVProgressHUD isVisible]) {
        [SVProgressHUD showWithStatus:@"   连接中...   "];
    }

    self.cancelBtn.hidden = NO;
}

- (void)hideWaitingView {
    [SVProgressHUD dismiss];
    self.cancelBtn.hidden = YES;
}

- (void)showControlPanel {
    self.returnBtn.hidden = NO;
    self.speakBtn.hidden = NO;
//    self.userListBtn.hidden = NO;
    self.userTable.hidden = NO;
}

- (void)hideControlPanel {
    self.returnBtn.hidden = YES;
    self.speakBtn.hidden = YES;
//    self.userListBtn.hidden = YES;
    self.userTable.hidden = YES;
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

- (IBAction)cancelBtnClicked:(id)sender {
    [self.room leave];
    [self hideWaitingView];
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)speakerBtnClicked:(id)sender {
    [self.room loudspeakerClick];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if ([self.room isSpeakerEnable]) {
            [self.view makeToast:@"Speaker ON" duration:1.0 position:CSToastPositionTop];
            [self.speakBtn setImage:[UIImage imageNamed:@"speakeron"] forState:UIControlStateNormal];
        } else {
            [self.view makeToast:@"Speaker OFF" duration:1.0 position:CSToastPositionTop];
            [self.speakBtn setImage:[UIImage imageNamed:@"speakeroff"] forState:UIControlStateNormal];
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

@end
