//
//  EventButton.h
//  GoCoRo
//
//  Created by ttonway on 2017/5/5.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "RoastProfile.h"

@interface TimePickerController : UIViewController <UIPickerViewDataSource, UIPickerViewDelegate>

@property (nonatomic) UIPickerView *statusPicker;
@property (nonatomic) UIPickerView *hourPicker;
@property (nonatomic) UIPickerView *minutePicker;
@property (nonatomic) UIPickerView *secondPicker;

@property (nonatomic) BOOL enableStatus;
@property (nonatomic) BOOL enableHour;

@property (nonatomic) RoastStatus status;
@property (nonatomic) NSInteger hour;
@property (nonatomic) NSInteger minute;
@property (nonatomic) NSInteger second;

@end



@interface EventButton : UIButton

@property (nonatomic) UILabel *nameLabel;
@property (nonatomic) UILabel *statusLabel;

@property (nonatomic) NSString *event;
@property (nonatomic) RoastData *eventData;

@end
