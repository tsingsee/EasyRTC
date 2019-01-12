//
//  ConferenceCommand.h
//  venus
//
//  Created by Jac Chen on 2018/8/24.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ConferenceCommand : NSObject
@property (nonatomic, copy) NSString *application;
@property (nonatomic, copy) NSString *command;
@property (nonatomic, copy) NSString *cmdId;

@end
