//
//  RecordCell.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/9/1.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "RecordCell.h"
#import "Masonry.h"

@implementation RecordCell

+ (instancetype)cellWithTableView:(UITableView *)tableView {
    static NSString *identifier = @"RecordCell";
    // 1.缓存中
    RecordCell *cell = [tableView dequeueReusableCellWithIdentifier:identifier];
    // 2.创建
    if (cell == nil) {
        cell = [[RecordCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    return cell;
}

/**
 *  构造方法(在初始化对象的时候会调用)
 *  一般在这个方法中添加需要显示的子控件
 */
- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.backgroundColor = [UIColor clearColor];
        
        _playBtn = [[UIButton alloc] init];
        [_playBtn setTitle:@" 播放" forState:UIControlStateNormal];
        _playBtn.titleLabel.font = [UIFont systemFontOfSize:14];
        [_playBtn setTitleColor:UIColorFromRGB(0x4c4c4c) forState:UIControlStateNormal];
        [_playBtn setTitleColor:UIColorFromRGB(0x58b9fb) forState:UIControlStateHighlighted];
        [_playBtn setImage:[UIImage imageNamed:@"list_record_pause"] forState:UIControlStateNormal];
        [_playBtn setImage:[UIImage imageNamed:@"list_record_play"] forState:UIControlStateHighlighted];
        [self.contentView addSubview:_playBtn];
        [_playBtn makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(@0);
            make.top.equalTo(@0);
            make.bottom.equalTo(@0);
            make.width.equalTo(@66);
        }];
        
        [[_playBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(id x) {
            [self.playSubject sendNext:self.model];
        }];
        
        _durationLabel = [[UILabel alloc] init];
        _durationLabel.textColor = UIColorFromRGB(0x4c4c4c);
        _durationLabel.font = [UIFont systemFontOfSize:14.0];
        _durationLabel.textAlignment = NSTextAlignmentCenter;
        [self.contentView addSubview:_durationLabel];
        [_durationLabel makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(self.playBtn.mas_left);
            make.top.equalTo(@0);
            make.bottom.equalTo(@0);
            make.width.equalTo(@88);
        }];
        
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.textColor = UIColorFromRGB(0x4c4c4c);
        _timeLabel.font = [UIFont systemFontOfSize:14.0];
        _timeLabel.textAlignment = NSTextAlignmentCenter;
        [self.contentView addSubview:_timeLabel];
        [_timeLabel makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(@0);
            make.top.equalTo(@0);
            make.bottom.equalTo(@0);
            make.right.equalTo(self.durationLabel.mas_left);
        }];
        
        UIView *line = [[UIView alloc] init];
        line.backgroundColor = UIColorFromRGB(0x999999);
        [self.contentView addSubview:line];
        [line makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(@0);
            make.right.equalTo(@0);
            make.bottom.equalTo(@0);
            make.height.equalTo(@0.5);
        }];
    }
    
    return self;
}

- (void) setModel:(RecordModel *)model {
    _model = model;
    
    _timeLabel.text = [model startAtFormat];
    _durationLabel.text = [model durationFormat];
}

- (RACSubject *) playSubject {
    if (!_playSubject) {
        _playSubject = [RACSubject subject];
    }
    
    return _playSubject;
}

@end
