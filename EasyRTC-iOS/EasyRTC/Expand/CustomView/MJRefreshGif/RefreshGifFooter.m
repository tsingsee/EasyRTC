//
//  RefreshGifFooter.m
//  Easy
//
//  Created by leo on 2018/5/14.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "RefreshGifFooter.h"

@interface RefreshGifFooter()

@property (nonatomic,strong)UILabel *statusLabel;
@property (nonatomic,strong)UIImageView *refreshView;

@end

@implementation RefreshGifFooter

// 初始化
- (void)prepare {
    [super prepare];
    
    self.mj_h = 82;
    
    //根据拖拽的情况自动切换透明度
    self.automaticallyChangeAlpha = YES;
    
    self.stateLabel.hidden = YES;
    
    self.statusLabel = [[UILabel alloc]init];
    self.statusLabel.textColor = UIColorFromRGB(EasyBaseFontColor);
    self.statusLabel.font = [UIFont systemFontOfSize:12];
    self.statusLabel.textAlignment = NSTextAlignmentCenter;
    [self addSubview:self.statusLabel];
    
    NSMutableArray *imagearray = [[NSMutableArray alloc] init];
    for (int i = 0; i < 8; i++) {
        NSString *name = [NSString stringWithFormat:@"Sprites_%d", i];
        [imagearray addObject:[UIImage imageNamed:name]];
    }
    
    self.refreshView = [[UIImageView alloc] init];
    self.refreshView.contentMode = UIViewContentModeCenter;
    self.refreshView.image = [UIImage imageNamed:@"Sprites_0"];
    self.refreshView.animationImages = imagearray;
    self.refreshView.animationDuration = 1;
    self.refreshView.animationRepeatCount = MAXFLOAT;
    
    [self addSubview:self.refreshView];
}

#pragma mark 在这里设置子控件的位置和尺寸

// 摆放子控件
- (void)placeSubviews {
    [super placeSubviews];
    self.refreshView.frame = CGRectMake((self.bounds.size.width - 15) / 2, 10, 15, 40);
    self.statusLabel.frame = CGRectMake(0, 60, self.bounds.size.width, 12);
}

#pragma mark 监听scrollView的contentOffset改变

- (void)scrollViewContentOffsetDidChange:(NSDictionary *)change {
    [super scrollViewContentOffsetDidChange:change];
}

#pragma mark 监听scrollView的contentSize改变

- (void)scrollViewContentSizeDidChange:(NSDictionary *)change {
    [super scrollViewContentSizeDidChange:change];
}

#pragma mark 监听scrollView的拖拽状态改变

- (void)scrollViewPanStateDidChange:(NSDictionary *)change {
    [super scrollViewPanStateDidChange:change];
}

#pragma mark 监听控件的刷新状态

- (void)setState:(MJRefreshState)state {
    MJRefreshCheckState;
    
    switch (state) {
        case MJRefreshStateIdle:
            [self.refreshView stopAnimating];
            self.statusLabel.text = @"上拉加载";
            break;
        case MJRefreshStatePulling:
            self.statusLabel.text = @"释放立即加载";
            break;
        case MJRefreshStateRefreshing:
            [self.refreshView startAnimating];
            self.statusLabel.text = @"正在加载...";
            break;
        default:
            break;
    }
}

@end
