//
//  RoomRecordCell.h
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface RoomRecordCell : UITableViewCell

@property (nonatomic, strong) UILabel *noLabel;

@property (nonatomic, copy) NSString *model;

+ (instancetype)cellWithTableView:(UITableView *)tableView;

@end

NS_ASSUME_NONNULL_END
