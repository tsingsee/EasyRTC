//
//  RoomViewController.m
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "RoomViewController.h"
#import "SettingViewController.h"
#import "CallViewController.h"
#import "PromptView.h"
#import "RoomCell.h"
#import "Masonry.h"

@interface RoomViewController ()<UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UIButton *typeBtn;
@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic, strong) RoomCell *tempCell;
@property (nonatomic, strong) PromptView *promptView;

@property (nonatomic, strong) NSArray *roomBeans;

@end

@implementation RoomViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"RoomViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.roomBeans = @[ [self roomBean:@"3580" name:@"2019年公司年度总结会议"],
                        [self roomBean:@"3581" name:@"2020年市场营销部业务规划会议"],
                        [self roomBean:@"3582" name:@"华为5G项目推进会"],
                        [self roomBean:@"3583" name:@"安徽省高速取消边界收费项目需求会"],
                        [self roomBean:@"3584" name:@"研发部例会"],
                        [self roomBean:@"3585" name:@"关于安防互联网直播项目的培训"],
                        [self roomBean:@"3586" name:@"EasyRTC新功能发布会"],
                        [self roomBean:@"3587" name:@"在线教育培训"] ];
    
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.tableView.backgroundColor = [UIColor clearColor];
    self.tableView.showsVerticalScrollIndicator = YES;
    
    if (@available(iOS 11.0, *)) {
        self.tableView.estimatedRowHeight = 0;
        self.tableView.estimatedSectionHeaderHeight = 0;
        self.tableView.estimatedSectionFooterHeight = 0;
    }
    
    self.tempCell = [[RoomCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"RoomCell"];
    
    [_textField addTarget:self action:@selector(textFieldDidChange:) forControlEvents:UIControlEventEditingChanged];
}

- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.parentViewController.navigationItem.title = @"视频会议";
    self.parentViewController.navigationController.navigationBar.barTintColor = UIColorFromRGB(EasyThemeColor);// 导航栏背景颜色
    self.parentViewController.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    self.parentViewController.navigationController.navigationBar.titleTextAttributes = @{NSForegroundColorAttributeName:[UIColor whiteColor]};
    
    UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"setting"] style:UIBarButtonItemStyleDone target:self action:@selector(setting)];
    self.parentViewController.navigationItem.leftBarButtonItem = item;
    
//    UIBarButtonItem *item1 = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"create_room"] style:UIBarButtonItemStyleDone target:self action:@selector(createRoom)];
//    self.parentViewController.navigationItem.rightBarButtonItem = item1;
}

- (void) setting {
    SettingViewController *vc = [[SettingViewController alloc] initWithStoryborad];
    [self basePushViewController:vc];
}

- (void) createRoom {
    // TODO
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.roomBeans.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    RoomCell *cell = [RoomCell cellWithTableView:tableView];
    
    RoomBean *model = self.roomBeans[indexPath.row];
    cell.model = model;
    
    return cell;
}

#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    RoomBean *model = self.roomBeans[indexPath.row];
    if (model.height == 0) {
        model.height = [self.tempCell heightForModel:model];
    }
    
    return model.height;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return CGFLOAT_MIN;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return CGFLOAT_MIN;
}

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    
    if (indexPath.row > (self.roomBeans.count - 1)) {
        return;
    }
    RoomBean *bean = self.roomBeans[indexPath.row];
    NSString *no = bean.roomNo;
    
    NSString *name = [[LoginInfoLocalData sharedInstance] gainName];
    NSString *pwd = [[LoginInfoLocalData sharedInstance] gainPWD];
    
    Options *options = [[Options alloc] init];
    options.username = name;
    options.password = pwd;
    options.roomNumber = no;
    options.displayName = options.username;
//    options.userEmail = [NSString stringWithFormat:@"%@@easydarwin.org", options.username];
//    options.serverAddress = self.serverTF.text.length > 0 ? self.serverTF.text : @"easyrtc.easydss.com";
    
    CallViewController *vc = [[CallViewController alloc] initWithStoryborad];
    vc.options = options;
    [self basePushViewController:vc];
}

- (void) addPromptView {
    if (!self.promptView) {
        self.promptView = [[PromptView alloc] initWithFrame:CGRectMake(0, 0, EasyScreenWidth, EasyScreenHeight)];
        [self.promptView setNilDataWithImagePath:@"" tint:@"暂无数据" btnTitle:@""];
        [self.view addSubview:self.promptView];
        [self.promptView makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self.view);
        }];
    }
}

#pragma mark - click

- (void) textFieldDidChange:(UITextField *)tf {
    // TODO
}

- (IBAction)chooseType:(id)sender {
    // TODO
}

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    
}

- (RoomBean *) roomBean:(NSString *)no name:(NSString *)name {
    RoomBean *bean = [[RoomBean alloc] init];
    bean.roomNo = no;
    bean.roomName = name;
    bean.status = @"在线";
    bean.createTime = @"2020-1-1 12:12:12";
    
    return bean;
}

@end
