//
//  UserInfoChangeEventArgs.h
//  venus
//
//  Created by Jac Chen on 2018/8/26.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UserInfo.h"

typedef NS_ENUM(NSUInteger, UserInfoChangeReason) {
    UserInfoChangeReason_INIT,
    UserInfoChangeReason_ADD,
    UserInfoChangeReason_DELETE,
    UserInfoChangeReason_MODIFY,
};

@interface UserInfoChangeEventArgs : NSObject
@property(nonatomic, assign) UserInfoChangeReason reason;
@property(nonatomic, strong) UserInfo *userInfo;

@end
