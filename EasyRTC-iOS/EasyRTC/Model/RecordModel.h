//
//  RecordModel.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/9/1.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseModel.h"

@interface RecordModel : BaseModel

@property (nonatomic, copy) NSString *name;         // 通道名称
@property (nonatomic, copy) NSString *hls;          // 录像播放链接
@property (nonatomic, copy) NSString *snap;
@property (nonatomic, copy) NSString *important;    // 重要标记
@property (nonatomic, copy) NSString *startAt;      // 开始时间, YYYYMMDDHHmmss
@property (nonatomic, assign) long duration;        // 录像时长(秒)

@property (nonatomic, assign) NSTimeInterval currentPlayTime;

- (NSString *) startAtFormat;
- (NSString *) durationFormat;

/**
 录像开始的当天的秒数

 @return 秒
 */
- (NSInteger) startAtSecond;

@end
