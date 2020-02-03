//
//  LiveViewController.m
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "LiveViewController.h"
#import "SettingViewController.h"
#import "LiveViewModel.h"
#import "LiveCell.h"
#import "PromptView.h"
#import "Masonry.h"

@interface LiveViewController ()<UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic, strong) LiveViewModel *vm;

@property (nonatomic, strong) LiveCell *tempCell;
@property (nonatomic, strong) PromptView *promptView;

@end

@implementation LiveViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"LiveViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
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

- (void) setting {
    SettingViewController *vc = [[SettingViewController alloc] initWithStoryborad];
    [self basePushViewController:vc];
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.vm.model.sessions.sessions.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    LiveCell *cell = [LiveCell cellWithTableView:tableView];
    
    Session *model = self.vm.model.sessions.sessions[indexPath.row];
    cell.model = model;
    
    return cell;
}

#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    Session *model = self.vm.model.sessions.sessions[indexPath.row];
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
    
    if (indexPath.row > (self.vm.model.sessions.sessions.count - 1)) {
        return;
    }
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
    [self.vm.dataCommand execute:nil];
    
    [self.vm.dataSubject subscribeNext:^(id x) {
        if (self.vm.model.sessions.sessions.count > 0) {
            [self.tableView reloadData];
        } else {
            [self addPromptView];
        }
    }];
}

- (LiveViewModel *) vm {
    if (!_vm) {
        _vm = [[LiveViewModel alloc] init];
    }
    
    return _vm;
}

@end
