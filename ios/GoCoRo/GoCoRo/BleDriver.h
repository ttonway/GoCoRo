//
//  BleDriver.h
//  tpms-ios
//
//  Created by ttonway on 2017/1/23.
//  Copyright © 2017年 ttonway. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

typedef NS_ENUM(NSUInteger, DriverState){
    StateClose = 0,
    StateOpenging,
    StateOpen,
    StateClosing
};

extern NSErrorDomain const DriverErrorDomain;
extern NSInteger const ERROR_WRONG_DEVICE;
extern NSInteger const ERROR_TIMEOUT;
extern NSInteger const ERROR_CONNECTION_FAIL;

@protocol ScanDelegate;
@protocol DriverDelegate;

@interface BleDriver : NSObject <CBCentralManagerDelegate, CBPeripheralDelegate>

@property (nonatomic) CBCentralManager *centralManager;
@property (nonatomic, readonly) CBPeripheral *currentPeripheral;
@property (nonatomic) DriverState state;
@property (nonatomic, assign)  id<DriverDelegate> delegate;

- (void)startScan:(id<ScanDelegate>)delegate;
- (void)stopScan;

- (BOOL)connect:(CBPeripheral *)peripheral;
- (void)disconnect;

- (BOOL)writeData:(NSData *)data;

@end


@protocol ScanDelegate <NSObject>
@required
- (void)driverDidPowerOn:(BleDriver *)driver;
- (void)driver:(BleDriver *)driver didDiscoverPeripheral:(CBPeripheral *)peripheral;
@end

@protocol DriverDelegate <NSObject>

- (void)driver:(BleDriver *)driver didStateChanged:(DriverState)state;
- (void)driver:(BleDriver *)driver onError:(NSError *)error;
- (void)driver:(BleDriver *)driver didReadData:(NSData *)data;

@end
