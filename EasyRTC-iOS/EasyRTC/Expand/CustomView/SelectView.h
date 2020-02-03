//
//  SelectView.h
//  Easy
//
//  Created by leo on 2017/4/27.
//  Copyright © 2017年 leo. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef void (^SelectViewListener)(id result);

@interface SelectView : UIView<UIGestureRecognizerDelegate> {
    int _defaultColor;
    int _selectColor;
}

@property (nonatomic, assign) int defaultColor;
@property (nonatomic, assign) int selectColor;
@property (nonatomic, assign) float alphaCount;

@property (nonatomic, strong) SelectViewListener clickListener;

@end
