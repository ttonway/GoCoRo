//
//  BleDriver.m
//  tpms-ios
//
//  Created by ttonway on 2017/1/23.
//  Copyright © 2017年 ttonway. All rights reserved.
//

#import "BleDriver.h"

#define NOTIFY_SERVICE   @"FFE0"
#define WRITE_SERVICE    @"FFE5"

#define NOTIFY_CHARACTERISTIC @"FFE4"
#define WRITE_CHARACTERISTIC  @"FFE9"

NSErrorDomain const DriverErrorDomain = @"DriverErrorDomain";
NSInteger const ERROR_WRONG_DEVICE = 1;
NSInteger const ERROR_TIMEOUT = 2;
NSInteger const ERROR_CONNECTION_FAIL = 3;

@implementation BleDriver {
    BOOL scaning;
    id<ScanDelegate> scanDelegate;
    
    CBUUID *writeServiceUUID;
    CBUUID *notifyServiceUUID;
    CBUUID *writeCharacteristicUUID;
    CBUUID *notifyCharacteristicUUID;
    CBCharacteristic *writeCharac;
    CBCharacteristic *notifyCharac;
    BOOL notifyEnabled;
    
    CBPeripheral *connectPeripheral;
}


@synthesize currentPeripheral = _currentPeripheral;
@synthesize state = _state;

- (instancetype)init {
    self = [super init];
    if (self) {
        dispatch_queue_t queue = dispatch_get_main_queue();
        self.centralManager = [[CBCentralManager alloc] initWithDelegate:self
                                                                   queue:queue
                                                                 options:@{ CBCentralManagerOptionRestoreIdentifierKey:@"ttonway-GoCoRo-identifier" }];
        
        writeServiceUUID = [CBUUID UUIDWithString:WRITE_SERVICE];
        notifyServiceUUID = [CBUUID UUIDWithString:NOTIFY_SERVICE];
        writeCharacteristicUUID = [CBUUID UUIDWithString:WRITE_CHARACTERISTIC];
        notifyCharacteristicUUID = [CBUUID UUIDWithString:NOTIFY_CHARACTERISTIC];
        
        
        // restore peripheral
        NSString *uuidString = [[NSUserDefaults standardUserDefaults] objectForKey:@"address"];
        if (uuidString) {
            NSUUID *uuid = [[NSUUID alloc] initWithUUIDString:uuidString];
            NSArray * peripherals = [self.centralManager retrievePeripheralsWithIdentifiers:@[uuid]];
            _currentPeripheral = peripherals.firstObject;
        }
    }
    return self;
}

- (void)dealloc {
}

- (void)setState:(DriverState)state {
    _state = state;
    [self.delegate driver:self didStateChanged:state];
}

- (void)startScan:(id<ScanDelegate>)delegate {
    scanDelegate = delegate;
    
    if (scaning) {
        
    } else if (self.centralManager.state != CBCentralManagerStatePoweredOn) {
        NSLog(@"startScan when centralManager state is %ld", (long)self.centralManager.state);
        scaning = YES;
    } else {
        NSLog(@"Scanning started");
        scaning = YES;
        [self.centralManager scanForPeripheralsWithServices:nil
                                                    options:@{ CBCentralManagerScanOptionAllowDuplicatesKey : @YES }];
    }
}

- (void)stopScan {
    NSLog(@"stopScan");
    scanDelegate = nil;
    if (scaning) {
        scaning = NO;
        [self.centralManager stopScan];
    }
}


- (BOOL)connect:(CBPeripheral *)peripheral {
    NSLog(@"connectToPeripheral %@", peripheral);
    if ([peripheral isEqual:connectPeripheral]) {
        return YES;
    }
    
    if (connectPeripheral) {
        [self disconnect];
    }
    
    if (self.centralManager.state == CBCentralManagerStatePoweredOn) {
        _currentPeripheral = peripheral;
        [[NSUserDefaults standardUserDefaults] setObject:peripheral.identifier.UUIDString forKey:@"address"];
        self.state = StateOpenging;
        
        connectPeripheral = peripheral;
        [self.centralManager connectPeripheral:peripheral options:nil];
        return YES;
    } else {
        NSLog(@"connect fail. BLE power off");
        return NO;
    }
}

- (void)disconnect {
    NSLog(@"disconnect");
    
    if (connectPeripheral) {
        if (notifyEnabled) {
            if (connectPeripheral.state == CBPeripheralStateConnected) {
                [connectPeripheral setNotifyValue:NO forCharacteristic:notifyCharac];
            }
            notifyEnabled = NO;
        }
        [self.centralManager cancelPeripheralConnection:connectPeripheral];
        connectPeripheral = nil;
        writeCharac = notifyCharac = nil;
        self.state = StateClose;
    }
}


#pragma mark - Central Methods
-(void)centralManager:(CBCentralManager *)central willRestoreState:(NSDictionary<NSString *,id> *)dict {
    NSLog(@"willRestoreState");
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    NSLog(@"centralManagerDidUpdateState %ld", (long)central.state);
    if (central.state != CBCentralManagerStatePoweredOn) {
        
        if (self.state == StateOpenging) {
            NSError *error = [NSError errorWithDomain:DriverErrorDomain code:ERROR_CONNECTION_FAIL userInfo:nil];
            [self.delegate driver:self onError:error];
        }
    } else {
        if (_currentPeripheral && !connectPeripheral) {
            [self connect:_currentPeripheral];
        }
        
        if (scanDelegate) {
            [scanDelegate driverDidPowerOn:self];
        }
        if (scaning) {
            NSLog(@"Scanning started");
            [self.centralManager scanForPeripheralsWithServices:nil
                                                        options:@{ CBCentralManagerScanOptionAllowDuplicatesKey : @YES }];
        }
    }
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {
    if (scanDelegate) {
        [scanDelegate driver:self didDiscoverPeripheral:peripheral];
    }
}


/** If the connection fails for whatever reason, we need to deal with it.
 */
- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    NSLog(@"Failed to connect to %@. (%@)", peripheral, [error localizedDescription]);
    [self.delegate driver:self onError:error];
}


/** We've connected to the peripheral, now we need to discover the services and characteristics to find the 'transfer' characteristic.
 */
- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    NSLog(@"Peripheral Connected %@", peripheral);
    
    // Stop scanning
    if (!scanDelegate && scaning) {
        scaning = NO;
        [self.centralManager stopScan];
    }
    
    // Search only for services that match our UUID
    peripheral.delegate = self;
    [peripheral discoverServices:@[writeServiceUUID, notifyServiceUUID]];
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    NSLog(@"Peripheral Disconnected %@ %@", peripheral, error);
    
//    if (!error) {
//        error = [NSError errorWithDomain:TpmsErrorDomain code:ERROR_CONNECTION_FAIL userInfo:nil];
//    }
//    [self.delegate driver:self onError:error];
    
    writeCharac = notifyCharac = nil;
    self.state = StateOpenging;
    [self.centralManager connectPeripheral:peripheral options:nil];
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    NSLog(@"didDiscoverServices %@ %@", peripheral, error);
    NSLog(@"Service list is %@", peripheral.services);
    if (error) {
        [self.delegate driver:self onError:error];
    }
    
    CBService *writeService;
    CBService *notifyService;
    for (CBService *service in peripheral.services) {
        if ([service.UUID isEqual:writeServiceUUID]) {
            writeService = service;
            [peripheral discoverCharacteristics:@[writeCharacteristicUUID]
                                     forService:service];
        } else if ([service.UUID isEqual:notifyServiceUUID]) {
            notifyService = service;
            [peripheral discoverCharacteristics:@[notifyCharacteristicUUID]
                                     forService:service];
        }
    }

    if (!writeService || !notifyService) {
        NSError *error = [NSError errorWithDomain:DriverErrorDomain code:ERROR_WRONG_DEVICE userInfo:nil];
        [self.delegate driver:self onError:error];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    NSLog(@"didDiscoverCharacteristicsForService %@ %@", service, error);
    if (error) {
        [self.delegate driver:self onError:error];
    }
    
    for (CBCharacteristic *characteristic in service.characteristics) {
        NSLog(@"find characteristic %@ prop %lx", characteristic, (unsigned long)characteristic.properties);
        
        if ([characteristic.UUID isEqual:writeCharacteristicUUID]) {
            writeCharac = characteristic;
        } else if ([characteristic.UUID isEqual:notifyCharacteristicUUID]) {
            notifyCharac = characteristic;
            [peripheral setNotifyValue:YES forCharacteristic:notifyCharac];
            notifyEnabled = YES;
        }
    }
    
    if ([service.UUID isEqual:writeServiceUUID] && !writeCharac) {
        NSError *error = [NSError errorWithDomain:DriverErrorDomain code:ERROR_WRONG_DEVICE userInfo:nil];
        [self.delegate driver:self onError:error];
    } else if ([service.UUID isEqual:notifyServiceUUID] && !notifyCharac) {
        NSError *error = [NSError errorWithDomain:DriverErrorDomain code:ERROR_WRONG_DEVICE userInfo:nil];
        [self.delegate driver:self onError:error];
    } else if (writeCharac && notifyCharac) {
        self.state = StateOpen;
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateNotificationStateForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    NSLog(@"didUpdateNotificationStateForCharacteristic %@ %@", characteristic, error);
    if (error) {
        [self.delegate driver:self onError:error];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didWriteValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    NSLog(@"didWriteValueForCharacteristic %@ %@", characteristic, error);
    if (error) {
        [self.delegate driver:self onError:error];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
//    NSLog(@"didUpdateValueForCharacteristic %@ %@", characteristic, error);
    if (error) {
        [self.delegate driver:self onError:error];
    }
    
    [self.delegate driver:self didReadData:characteristic.value];
}


- (BOOL)writeData:(NSData *)data {
//    NSLog(@"writeData %@", data);
    if (writeCharac) {
        [connectPeripheral writeValue:data forCharacteristic:writeCharac type:CBCharacteristicWriteWithoutResponse];
        return YES;
    } else {
        return NO;
    }
}

@end
