//
//  GoCoRoDevice.h
//  GoCoRo
//
//  Created by ttonway on 2017/5/9.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BleDriver.h"
#import "RoastProfile.h"

extern NSString * const NotificationDeviceError;
extern NSString * const NotificationProfile;
extern NSString * const NotificationStateChange;


@interface GoCoRoDevice : NSObject <DriverDelegate>

@property (nonatomic, readonly) BleDriver *dirver;
@property (nonatomic, readonly) DriverState state;

@property (nonatomic, readonly) RoastProfile *profile;
@property (nonatomic, readonly) RoastStatus roastStatus;
@property (nonatomic, readonly) NSDate *lastUpdateTime;

+ (GoCoRoDevice *)sharedInstance;


- (BOOL)openDevice;
- (void)closeDevice;
- (BOOL)isOpen;
- (BOOL)isDeviceBusy;

- (void)readyProfile:(RoastProfile *)profile;
- (void)startRoast:(NSInteger)seconds fire:(NSInteger)fire cool:(NSInteger)coolTemperature;
- (void)setRoast:(NSInteger)seconds fire:(NSInteger)fire;
- (void)stopRoast;
    
@end
