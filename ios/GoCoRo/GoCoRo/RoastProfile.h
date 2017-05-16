//
//  RoastProfile.h
//  GoCoRo
//
//  Created by ttonway on 2017/4/26.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <Realm/Realm.h>

typedef NS_ENUM(NSInteger, RoastStatus) {
    StatusUnknow = -1,
    StatusIdle = 0,
    StatusPreheating,
    StatusRoasting,
    StatusCooling
};

extern NSString * const EVENT_BURST1_START;//一爆開始
extern NSString * const EVENT_BURST1;//一爆密集
extern NSString * const EVENT_BURST2_START;//二爆開始
extern NSString * const EVENT_BURST2;//二爆密集

@interface RoastData : RLMObject

@property NSInteger time;
@property NSInteger fire;
@property NSInteger temperature;
@property NSInteger status;
@property NSString *event;
@property BOOL manualCool;

- (NSString *)eventName;

@end
RLM_ARRAY_TYPE(RoastData)


@interface RoastProfile : RLMObject

@property NSString *uuid;

@property NSString *deviceId;
@property NSString *deviceIdentifier;

@property NSString *people;
@property NSString *beanCountry;
@property NSString *beanName;
@property NSDate *startTime;
@property NSDate *endTime;
@property NSInteger startWeight;
@property NSInteger endWeight;
@property NSInteger envTemperature;

@property NSInteger startFire;
@property NSInteger startDruation;// in seconds
@property NSInteger coolTemperature;
@property NSInteger preHeatTime;
@property NSInteger roastTime;
@property NSInteger coolTime;
@property BOOL complete;// if roast process completed

// sync with server
@property BOOL dirty;
@property NSInteger sid;

@property RoastProfile *referenceProfile;

@property RLMArray<RoastData *><RoastData> *plotDatas;


- (NSString *)fullName;
- (NSInteger)getTimeInStatus:(NSInteger)time;

@end
