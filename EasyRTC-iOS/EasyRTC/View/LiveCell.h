//
//  LiveCell.h
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "LiveSessionModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface LiveCell : UITableViewCell

@property (nonatomic, strong) UILabel *noLabel;
@property (nonatomic, strong) UILabel *timeLabel;
@property (nonatomic, strong) UILabel *bitLabel;

@property (nonatomic, strong) Session *model;

+ (instancetype)cellWithTableView:(UITableView *)tableView;

- (CGFloat)heightForModel:(Session *)model;

@end

NS_ASSUME_NONNULL_END
