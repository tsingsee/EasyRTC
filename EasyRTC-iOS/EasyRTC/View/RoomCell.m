//
//  RoomCell.m
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "RoomCell.h"
#import <YYKit/YYKit.h>
#import "UILabel+ChangeLineSpaceAndWordSpace.h"
#import "UILabel+lineSpace.h"
#import "Masonry.h"

@implementation RoomCell

+ (instancetype)cellWithTableView:(UITableView *)tableView {
    static NSString *identifier = @"RoomCell";
    // 1.缓存中
    RoomCell *cell = [tableView dequeueReusableCellWithIdentifier:identifier];
    // 2.创建
    if (cell == nil) {
        cell = [[RoomCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    
    cell.selectionStyle = UITableViewCellSelectionStyleDefault;
    
    return cell;
}

/**
 *  构造方法(在初始化对象的时候会调用)
 *  一般在这个方法中添加需要显示的子控件
 */
- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.backgroundColor = [UIColor whiteColor];
        
        _noLabel = [[UILabel alloc] init];
        _noLabel.textColor = UIColorFromRGB(0x333333);
        _noLabel.font = [UIFont systemFontOfSize:16];
        [self addSubview:_noLabel];
        [_noLabel makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(@15);
            make.top.equalTo(@15);
        }];
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.numberOfLines = 0;
        _nameLabel.textColor = UIColorFromRGB(0x8d9197);
        _nameLabel.font = [UIFont systemFontOfSize:15];
        [self addSubview:_nameLabel];
        [_nameLabel makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(@15);
            make.right.equalTo(@(-15));
            make.top.equalTo(self.noLabel.mas_bottom).offset(15);
        }];
        
        _statusLabel = [[UILabel alloc] init];
        _statusLabel.textColor = UIColorFromRGB(0x55c40b);
        _statusLabel.font = [UIFont systemFontOfSize:14];
        [self addSubview:_statusLabel];
        [_statusLabel makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.nameLabel);
            make.top.equalTo(self.nameLabel.mas_bottom).offset(15);
        }];
        
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.textColor = UIColorFromRGB(0xb2b2b2);
        _timeLabel.font = [UIFont systemFontOfSize:14];
        [self addSubview:_timeLabel];
        [_timeLabel makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(self.nameLabel);
            make.top.equalTo(self.nameLabel.mas_bottom).offset(15);
        }];
        
        UIView *line = [[UIView alloc] init];
        line.backgroundColor = UIColorFromRGB(EasyBLineViewColor);
        [self addSubview:line];
        [line makeConstraints:^(MASConstraintMaker *make) {
            make.left.right.bottom.equalTo(@0);
            make.height.equalTo(@10);
        }];
    }
    
    return self;
}

- (void) setModel:(RoomBean *)model {
    self.noLabel.text = [NSString stringWithFormat:@"会议室号：%@", model.roomNo];
    self.nameLabel.text = model.roomName ? model.roomName : @"";
    self.timeLabel.text = model.createTime ? model.createTime : @"";
    self.statusLabel.text = model.status ? model.status : @"";
    
    // 调整行间距
    [UILabel changeLineSpaceForLabel:self.nameLabel WithSpace:4];
}

- (CGFloat)heightForModel:(RoomBean *)model {
    CGFloat height = 125;

//    height += 12;
//    self.nameLabel.text = model.newsTitle ? model.newsTitle : @"";
//    height += [_nameLabel labelHeightWithLineSpace:4 WithWidth:(HRGScreenWidth-132) WithNumline:0].height;
//
//    height += 12;
//    height += 17;
//    height += 12;
//
//    if (height < 88) {
//        height = 88;
//    }
    
    return height;
}

@end
