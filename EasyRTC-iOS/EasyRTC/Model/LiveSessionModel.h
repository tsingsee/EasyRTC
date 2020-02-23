//
//  LiveSessionModel.h
//  EasyRTC
//
//  Created by liyy on 2020/2/2.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "BaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface Session : BaseModel

@property (nonatomic, copy) NSString *Application;
@property (nonatomic, copy) NSString *AudioBitrate;
@property (nonatomic, copy) NSString *AudioChannel;
@property (nonatomic, copy) NSString *AudioCodec;
@property (nonatomic, copy) NSString *AudioSampleRate;
@property (nonatomic, copy) NSString *AudioSampleSize;
@property (nonatomic, copy) NSString *HLS;
@property (nonatomic, copy) NSString *flv;
@property (nonatomic, copy) NSString *sessionID;
@property (nonatomic, assign) int InBitrate;
@property (nonatomic, copy) NSString *InBytes;
@property (nonatomic, copy) NSString *NumOutputs;
@property (nonatomic, copy) NSString *OutBitrate;
@property (nonatomic, copy) NSString *OutBytes;
@property (nonatomic, copy) NSString *PublisherIP;
@property (nonatomic, copy) NSString *RTMP;
@property (nonatomic, copy) NSString *RTSP;
@property (nonatomic, copy) NSString *StartTime;
@property (nonatomic, copy) NSString *Time;
@property (nonatomic, copy) NSString *VideoBitrate;
@property (nonatomic, copy) NSString *VideoCodec;
@property (nonatomic, copy) NSString *VideoHeight;
@property (nonatomic, copy) NSString *VideoWidth;

@property (nonatomic, assign) CGFloat height;

@end

@interface Sessions : BaseModel

@property (nonatomic, strong) NSArray<Session *> *sessions;

@end

@interface LiveSessionModel : BaseModel

@property (nonatomic, copy) NSString *sessionCount;
@property (nonatomic, strong) Sessions *sessions;

@end

NS_ASSUME_NONNULL_END
