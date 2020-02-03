//
//  SelectView.m
//  Easy
//
//  Created by leo on 2017/4/27.
//  Copyright © 2017年 leo. All rights reserved.
//

#import "SelectView.h"

@implementation SelectView

@synthesize defaultColor = _defaultColor;
@synthesize selectColor = _selectColor;

- (instancetype) initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self initView];
    }
    return self;
}

- (instancetype) initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self initView];
    }
    return self;
}

- (void) initView {
    self.userInteractionEnabled = YES;
    
    self.defaultColor = 0xffffff;
    self.selectColor = 0xf5f5f5;
    self.alphaCount = 1;
    
    [self setBackgroundColor:UIColorFromRGBA(self.defaultColor, self.alphaCount)];
}

- (void) setDefaultColor:(int)defaultColor {
    _defaultColor = defaultColor;
    [self setBackgroundColor:UIColorFromRGBA(self.defaultColor, self.alphaCount)];
}

#pragma 手势操作的方法
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    [self setBackgroundColor:UIColorFromRGBA(self.selectColor, self.alphaCount)];
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    [self setBackgroundColor:UIColorFromRGBA(self.selectColor, self.alphaCount)];
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    [self setBackgroundColor:UIColorFromRGBA(self.defaultColor, self.alphaCount)];
    
    if (self.clickListener) {
        self.clickListener(self);
    }
}

- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event {
    [self setBackgroundColor:UIColorFromRGBA(self.defaultColor, self.alphaCount)];
}

@end
