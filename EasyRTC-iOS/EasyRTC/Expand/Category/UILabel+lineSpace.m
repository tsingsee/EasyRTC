//
//  UILabel+lineSpace.m
//  DrawLine
//
//  Created by lyy on 2018/11/19.
//  Copyright © 2018 HRG. All rights reserved.
//

#import "UILabel+lineSpace.h"

@implementation UILabel (lineSpace)

- (CGSize)getLabelHeightWithLineSpace:(CGFloat)lineSpace WithWidth:(CGFloat)width WithNumline:(NSInteger)lineCount {
    self.numberOfLines = lineCount;
    
    NSMutableParagraphStyle *style = [[NSMutableParagraphStyle alloc]init];
    style.lineSpacing  = lineSpace;
    
    NSMutableAttributedString *muAttrStr = [[NSMutableAttributedString alloc] initWithString:(self.text ? self.text : @"")];
    [muAttrStr addAttributes:@{ NSParagraphStyleAttributeName : style, NSFontAttributeName : self.font }
                       range:NSMakeRange(0, muAttrStr.string.length)];
    self.attributedText = muAttrStr;
    
    CGSize heightSize = [self sizeThatFits:CGSizeMake(width, MAXFLOAT)];
    if (heightSize.height - lineSpace <= self.font.pointSize + 4) {
        style.lineSpacing = 0;
        
        [muAttrStr addAttributes:@{ NSParagraphStyleAttributeName : style, NSFontAttributeName : self.font }
                           range:NSMakeRange(0, muAttrStr.string.length)];
        self.attributedText = muAttrStr;
        
        return [self sizeThatFits:CGSizeMake(width, MAXFLOAT)];
    }
    
    return heightSize;
}

- (CGSize)labelHeightWithLineSpace:(CGFloat)lineSpace WithWidth:(CGFloat)width WithNumline:(NSInteger)lineCount {
    self.numberOfLines = lineCount;
    
    NSMutableParagraphStyle *style = [[NSMutableParagraphStyle alloc] init];
    style.lineSpacing  = lineSpace;
    
    NSMutableAttributedString *muAttrStr = [[NSMutableAttributedString alloc] initWithString:(self.text ? self.text : @"")];
    NSDictionary *attributes = @{ NSParagraphStyleAttributeName : style,
                                  NSFontAttributeName : self.font
                                };
    [muAttrStr addAttributes:attributes range:NSMakeRange(0, muAttrStr.string.length)];
    self.attributedText = muAttrStr;
    
    CGSize widthSize = [self sizeThatFits:CGSizeMake(MAXFLOAT, self.font.pointSize)];
    if (widthSize.width <= width) {
        style.lineSpacing = 0;
        [muAttrStr addAttributes:@{NSParagraphStyleAttributeName:style,NSFontAttributeName:self.font} range:NSMakeRange(0, muAttrStr.string.length)];
        self.attributedText = muAttrStr;
    }
    
    CGSize heightSize = [self sizeThatFits:CGSizeMake(width, MAXFLOAT)];
    
    // 计算 \n 的数量，再加上行间距lineSpace
    for(int i = 0; i < self.text.length; i++) {
        NSString * item = [self.text substringWithRange:NSMakeRange(i, 1)];
        if ([item isEqualToString:@"\n"]) {
            // 上一个字符,不能是\r
            if (i > 0) {
                NSString * previousItem = [self.text substringWithRange:NSMakeRange(i-1, 1)];
                //  \r\n 的情况，不需要再加上行间距lineSpace，只要
                if (![previousItem isEqualToString:@"\r"]) {
                    heightSize.height += lineSpace;
                }
            }
        }
    }
    
    return heightSize;
}



@end
