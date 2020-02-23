//
//  RecordListViewController.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/23.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "RecordListViewController.h"
#import "RecordPlayerViewController.h"
#import "CalendarViewController.h"
#import "DownloadViewController.h"
#import "RecordViewModel.h"
#import "RecordCell.h"
#import "DateUtil.h"
#import "Masonry.h"

@interface RecordListViewController ()<UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UIButton *dateBtn;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic, strong) RecordViewModel *viewModel;

@end

@implementation RecordListViewController

- (instancetype) initWithStoryboard {
    return [[UIStoryboard storyboardWithName:@"Record" bundle:nil] instantiateViewControllerWithIdentifier:@"RecordListViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title = @"视频广场";
    
    self.view.backgroundColor = UIColorFromRGB(0xeceff0);
    EasyViewBorderRadius(_dateBtn, 4, 0.6, UIColorFromRGB(0xb2b2b2));
    
    [_dateBtn setImage:[UIImage imageNamed:@"time_calendar"] forState:UIControlStateNormal];
    [_dateBtn setImage:[UIImage imageNamed:@"time_calendar_on"] forState:UIControlStateHighlighted];
    [_dateBtn setTitleColor:UIColorFromRGB(0x666666) forState:UIControlStateNormal];
    [_dateBtn setTitleColor:UIColorFromRGB(0x58b9fb) forState:UIControlStateHighlighted];
    
    self.viewModel.selectDate = self.selectDate;
    [self setDateBtnTitle];
    
    self.tableView.backgroundColor = [UIColor clearColor];
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    self.tableView.rowHeight = 50;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void) setDateBtnTitle {
    NSString *dateStr = [NSString stringWithFormat:@"  %@", [DateUtil dateYYYY_MM_DD:self.viewModel.selectDate]];
    [self.dateBtn setTitle:dateStr forState:UIControlStateNormal];
}

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    [self.viewModel.tokenSubject subscribeNext:^(RACCommand *command) {
        [self loginFirstWithCommend:command];
    }];
    
    [self showHub];
    [self.viewModel.querydailyCommand execute:self.recordId];
    
    [self.viewModel.querydailySubject subscribeNext:^(id x) {
        [self hideHub];
        
        if (x) {
            [self.tableView reloadData];
        } else {
            [self showTextHubWithContent:@"暂无录像"];
            [self.navigationController popViewControllerAnimated:YES];
        }
    }];
    
    [self.viewModel.removeSubject subscribeNext:^(NSString *res) {
        [self hideHub];
        
        if (res) {
//            [self showTextHubWithContent:res];
        }
        
        [self.subject sendNext:nil];
        
        // 删除录像后 刷新数据
        [self.viewModel.querydailyCommand execute:self.recordId];
    }];
}

#pragma mark - click

// 展开日历，选择日期
- (IBAction)selectDate:(id)sender {
    CalendarViewController *controller = [[CalendarViewController alloc] initWithStoryborad];
    controller.recordId = self.recordId;
    controller.chooseMonth = self.viewModel.selectDate;
    [controller.subject subscribeNext:^(NSDate *date) {
        self.viewModel.selectDate = date;
        [self setDateBtnTitle];
        
        [self showHub];
        [self.viewModel.querydailyCommand execute:self.recordId];
    }];
    [self basePushViewController:controller];
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.viewModel.records.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    RecordCell *cell = [RecordCell cellWithTableView:tableView];
    cell.model = self.viewModel.records[indexPath.row];
    
    // 播放
    [cell.playSubject subscribeNext:^(RecordModel *model) {
        RecordPlayerViewController *controller = [[RecordPlayerViewController alloc] initWithStoryboard];
        controller.model = model;
        controller.modalPresentationStyle = UIModalPresentationOverCurrentContext;
        [self presentViewController:controller animated:YES completion:nil];
    }];
    
    return cell;
}

#pragma mark - UITableViewDelegate

- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 50;
}

- (UIView *) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UIView *view = [[UIView alloc] init];
    view.backgroundColor = UIColorFromRGB(0xECEFF6);
    
    UILabel *playLabel = [[UILabel alloc] init];
    playLabel.text = @"操作";
    playLabel.textColor = UIColorFromRGB(0x4c4c4c);
    playLabel.font = [UIFont systemFontOfSize:14.0];
    playLabel.textAlignment = NSTextAlignmentCenter;
    [view addSubview:playLabel];
    [playLabel makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(@0);
        make.top.equalTo(@0);
        make.bottom.equalTo(@0);
        make.width.equalTo(@66);
    }];
    
    UILabel *durationLabel = [[UILabel alloc] init];
    durationLabel.text = @"录像时长";
    durationLabel.textColor = UIColorFromRGB(0x4c4c4c);
    durationLabel.font = [UIFont systemFontOfSize:14.0];
    durationLabel.textAlignment = NSTextAlignmentCenter;
    [view addSubview:durationLabel];
    [durationLabel makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(playLabel.mas_left);
        make.top.equalTo(@0);
        make.bottom.equalTo(@0);
        make.width.equalTo(@88);
    }];
    
    UILabel *timeLabel = [[UILabel alloc] init];
    timeLabel.text = @"开始时间";
    timeLabel.textColor = UIColorFromRGB(0x4c4c4c);
    timeLabel.font = [UIFont systemFontOfSize:14.0];
    timeLabel.textAlignment = NSTextAlignmentCenter;
    [view addSubview:timeLabel];
    [timeLabel makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(@0);
        make.top.equalTo(@0);
        make.bottom.equalTo(@0);
        make.right.equalTo(durationLabel.mas_left);
    }];
    
    UIView *line = [[UIView alloc] init];
    line.backgroundColor = UIColorFromRGB(0x999999);
    [view addSubview:line];
    [line makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(@0);
        make.right.equalTo(@0);
        make.bottom.equalTo(@0);
        make.height.equalTo(@0.5);
    }];
    
    return view;
}

#pragma mark - 侧滑

- (NSArray *)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewRowAction *action1 = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleNormal title:@"删除" handler:^(UITableViewRowAction *action, NSIndexPath *indexPath) {
        [self deleteRecord:indexPath];
    }];
    action1.backgroundColor = UIColorFromRGB(0xfd6845);
    
    UITableViewRowAction *action2 = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleNormal title:@"分享" handler:^(UITableViewRowAction *action, NSIndexPath *indexPath) {
        [self shareRecord:indexPath];
    }];
    action2.backgroundColor = UIColorFromRGB(0xfd9643);
    
    UITableViewRowAction *action3 = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleNormal title:@"下载" handler:^(UITableViewRowAction *action, NSIndexPath *indexPath) {
        [self downloadRecord:indexPath];
    }];
    action3.backgroundColor = UIColorFromRGB(0x5bb2ee);
    
//    return @[ action1, action2, action3 ];
    return @[ action2 ];
}

- (void) deleteRecord:(NSIndexPath *)indexPath {
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:@"提示"
                                                                        message:@"确认删除该时段的录像吗？"
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self showHubWithLoadText:@"删除中"];
        self.viewModel.curRecord = self.viewModel.records[indexPath.row];
        [self.viewModel.removeCommand execute:self.recordId];
    }];
    [controller addAction:cancelAction];
    [controller addAction:okAction];
    
    [self presentViewController:controller animated:YES completion:nil];
}

- (void) downloadRecord:(NSIndexPath *)indexPath {
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:@"提示"
                                                                        message:@"确认下载该时段的录像吗？"
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        DownloadViewController *controller = [[DownloadViewController alloc] initWithStoryborad];
        controller.recordId = self.recordId;
        controller.curRecord = self.viewModel.records[indexPath.row];
        controller.modalPresentationStyle = UIModalPresentationOverCurrentContext;
        [self presentViewController:controller animated:YES completion:nil];
    }];
    [controller addAction:cancelAction];
    [controller addAction:okAction];
    
    [self presentViewController:controller animated:YES completion:nil];
}

- (void) shareRecord:(NSIndexPath *)indexPath {
    self.viewModel.curRecord = self.viewModel.records[indexPath.row];
    
    // 添加到复制板
    UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
    pasteboard.string = self.viewModel.curRecord.hls;
    
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:@"提示"
                                                                        message:@"您已成功复制该时段的录像地址"
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        
    }];
    [controller addAction:okAction];
    [self presentViewController:controller animated:YES completion:nil];
}

#pragma mark - getter

- (RecordViewModel *) viewModel {
    if (!_viewModel) {
        _viewModel = [[RecordViewModel alloc] init];
    }
    
    return _viewModel;
}

- (RACSubject *)subject {
    if (!_subject) {
        _subject = [[RACSubject alloc] init];
    }
    
    return _subject;
}

@end
