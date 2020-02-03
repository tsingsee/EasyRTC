//
//  NetDataReturnModel.h
//  Easy
//
//  Created by leo on 2018/6/8.
//  Copyright © 2018年 leo. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, ReturnType) {
    ReturnSuccess,
    ReturnFailure,
    ReturnProcess,
    ReturnValidToken
};

@interface NetDataReturnModel : NSObject

@property (nonatomic, assign) ReturnType type;

@property (nonatomic, strong) id result;
@property (nonatomic, copy) NSString *error;
@property (nonatomic, assign) float process;

@end
