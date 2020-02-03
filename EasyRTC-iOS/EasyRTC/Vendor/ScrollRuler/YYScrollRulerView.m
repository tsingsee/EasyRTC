//
//  YYScrollRulerView.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/23.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "YYScrollRulerView.h"

@interface YYScrollRulerView()<UIScrollViewDelegate>

@property (nonatomic, strong) UIScrollView *rulerBackgroundView;
@property (nonatomic, strong) YYRullerView *rulerView;
@property (nonatomic, strong) UIImageView *pointerView; // 指示线
@property (nonatomic, strong) UILabel *curLabel;        // 当前值

@property (nonatomic, assign) CGFloat unitPX;   // 标尺单位长度
@property (nonatomic, assign) float unitCount;  // 当前标尺位置的数量

@property (nonatomic, assign) BOOL pointerFrameSeted;

@end

@implementation YYScrollRulerView

#pragma mark - init

- (instancetype)init {
    if (self = [super init]) {
        NSAssert(NO, @"RULER:请以 initWithFrame 初始化！");
    }
    
    return self;
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        NSAssert(NO, @"RULER:请以 initWithFrame 初始化！");
    }
    
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        self.backgroundColor = [UIColor whiteColor];
        
        [self initialization];
        [self addView];
    }
    
    return self;
}

#pragma mark - private init

- (void)initialization {
    // 初始化内置参数
    self.unitPX = cy_fit(20);
    
    // 初始化外部默认参数
    self.unitValue = 1;
    self.lockMin = 0;
    self.lockMax = 360;
    self.lockDefault = self.lockMin;
    self.rulerWidth = cy_ScreenW;
    self.rulerHeight = cy_fit(150);
    self.rulerDirection = RulerDirectionHorizontal;
    self.rulerFace = RulerFace_up_left;
    self.rulerBackgroundColor = [UIColor whiteColor];
    self.isShowRulerValue = YES;
    self.h_height = cy_fit(24);
    self.m_height = cy_fit(12);
    self.pointerBackgroundColor = YYUIColorFromRGB(0x58b9fb);
    CGFloat pointerWidth = 3;
//    self.pointerFrame = CGRectMake(cy_selfWidth / 2.0 - cy_fit(pointerWidth) / 2.0,
//                                   cy_selfHeight / 2.0 - (cy_selfHeight >= cy_fit(120) ? cy_fit(60) : cy_selfHeight / 2.0),
//                                   cy_fit(pointerWidth),
//                                   cy_selfHeight >= cy_fit(120) ? cy_fit(60) : cy_selfHeight / 2.0);
    self.pointerFrame = CGRectMake(cy_selfWidth / 2.0 - cy_fit(pointerWidth) / 2.0,
                                   0,
                                   cy_fit(pointerWidth),
                                   cy_selfHeight / 2.0);
    self.pointerFrameSeted = NO;
    
    self.curLabelColor = YYUIColorFromRGB(0x58b9fb);
    self.curLabelFrame = CGRectMake(cy_selfWidth / 2.0 - 80 / 2.0, cy_selfHeight - 16, 80, 16);
}

- (void)addView {
    self.rulerBackgroundView = [[UIScrollView alloc] initWithFrame:self.bounds];
    [self addSubview:self.rulerBackgroundView];
    self.rulerBackgroundView.delegate = self;
    self.rulerBackgroundView.showsHorizontalScrollIndicator = NO;
    self.rulerBackgroundView.showsVerticalScrollIndicator = NO;
    self.rulerBackgroundView.alwaysBounceHorizontal = NO;
    self.rulerBackgroundView.alwaysBounceVertical = NO;
    self.rulerBackgroundView.bounces = NO;
    
    self.pointerView = [[UIImageView alloc] initWithFrame:self.pointerFrame];
    self.pointerView.backgroundColor = self.pointerBackgroundColor;
    [self addSubview:self.pointerView];
    
    self.curLabel = [[UILabel alloc] initWithFrame:self.curLabelFrame];
    self.curLabel.textColor = self.curLabelColor;
    self.curLabel.textAlignment = NSTextAlignmentCenter;
    self.curLabel.font = [UIFont systemFontOfSize:12.0];
    [self addSubview:self.curLabel];
}

#pragma mark - public method

- (void)classicRuler {
    [self rulerInit];
    [self.rulerView drawRuler:nil];
}

- (void)customRulerWithLineColor:(CustomeColor)lineColor numColor:(UIColor *)numColor scrollEnable:(BOOL)enable {
    [self rulerInit];
    [self.rulerView drawRuler:^{
        self.rulerView.lineColor = lineColor;
        if (numColor) {
            self.rulerView.txtColor = numColor;
        }
        if (enable) {
            self.rulerBackgroundView.scrollEnabled = enable;
        }
    }];
}

- (void)reDrawerRuler {
    [self rulerInit];
    [self.rulerView setNeedsDisplay];
}

- (void)scrollToValue:(NSInteger)value animation:(BOOL)flag {
    if (value < self.lockMin || value > self.lockMax) {
        NSAssert(NO, @"scrollToValue:(NSInteger)value animation:(BOOL)flag, value 必须在标尺范围内");
        return;
    }
    
    CGFloat x = ((float)value) / ((float)_unitValue) * ((float)_unitPX);
    if (self.rulerDirection == RulerDirectionHorizontal) {
        [self.rulerBackgroundView setContentOffset:CGPointMake(x, 0) animated:flag];
    } else if (self.rulerDirection == RulerDirectionVertical) {
        [self.rulerBackgroundView setContentOffset:CGPointMake(0, value / _unitValue * _unitPX) animated:flag];
    } else {
        NSAssert(NO, @"error");
    }
}

#pragma mark - private method

- (void)rulerInit {
    if (![self paramIsAvialiable]) {
        return;
    }
    
    if (self.rulerDirection == RulerDirectionHorizontal) {
        self.rulerBackgroundView.contentSize = CGSizeMake(_unitPX * _lockMax / _unitValue + cy_selfWidth, 0);
        self.rulerBackgroundView.contentOffset = CGPointMake(_unitPX * _lockDefault / _unitValue, 0);
        
        if (!self.rulerView) {
            self.rulerView = [[YYRullerView alloc] initWithFrame:CGRectMake(0, 0, _rulerBackgroundView.contentSize.width, cy_selfHeight)];
            [self.rulerBackgroundView addSubview:self.rulerView];
        } else {
            self.rulerView.frame = CGRectMake(0, 0, _rulerBackgroundView.contentSize.width, cy_selfHeight);
        }
        
        if (!self.pointerFrameSeted && self.rulerFace == RulerFace_down_right) {
            self.pointerView.transform = CGAffineTransformMakeTranslation(0, _pointerFrame.size.height);
        }
    } else if (self.rulerDirection == RulerDirectionVertical) {
        self.rulerBackgroundView.contentSize = CGSizeMake(0, _unitPX * _lockMax / _unitValue + cy_selfHeight);
        self.rulerBackgroundView.contentOffset = CGPointMake(0, _unitPX * _lockDefault / _unitValue);
        
        if (!self.rulerView) {
            self.rulerView = [[YYRullerView alloc] initWithFrame:CGRectMake(0, 0, cy_selfWidth, self.rulerBackgroundView.contentSize.height)];
            [self.rulerBackgroundView addSubview:self.rulerView];
        } else {
            self.rulerView.frame = CGRectMake(0, 0, cy_selfWidth, _rulerBackgroundView.contentSize.height);
        }
        
        if (!self.pointerFrameSeted && _rulerFace == RulerFace_up_left) {
            self.pointerView.frame = CGRectMake(cy_selfWidth / 2.0 - (cy_selfHeight >= cy_fit(120) ? cy_fit(80) : cy_selfHeight/2.0),
                                                cy_selfHeight / 2.0 - cy_fit(1) / 2.0,
                                                cy_selfWidth >= cy_fit(120) ? cy_fit(80) : cy_selfWidth / 2.0,
                                                cy_fit(1));
        }
        
        if (!self.pointerFrameSeted && self.rulerFace == RulerFace_down_right) {
            self.pointerView.frame = CGRectMake(cy_selfWidth / 2.0,
                                                cy_selfHeight / 2.0 - cy_fit(1) / 2.0,
                                                cy_selfWidth >= cy_fit(120) ? cy_fit(80) : cy_selfWidth / 2.0,
                                                cy_fit(1));
        }
    } else {
        NSAssert(NO, @"error");
    }
    
    self.rulerView.h_height = self.h_height;
    self.rulerView.m_height = self.m_height;
    self.rulerView.lockMin = self.lockMin;
    self.rulerView.lockMax = self.lockMax;
    self.rulerView.unitValue = self.unitValue;
    self.rulerView.step = self.step;
    self.rulerView.unitPX = self.unitPX;
    self.rulerView.isTimeAlias = self.isTimeAlias;
    self.rulerView.rulerDirection = self.rulerDirection;
    self.rulerView.rulerFace = self.rulerFace;
    self.rulerView.isShowRulerValue = self.isShowRulerValue;
    self.rulerView.pointerFrame = self.pointerView.frame;
    self.rulerView.rulerBackgroundColor = self.rulerBackgroundColor;
    self.rulerView.segments = self.segments;
    self.rulerView.segmentColor = self.segmentColor;
}

- (BOOL)paramIsAvialiable {
    BOOL flag = YES;
    if (self.lockMax < self.lockMin) {
        NSAssert(NO, @"最小值应该比最大值小");
        flag = NO;
    }
    if (self.lockDefault < self.lockMin || self.lockDefault > self.lockMax) {
        NSLog(@"标尺默认值不合法，已被修改为最小值了");
        self.lockDefault = self.lockMin;
    }
    if (self.lockMin % self.step != 0 || self.lockMax % self.step != 0 || self.lockDefault % self.step != 0) {
        NSLog(@"小伙子，我不是太推荐你设置的这种参数~,但是随你……");
    }
    
    return flag;
}

- (void) setCurLabelContent:(NSInteger) seconds {
    if (!self.isTimeAlias) {
        return;
    }
    
    NSString *str_hour = [NSString stringWithFormat:@"%02ld", seconds / 3600];
    NSString *str_minute = [NSString stringWithFormat:@"%02ld", (seconds % 3600) / 60];
    NSString *str_second = [NSString stringWithFormat:@"%02ld", seconds % 60];
    // format of time
    NSString *format_time = [NSString stringWithFormat:@"%@:%@:%@", str_hour, str_minute, str_second];
    
    self.curLabel.text = format_time;
}

- (NSInteger) scrollValue {
    if (self.isTimeAlias) {
        self.unitCount = self.rulerBackgroundView.contentOffset.x / _unitPX;
    } else {
        self.unitCount = (int)(self.rulerBackgroundView.contentOffset.x / _unitPX + 0.5);
    }
    
    NSInteger num = self.unitCount * _unitValue;
    [self.rulerDelegate rulerValue:num];
    [self setCurLabelContent:num];
    
    return num;
}

#pragma mark - UIScrollViewDelegate

- (void) scrollViewWillBeginDragging:(UIScrollView *)scrollView {
    if ([self.rulerDelegate respondsToSelector:@selector(touchesBegan)]) {
        [self.rulerDelegate touchesBegan];
    }
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    if (self.rulerDirection == RulerDirectionHorizontal) {
        if (scrollView.contentOffset.x <= _unitPX * (_lockMin / _unitValue)) {
            scrollView.contentOffset = CGPointMake(_unitPX * (_lockMin / _unitValue), 0);
            [self.rulerDelegate rulerValue:_lockMin];
            [self setCurLabelContent:_lockMin];
            
            if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
                [self.rulerDelegate rulerRunEndValue:_lockMin];
            }
        }
        if (scrollView.contentOffset.x >= _unitPX * (_lockMax / _unitValue)) {
            scrollView.contentOffset = CGPointMake(_unitPX * (_lockMax / _unitValue), 0);
            [self.rulerDelegate rulerValue:_lockMax];
            [self setCurLabelContent:_lockMax];
            
            if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
                [self.rulerDelegate rulerRunEndValue:_lockMax];
            }
        }
        
        [self scrollValue];
    } else if (self.rulerDirection == RulerDirectionVertical) {
        if (scrollView.contentOffset.y <= _unitPX * (_lockMin / _unitValue)) {
            scrollView.contentOffset = CGPointMake(0, _unitPX * (_lockMin / _unitValue));
            [self.rulerDelegate rulerValue:_lockMin];
            [self setCurLabelContent:_lockMin];
            
            if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
                [self.rulerDelegate rulerRunEndValue:_lockMin];
            }
        }
        if (scrollView.contentOffset.y >= _unitPX * (_lockMax / _unitValue)) {
            scrollView.contentOffset = CGPointMake(0, _unitPX * (_lockMax / _unitValue));
            [self.rulerDelegate rulerValue:_lockMax];
            [self setCurLabelContent:_lockMax];
            
            if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
                [self.rulerDelegate rulerRunEndValue:_lockMax];
            }
        }
        
        [self scrollValue];
    } else {
        NSAssert(NO, @"error");
    }
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
    if (self.rulerDirection == RulerDirectionHorizontal) {
        if (!decelerate) {
            NSInteger num = [self scrollValue];
            
            if (!self.isTimeAlias) {
                [scrollView setContentOffset:CGPointMake(_unitPX * self.unitCount, 0) animated:YES];
            }
            
            if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
                [self.rulerDelegate rulerRunEndValue:num];
            }
        }
    } else if (self.rulerDirection == RulerDirectionVertical) {
        if (!decelerate) {
            NSInteger num = [self scrollValue];
            
            if (!self.isTimeAlias) {
                [scrollView setContentOffset:CGPointMake(0, _unitPX * self.unitCount) animated:YES];
            }
            
            if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
                [self.rulerDelegate rulerRunEndValue:num];
            }
        }
    } else {
        NSAssert(NO, @"error");
    }
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
    if (self.rulerDirection == RulerDirectionHorizontal) {
        NSInteger num = [self scrollValue];
        
        if (!self.isTimeAlias) {
            [scrollView setContentOffset:CGPointMake(_unitPX * self.unitCount, 0) animated:YES];
        }
        
        if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
            [self.rulerDelegate rulerRunEndValue:num];
        }
    } else if (self.rulerDirection == RulerDirectionVertical) {
        NSInteger num = [self scrollValue];
        
        if (!self.isTimeAlias) {
            [scrollView setContentOffset:CGPointMake(0, _unitPX * self.unitCount) animated:YES];
        }
        
        if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
            [self.rulerDelegate rulerRunEndValue:num];
        }
    } else {
        NSAssert(NO, @"error");
    }
}

- (void)scrollViewDidEndScrollingAnimation:(UIScrollView *)scrollView {
    if (self.rulerDirection == RulerDirectionHorizontal) {
        NSInteger num = [self scrollValue];
        
        if (!self.isTimeAlias) {
            [scrollView setContentOffset:CGPointMake(_unitPX * self.unitCount, 0) animated:NO];
        }
        
        if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
            [self.rulerDelegate rulerRunEndValue:num];
        }
    } else if (self.rulerDirection == RulerDirectionVertical) {
        NSInteger num = [self scrollValue];
        
        if (!self.isTimeAlias) {
            [scrollView setContentOffset:CGPointMake(0, _unitPX * self.unitCount) animated:NO];
        }
        
        if ([self.rulerDelegate respondsToSelector:@selector(rulerRunEndValue:)]) {
            [self.rulerDelegate rulerRunEndValue:num];
        }
    } else {
        NSAssert(NO, @"error");
    }
}

#pragma mark - setter

- (void)setPointerFrame:(CGRect)pointerFrame {
    _pointerFrame = pointerFrame;
    self.pointerView.frame = self.pointerFrame;
    self.pointerFrameSeted = YES;
}

- (void)setPointerBackgroundColor:(UIColor *)pointerBackgroundColor {
    _pointerBackgroundColor = pointerBackgroundColor;
    self.pointerView.backgroundColor = self.pointerBackgroundColor;
}

- (void)setPointerImage:(UIImage *)pointerImage {
    _pointerImage = pointerImage;
    self.pointerView.image = self.pointerImage;
}

- (void) setCurLabelColor:(UIColor *)curLabelColor {
    _curLabelColor = curLabelColor;
    self.curLabel.textColor = curLabelColor;
}

- (void) setCurLabelFrame:(CGRect)curLabelFrame {
    _curLabelFrame = curLabelFrame;
    self.curLabel.frame = curLabelFrame;
}

@end
