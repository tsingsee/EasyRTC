//
//  LiveCell.m
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "LiveCell.h"
#import <YYKit/YYKit.h>
#import "UILabel+ChangeLineSpaceAndWordSpace.h"
#import "UILabel+lineSpace.h"
#import "Masonry.h"

@implementation LiveCell

+ (instancetype)cellWithTableView:(UITableView *)tableView {
    static NSString *identifier = @"LiveCell";
    // 1.缓存中
    LiveCell *cell = [tableView dequeueReusableCellWithIdentifier:identifier];
    // 2.创建
    if (cell == nil) {
        cell = [[LiveCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
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
            make.left.equalTo(@12);
            make.right.equalTo(@(-12));
            make.top.equalTo(@15);
        }];
        
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.textColor = UIColorFromRGB(0xb2b2b2);
        _timeLabel.font = [UIFont systemFontOfSize:14];
        [self addSubview:_timeLabel];
        [_timeLabel makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.noLabel);
            make.top.equalTo(self.noLabel.mas_bottom).offset(15);
        }];
        
        _bitLabel = [[UILabel alloc] init];
        _bitLabel.textColor = UIColorFromRGB(0xb2b2b2);
        _bitLabel.font = [UIFont systemFontOfSize:14];
        [self addSubview:_bitLabel];
        [_bitLabel makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(self.noLabel);
            make.top.equalTo(self.noLabel.mas_bottom).offset(15);
        }];
        
        UIView *line = [[UIView alloc] init];
        line.backgroundColor = UIColorFromRGB(EasyBLineViewColor);
        [self addSubview:line];
        [line makeConstraints:^(MASConstraintMaker *make) {
            make.left.right.bottom.equalTo(@0);
            make.height.equalTo(@1);
        }];
    }
    
    return self;
}

- (void) setModel:(Session *)model {
    self.noLabel.text = [NSString stringWithFormat:@"会议室号：%@", model.sessionID];
    self.timeLabel.text = [NSString stringWithFormat:@"会议时长：%@", model.Time];
    self.bitLabel.text = [NSString stringWithFormat:@"推送码率：%dKB", model.InBitrate / 1000];
}

- (CGFloat)heightForModel:(Session *)model {
    CGFloat height = 85;

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
