//
//  DialogParameter.h
//  venus
//
//  Created by Jac Chen on 2018/8/2.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DialogParameter : NSObject

@property(nonatomic, copy) NSString *callID;
@property(nonatomic, copy) NSString *destination_number;
@property(nonatomic, copy) NSString *login;
@property(nonatomic, copy) NSString *remote_caller_id_name;
@property(nonatomic, copy) NSString *remote_caller_id_number;
@property(nonatomic, copy) NSString *caller_id_name;
@property(nonatomic, copy) NSString *caller_id_number;
@property(nonatomic, copy) NSString *tag;
@property(nonatomic, copy) NSString *useMic;
@property(nonatomic, copy) NSString *useSpeak;
@property(nonatomic, assign) BOOL screenShare;
@property(nonatomic, assign) BOOL useStereo;
@property(nonatomic, assign) BOOL useVideo;

+ (DialogParameter *)createWithDestNumber:(NSString *)destNumber andUserName:(NSString *)username displayName:(NSString *)displayName email:(NSString *)email callId:(NSString *)callId;
@end
