//
//  DES3Util.h
//  Easy
//
//  Created by js on 15/9/6.
//  Copyright (c) 2015年 js. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DES3Util : NSObject

// 加密方法
+ (NSString*)encrypt:(NSString*)plainText;

// 解密方法
+ (NSString*)decrypt:(NSString*)encryptText;

//将json转换为string
+(NSString*)dataTojsonString:(id)object;

//将string转NSDictionary
+(NSDictionary *)stringToNSDictionary:(NSString*)str;

+(NSArray *)stringToNSArray:(NSString*)str;

// 正则验证手机号
+(BOOL)isMobileNumber:(NSString *)mobileNum;

// 正则验证邮箱
+ (BOOL) isEmailAdress:(NSString *)Email;

+ (BOOL) isNickname:(NSString *)nickName;
+ (BOOL) isUserName:(NSString *)username;
+ (BOOL) isValidPassword:(NSString *)pwd;
+ (BOOL) checkUserID:(NSString *)userID;

/*转换首字母大写*/
+ (NSString *)firstCharactor:(NSString *)aString;

/*转换城拼音*/
+ (NSString *)transformChineseToPinyin:(NSString *)chinese;

+ (int)checkIsHaveNumAndLetter:(NSString*)password;

+ (BOOL)isPureInt:(NSString*)string;

+ (BOOL)isPureFloat:(NSString*)string;

//点击全城按钮准换txt文件
+(void)getCityListWithCompletionBlock:(void (^) (NSMutableArray *cityArray))block;

@end
