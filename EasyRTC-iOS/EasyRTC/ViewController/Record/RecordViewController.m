//
//  RecordViewController.m
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "RecordViewController.h"
#import "SettingViewController.h"
#import "RecordTimeAxisViewController.h"
#import "RoomRecordViewModel.h"
#import "RoomRecordCell.h"
#import "PromptView.h"
#import "Masonry.h"

@interface RecordViewController ()<UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic, strong) RoomRecordViewModel *vm;

@property (nonatomic, strong) RoomRecordCell *tempCell;
@property (nonatomic, strong) PromptView *promptView;

@property (nonatomic, strong) NSMutableArray<NSString *> *devices;

@end

@implementation RecordViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"RecordViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.devices = [[NSMutableArray alloc] init];
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
    
    self.tempCell = [[RoomRecordCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"RoomRecordCell"];
    
    [_textField addTarget:self action:@selector(changedTextField:) forControlEvents:UIControlEventEditingChanged];
}

- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.parentViewController.navigationItem.title = @"会议回看";
    
    UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"setting"] style:UIBarButtonItemStyleDone target:self action:@selector(setting)];
    self.parentViewController.navigationItem.leftBarButtonItem = item;
    
    self.parentViewController.navigationItem.rightBarButtonItem = nil;
}

- (void) setting {
    SettingViewController *vc = [[SettingViewController alloc] initWithStoryborad];
    [self basePushViewController:vc];
}

-(void)changedTextField:(id)textField {
    [self.devices removeAllObjects];
    
    if ([_textField.text isEqualToString:@""]) {
        [self.devices addObjectsFromArray:self.vm.model.devices];
    } else {
        for (NSString *item in self.vm.model.devices) {
            if ([item containsString:_textField.text]) {
                [self.devices addObject:item];
            }
        }
    }
    
    [self.tableView reloadData];
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.devices.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    RoomRecordCell *cell = [RoomRecordCell cellWithTableView:tableView];
    
    NSString *model = self.devices[indexPath.row];
    cell.model = model;
    
    return cell;
}

#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 65;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return CGFLOAT_MIN;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return CGFLOAT_MIN;
}

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    
    if (indexPath.row > (self.devices.count - 1)) {
        return;
    }
    
    RecordTimeAxisViewController *vc = [[RecordTimeAxisViewController alloc] initWithStoryboard];
    vc.recordId = self.devices[indexPath.row];
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

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    [self showHub];
    [self.vm.dataCommand execute:nil];
    
    [self.vm.dataSubject subscribeNext:^(id x) {
        [self hideHub];
        
        if (self.vm.model.devices.count > 0) {
            [self.devices removeAllObjects];
            [self.devices addObjectsFromArray:self.vm.model.devices];
            [self.tableView reloadData];
        } else {
            [self addPromptView];
        }
    }];
}

- (RoomRecordViewModel *) vm {
    if (!_vm) {
        _vm = [[RoomRecordViewModel alloc] init];
    }
    
    return _vm;
}

@end
