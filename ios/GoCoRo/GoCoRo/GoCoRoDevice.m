//
//  GoCoRoDevice.m
//  GoCoRo
//
//  Created by ttonway on 2017/5/9.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "GoCoRoDevice.h"

NSString * const NotificationDeviceError = @"Notification_DeviceError";
NSString * const NotificationProfile = @"Notification_ProfileUpdated";
NSString * const NotificationStateChange = @"Notification_StateChange";

#define CMD_ROAST            ((Byte)0x01)
#define CMD_SET              ((Byte)0x02)
#define CMD_STOP             ((Byte)0x03)
#define CMD_STATUS           ((Byte)0xff)

@implementation GoCoRoDevice {
    RLMRealm *realm;
    
    BOOL dataNotified;
}

@synthesize dirver = _dirver;
@synthesize state = _state;
@synthesize profile = _profile;
@synthesize roastStatus = _roastStatus;
@synthesize lastUpdateTime = _lastUpdateTime;


+ (GoCoRoDevice *)sharedInstance
{
    static  GoCoRoDevice *sharedInstance = nil;
    static  dispatch_once_t onceToken;
    dispatch_once (& onceToken, ^ {
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _dirver = [[BleDriver alloc] init];
        _dirver.delegate = self;
        _state = _dirver.state;
        
        realm = [RLMRealm defaultRealm];
        
    }
    return self;
}


- (BOOL)openDevice {
    NSLog(@"openDevice");
    CBPeripheral *peripheral = self.dirver.currentPeripheral;
    if (peripheral && [self.dirver connect:peripheral]) {
        return YES;
    }
    return NO;
}
- (void)closeDevice {
    NSLog(@"closeDevice");
    [self.dirver disconnect];
    
    _lastUpdateTime = nil;
    _roastStatus = StatusUnknow;
    
    dataNotified = NO;
    
    [self resetProfile];
}
- (BOOL)isOpen {
    return _dirver.state == StateOpen;
}
- (BOOL)isDeviceBusy {
    return _roastStatus != StatusUnknow && _roastStatus != StatusIdle && _lastUpdateTime && [_lastUpdateTime timeIntervalSinceNow] > -3;
}

- (void)checkTimeout {
    [[NSNotificationCenter defaultCenter] postNotificationName:NotificationDeviceError object:nil userInfo:nil];
    [self resetProfile];
}

- (void)readyProfile:(RoastProfile *)profile {
    NSLog(@"readyProfile");
    NSAssert(!_profile, @"ready profile when roasting");
    
    _profile = profile;
    
    [GoCoRoDevice cancelPreviousPerformRequestsWithTarget:self selector:@selector(checkTimeout) object:nil];
    [self performSelector:@selector(checkTimeout) withObject:nil afterDelay:10.0];
}
- (void)resetProfile {
    NSLog(@"resetProfile");
    if (_profile) {
        _profile = nil;
        
        [[NSNotificationCenter defaultCenter] postNotificationName:NotificationProfile object:nil userInfo:nil];
        
        [GoCoRoDevice cancelPreviousPerformRequestsWithTarget:self selector:@selector(checkTimeout) object:nil];
    }
}

- (Byte)makeParity:(Byte *)buf length:(NSInteger)length {
    Byte parity = 0;
    for (NSInteger i = 0; i < length; i++) {
        parity = (Byte) (parity ^ buf[i]);
    }
    return parity;
}

- (BOOL)writeCommand:(Byte)cmd payload:(NSData *)payload {
    Byte buf[64];
    NSInteger index = 0;
    buf[index++] = '@';
    buf[index++] = (Byte) (payload.length + 2);
    buf[index++] = cmd;
    [payload getBytes:(buf + index) length:payload.length];
    index += payload.length;
    buf[index] = [self makeParity:buf length:index];
    index++;//remove warning
    buf[index++] = '$';
    
    NSData *data = [NSData dataWithBytes:buf length:index];
    NSLog(@"[WriteData] %@", data);
    return [self. dirver writeData:data];
}

- (void)startRoast:(NSInteger)seconds fire:(NSInteger)fire cool:(NSInteger)coolTemperature {
    NSLog(@"startRoast %lds fire-%ld", (long)seconds, (long)fire);
    Byte data[4];
    data[0] = (Byte) ((seconds >> 8) & 0xFF);
    data[1] = (Byte) (seconds & 0xFF);
    data[2] = (Byte) (fire & 0xFF);
    data[3] = (Byte) (coolTemperature & 0xFF);
    [self writeCommand:CMD_ROAST payload:[NSData dataWithBytes:data length:4]];
}
- (void)setRoast:(NSInteger)seconds fire:(NSInteger)fire {
    NSLog(@"setRoast %lds fire-%ld", (long)seconds, (long)fire);
    Byte data[3];
    data[0] = (Byte) ((seconds >> 8) & 0xFF);
    data[1] = (Byte) (seconds & 0xFF);
    data[2] = (Byte) (fire & 0xFF);
    [self writeCommand:CMD_SET payload:[NSData dataWithBytes:data length:3]];
}
- (void)stopRoast {
    NSLog(@"stopRoast");
    [self writeCommand:CMD_STOP payload:nil];
}

#pragma mark - DriverDelegate
- (void)driver:(BleDriver *)driver didReadData:(NSData *)data {
    NSLog(@"[ReadData] %@", data);
    Byte *buf = (Byte *)data.bytes;
    NSUInteger length = data.length;
    NSUInteger begin;
    NSUInteger end;
    for (begin = 0; begin < length; begin++) {
        if (buf[begin] == '@') {
            for (end = begin + 1; end < length; end++) {
                if (buf[end] == '$' && (end + 1 == length || buf[end + 1] == '@')) {
                    break;
                }
            }
            
            if (end < length) { // found one frame
                NSData *frame = [data subdataWithRange:NSMakeRange(begin, end + 1 - begin)];
                begin = end;
                [self driver:driver didReadFrame:frame];
            }
        }
    }
}

- (void)driver:(BleDriver *)driver didReadFrame:(NSData *)data {
    NSLog(@"[ReadFrame] %@", data);
    Byte *buf = (Byte *)data.bytes;
    NSUInteger length = data.length;
    if (length < 5) {
        NSLog(@"[ReadFrame] length too short.");
        return;
    }
    if (buf[0] != '@' || buf[length - 1] != '$') {
        NSLog(@"[ReadFrame] wrong tags.");
        return;
    }
    if (buf[1] != length - 3) {
        NSLog(@"[ReadFrame] wrong frame length.");
        return;
    }
    Byte parity = [self makeParity:buf length:length - 2];
    if (buf[length - 2] != parity) {
        NSLog(@"[ReadFrame] wrong parity.");
        return;
    }
    
    Byte cmd = buf[2];
    Byte *payload = buf + 3;
    switch (cmd) {
        case CMD_STATUS: {
            Byte status = payload[0];
            NSInteger time = (payload[1] & 0xFF) << 8 | (payload[2] & 0xFF);
            NSInteger setTime =(payload[3] & 0xFF) << 8 | (payload[4] & 0xFF);
            Byte fire = payload[5];
            Byte temp = payload[6];
            
            [GoCoRoDevice cancelPreviousPerformRequestsWithTarget:self selector:@selector(checkTimeout) object:nil];
            _lastUpdateTime = [NSDate date];
            _roastStatus = status;
            
            [realm transactionWithBlock:^{
                if (!_profile) {
                    NSLog(@"Add roast data to a null profile.");
                    
                    if (status != StatusIdle && !dataNotified) {
                        NSLog(@"Notify to restore profile.");
                        dataNotified = true;
                        RoastProfile *firstOne = [[RoastProfile objectsWhere:@"startTime != nil"] sortedResultsUsingKeyPath:@"startTime" ascending:NO].firstObject;
                        if (firstOne) {
                            NSString *deviceId = driver.currentPeripheral.identifier.UUIDString;
                            if ([firstOne.deviceId isEqualToString:deviceId] && [firstOne.startTime timeIntervalSinceNow] >/* 30min */ -30 * 60) {
                                
                                [[NSNotificationCenter defaultCenter] postNotificationName:NotificationProfile object:firstOne userInfo:nil];
                            }
                        }
                    }
                } else {
                    RoastData *lastData = _profile.plotDatas.lastObject;
                    if (status == StatusIdle) {
                        NSLog(@"current profile compelted.");
                        if (!_profile.complete) {
                            _profile.endTime = [NSDate date];
                            _profile.complete = YES;
                            _profile.dirty = YES;
                        }
                        
                        [self resetProfile];
                        return;
                    }
                    
                    if (_profile.startDruation != setTime) {
                        _profile.startDruation = setTime;
                        _profile.dirty = YES;
                    }
                    
                    if (lastData && time <= lastData.time) {
                        NSLog(@"Bad data found for duplicate or out-of-order.");
                        return;
                    }
                    NSLog(@"Add roast data at time %ld status %ld", (long)time, (long)status);
                    if (status == StatusRoasting && _profile.roastTime == 0) {
                        NSLog(@"Roast process start!");
                        _profile.roastTime = time - 1;
                    } else if (status == StatusCooling && _profile.coolTime == 0) {
                        NSLog(@"Cool process start!");
                        _profile.coolTime = time - 1;
                    }
                    RoastData *entry = [[RoastData alloc] init];
                    entry.status = status;
                    entry.time = time;
                    entry.fire = fire;
                    entry.temperature = temp & 0xff;
                    [_profile.plotDatas addObject:entry];
                    _profile.dirty = YES;
                }
            }];

            break;
        }
        default: {
            NSLog(@"Unhandled frame.");
            break;
        }
    }
}

- (void)driver:(BleDriver *)driver onError:(NSError *)error {
    NSLog(@"BleDriver error %@", error);
    [[NSNotificationCenter defaultCenter] postNotificationName:NotificationDeviceError object:error userInfo:nil];
    
    [self closeDevice];
}

- (void)driver:(BleDriver *)driver didStateChanged:(DriverState)state {
    _state = state;
    [[NSNotificationCenter defaultCenter] postNotificationName:NotificationStateChange object:nil userInfo:nil];
}

@end
