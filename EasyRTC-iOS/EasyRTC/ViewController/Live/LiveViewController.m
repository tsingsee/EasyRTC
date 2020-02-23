//
//  LiveViewController.m
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "LiveViewController.h"
#import "SettingViewController.h"
#import "LiveDetailViewController.h"
#import "PlayerViewController.h"
#import "LiveViewModel.h"
#import "LiveCell.h"
#import "PromptView.h"
#import "Masonry.h"
#import <EasyRTC/Options.h>

@interface LiveViewController ()<UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic, strong) LiveViewModel *vm;

@property (nonatomic, strong) LiveCell *tempCell;
@property (nonatomic, strong) PromptView *promptView;

@property (nonatomic, strong) NSTimer *timer;
@property (nonatomic, assign) int rateSecond;

@end

@implementation LiveViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"LiveViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.rateSecond = 60 * 60 * 24;
    [self startTimer];
    
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
    
    self.tempCell = [[LiveCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"LiveCell"];
}

- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.parentViewController.navigationItem.title = @"会议直播";
    
    UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"setting"] style:UIBarButtonItemStyleDone target:self action:@selector(setting)];
    self.parentViewController.navigationItem.leftBarButtonItem = item;
    
    self.parentViewController.navigationItem.rightBarButtonItem = nil;
}

- (void) viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
    if (self.timer) {
        [self.timer invalidate];
        self.timer = nil;
    }
}

- (void) setting {
    SettingViewController *vc = [[SettingViewController alloc] initWithStoryborad];
    [self basePushViewController:vc];
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.vm.sessions.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    LiveCell *cell = [LiveCell cellWithTableView:tableView];
    
    Session *model = self.vm.sessions[indexPath.row];
    cell.model = model;
    
    return cell;
}

#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    Session *model = self.vm.sessions[indexPath.row];
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
    
    if (indexPath.row > (self.vm.sessions.count - 1)) {
        return;
    }
}

#pragma mark - 侧滑

- (NSArray *)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewRowAction *action1 = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleNormal title:@"播放" handler:^(UITableViewRowAction *action, NSIndexPath *indexPath) {
        Session *model = self.vm.sessions[indexPath.row];
        [self selectLive:model];
    }];
    action1.backgroundColor = UIColorFromRGB(0x00a4f2);
    
    UITableViewRowAction *action2 = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleNormal title:@"详情" handler:^(UITableViewRowAction *action, NSIndexPath *indexPath) {
        LiveDetailViewController *vc = [[LiveDetailViewController alloc] initWithStoryborad];
        vc.session = self.vm.sessions[indexPath.row];
        [self basePushViewController:vc];
    }];
    action2.backgroundColor = UIColorFromRGB(0xf4780b);
    
    return @[ action2, action1 ];
}

- (void) selectLive:(Session *)model {
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"播放类型" message:nil preferredStyle: UIAlertControllerStyleActionSheet];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    [alertController addAction: [UIAlertAction actionWithTitle: @"HLS" style: UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        Options *options = [[Options alloc] init];
        NSString *addr = [NSString stringWithFormat:@"https://%@/record", options.serverAddress];
        NSString *url = [NSString stringWithFormat:@"%@%@", addr, [model.HLS stringByReplacingOccurrencesOfString:@"/hls" withString:@""]];
        [self player:url];
    }]];
    [alertController addAction: [UIAlertAction actionWithTitle: @"FLV" style: UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        Options *options = [[Options alloc] init];
        NSString *addr = [NSString stringWithFormat:@"https://%@/record", options.serverAddress];
        NSString *url = [NSString stringWithFormat:@"%@%@", addr, [model.flv stringByReplacingOccurrencesOfString:@"/hls" withString:@""]];
        [self player:url];
    }]];
    
    [alertController addAction:cancelAction];
    [self presentViewController:alertController animated:YES completion:nil];
}

- (void) player:(NSString *)url {
    PlayerViewController *vc = [[PlayerViewController alloc] initWithStoryboard];
    vc.urlStr = url;
    vc.modalPresentationStyle = UIModalPresentationOverCurrentContext;
    [self presentViewController:vc animated:YES completion:nil];
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

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    [self showHub];
    [self.vm.dataCommand execute:nil];
    
    [self.vm.dataSubject subscribeNext:^(id x) {
        [self hideHub];
        
        if (self.vm.sessions.count > 0) {
            [self.tableView reloadData];
            [self.promptView setHidden:YES];
        } else {
            [self addPromptView];
            [self.promptView setHidden:NO];
        }
    }];
}

- (LiveViewModel *) vm {
    if (!_vm) {
        _vm = [[LiveViewModel alloc] init];
    }
    
    return _vm;
}

- (void) startTimer {
    if (@available(iOS 10.0, *)) {
        self.timer = [NSTimer scheduledTimerWithTimeInterval:1.0 repeats:YES block:^(NSTimer * _Nonnull timer) {
            if (self.rateSecond % 10 == 0) {
                [self.vm.dataCommand execute:nil];
            }
            
            self.rateSecond--;
            if (self.rateSecond == -1) {
                [self.timer invalidate];
                self.timer = nil;
            }
        }];
    } else {
        // Fallback on earlier versions
    }
}

@end
