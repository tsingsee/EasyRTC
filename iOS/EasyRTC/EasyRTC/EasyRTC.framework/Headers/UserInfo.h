//
//  UserInfo.h
//  venus
//
//  Created by Jac Chen on 2018/8/24.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface UserInfo : NSObject
@property (nonatomic, copy) NSString *userId;
@property (nonatomic, copy) NSString *displayName;
@property (nonatomic, copy) NSString *userEmail;
@property (nonatomic, assign) BOOL isMute;
@property (nonatomic, assign) NSInteger energy;
@property (nonatomic, copy) NSString *callId;

@end
