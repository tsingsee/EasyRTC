//
//  RoomCell.h
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RoomBean.h"

NS_ASSUME_NONNULL_BEGIN

@interface RoomCell : UITableViewCell

@property (nonatomic, strong) UILabel *noLabel;
@property (nonatomic, strong) UILabel *nameLabel;
@property (nonatomic, strong) UILabel *statusLabel;
@property (nonatomic, strong) UILabel *timeLabel;

@property (nonatomic, strong) RoomBean *model;

+ (instancetype)cellWithTableView:(UITableView *)tableView;

- (CGFloat)heightForModel:(RoomBean *)model;

@end

NS_ASSUME_NONNULL_END
