//
//  ScanViewController.m
//  tpms-ios
//
//  Created by ttonway on 2017/1/23.
//  Copyright © 2017年 ttonway. All rights reserved.
//

#import "ScanViewController.h"

#import "Constants.h"
#import "GoCoRoDevice.h"
#import "LogoBackgroundView.h"

@interface ScanViewController () <ScanDelegate> {
    GoCoRoDevice *device;
    BleDriver *driver;
}

@property (nonatomic) UILabel *emptyLabel;

@property (nonatomic) NSMutableArray *foundDevice;

@end

@implementation ScanViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"activity_le_scan", nil);
    
    device = [GoCoRoDevice sharedInstance];
    driver = device.dirver;
    self.foundDevice = [NSMutableArray array];
    if (driver.currentPeripheral) {
        [self.foundDevice addObject:driver.currentPeripheral];
    }
    
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    self.tableView.separatorInset = UIEdgeInsetsMake(0, 8, 0, 0);
    UIView *v = [[UIView alloc] initWithFrame:CGRectZero];
    [self.tableView setTableFooterView:v];
    self.tableView.backgroundView = [[LogoBackgroundView alloc] init];
   
    self.emptyLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    self.emptyLabel.text = NSLocalizedString(@"Bluetooth unavailable.", nil);
    self.emptyLabel.textColor = [UIColor lightTextColor];
    self.emptyLabel.textAlignment = NSTextAlignmentCenter;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(refreshTable:) name:NotificationStateChange object:nil];
}

- (void)refreshTable:(NSNotification *)notification {
    NSLog(@"state changed");
    [self.tableView reloadData];
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];
    
    self.emptyLabel.frame = self.tableView.bounds;
}

- (void)driver:(BleDriver *)service didDiscoverPeripheral:(CBPeripheral *)peripheral {
    if (![self.foundDevice containsObject:peripheral]) {
        [self.foundDevice addObject:peripheral];
        [self.tableView reloadData];
    }
}

- (void)driverDidPowerOn:(BleDriver *)service {
    [self.emptyLabel removeFromSuperview];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];

    
    [driver startScan:self];
    if (driver.centralManager.state == CBCentralManagerStatePoweredOn) {
        [self.emptyLabel removeFromSuperview];
    } else {
        [self.tableView addSubview:self.emptyLabel];
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [driver stopScan];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.foundDevice.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellIdentifier = @"device-identifier";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];

    UIView *indicator;
    UILabel *stateLabel;
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
        cell.backgroundColor = [UIColor clearColor];
        cell.textLabel.textColor = [UIColor lightTextColor];
        
        indicator = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 8, 44)];
        indicator.backgroundColor = [UIColor colorWithRed:255.0f/255.0f green:74.0f/255.0f blue:73.0f/255.0f alpha:1.0f];
        indicator.tag = 5;
        [cell.contentView addSubview:indicator];
        
        CGFloat width = CGRectGetWidth(tableView.bounds);
        stateLabel = [[UILabel alloc] initWithFrame:CGRectMake(width - 8 - 200, 11, 200, 21)];
        stateLabel.textColor = [UIColor lightTextColor];
        stateLabel.textAlignment = NSTextAlignmentRight;
        stateLabel.tag = 6;
        [cell.contentView addSubview:stateLabel];
    } else {
        indicator = [cell.contentView viewWithTag:5];
        stateLabel = [cell.contentView viewWithTag:6];
    }
    
    CBPeripheral *peripheral = [self.foundDevice objectAtIndex:indexPath.row];
    NSString *name = peripheral.name;
    if (name.length == 0) {
        name = NSLocalizedString(@"label_unknown_device", nil);
    }
    cell.textLabel.text = name;
    
    if ([peripheral isEqual:driver.currentPeripheral]) {
        indicator.hidden = NO;
        switch (device.state) {
            case StateOpen:
                stateLabel.text = NSLocalizedString(@"state_open", nil);
                break;
            case StateClose:
                stateLabel.text = NSLocalizedString(@"state_close", nil);
                break;
            case StateOpenging:
                stateLabel.text = NSLocalizedString(@"state_opening", nil);
                break;
            case StateClosing:
                stateLabel.text = NSLocalizedString(@"state_closing", nil);
                break;
            default:
                stateLabel.text = nil;
                break;
        }
    } else {
        indicator.hidden = YES;
        stateLabel.text = nil;
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    CBPeripheral *peripheral = [self.foundDevice objectAtIndex:indexPath.row];
    if (![peripheral isEqual:driver.currentPeripheral]) {
        [device closeDevice];
        [driver connect:peripheral];
        [device openDevice];
    } else {
        if ([device isOpen]) {
            [device closeDevice];
        } else {
            [device openDevice];
        }
    }
    
    [self.tableView reloadData];
}

@end
