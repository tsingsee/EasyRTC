//
//  LiveArrayEventParameter.h
//  venus
//
//  Created by Jac Chen on 2018/8/26.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EventParameter.h"

@interface LiveArrayEventParameter : EventParameter

@property(nonatomic, copy) NSString *laChannel;
@property(nonatomic, copy) NSString *laName;
@property(nonatomic, copy) NSString *role;
@property(nonatomic, copy) NSString *chatID;
@property(nonatomic, copy) NSString *conferenceMemberID;
@property(nonatomic, copy) NSString *chatChannel;
@property(nonatomic, copy) NSString *infoChannel;
@property(nonatomic, copy) NSString *modChannel;
@property(nonatomic, assign) NSInteger canvasCount;

@end
