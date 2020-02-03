//
//  RecordCell.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/9/1.
//  Copyright © 2018年 leo. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RecordModel.h"

@interface RecordCell : UITableViewCell

@property (nonatomic, strong) UILabel *timeLabel;
@property (nonatomic, strong) UILabel *durationLabel;
@property (nonatomic, strong) UIButton *playBtn;

@property (nonatomic, strong) RecordModel *model;
@property (nonatomic, strong) RACSubject *playSubject;

+ (instancetype)cellWithTableView:(UITableView *)tableView;

@end
