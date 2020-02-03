//
//  LiveCell.m
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "RoomRecordCell.h"
#import <YYKit/YYKit.h>
#import "UILabel+ChangeLineSpaceAndWordSpace.h"
#import "UILabel+lineSpace.h"
#import "Masonry.h"

@implementation RoomRecordCell

+ (instancetype)cellWithTableView:(UITableView *)tableView {
    static NSString *identifier = @"RoomRecordCell";
    // 1.缓存中
    RoomRecordCell *cell = [tableView dequeueReusableCellWithIdentifier:identifier];
    // 2.创建
    if (cell == nil) {
        cell = [[RoomRecordCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
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
        _noLabel.numberOfLines = 0;
        [self addSubview:_noLabel];
        [_noLabel makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(@12);
            make.right.equalTo(@(-12));
            make.centerY.equalTo(self);
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

- (void) setModel:(NSString *)model {
    self.noLabel.text = [NSString stringWithFormat:@"会议室号：%@", model];
}

@end
