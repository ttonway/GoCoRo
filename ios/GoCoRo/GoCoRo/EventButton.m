//
//  EventButton.m
//  GoCoRo
//
//  Created by ttonway on 2017/5/5.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "EventButton.h"


@implementation TimePickerController {
    NSArray<UIPickerView *> *pickers;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.statusPicker = [[UIPickerView alloc] initWithFrame:CGRectZero];
    self.hourPicker = [[UIPickerView alloc] initWithFrame:CGRectZero];
    self.minutePicker = [[UIPickerView alloc] initWithFrame:CGRectZero];
    self.secondPicker = [[UIPickerView alloc] initWithFrame:CGRectZero];
    pickers = @[self.statusPicker, self.hourPicker, self.minutePicker, self.secondPicker];
    for (UIPickerView *picker in pickers) {
        picker.dataSource = self;
        picker.delegate = self;
        [self.view addSubview:picker];
    }
    
    if (!self.enableStatus) {
        self.statusPicker.hidden = YES;
    }
    if (!self.enableHour) {
        self.hourPicker.hidden = YES;
    }
    
    [self.statusPicker selectRow:(self.status == StatusCooling ? 1 : 0) inComponent:0 animated:NO];
    [self.hourPicker selectRow:self.hour inComponent:0 animated:NO];
    [self.minutePicker selectRow:self.minute inComponent:0 animated:NO];
    [self.secondPicker selectRow:self.second inComponent:0 animated:NO];
}
- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];
    
    NSMutableArray *array = [NSMutableArray array];
    if (self.enableStatus) {
        [array addObject:self.statusPicker];
    }
    if (self.enableHour) {
        [array addObject:self.hourPicker];
    }
    [array addObject:self.minutePicker];
    [array addObject:self.secondPicker];
    
    CGRect rect = self.view.bounds;
    rect.size.width = rect.size.width / array.count;
    for (UIPickerView *picker in array) {
        picker.frame = rect;
        rect.origin.x += rect.size.width;
    }
}

#pragma mark - Picker view data source
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView*)pickerView {
    return 1;
}
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    if (pickerView == self.statusPicker) {
        return 2;
    } else if (pickerView == self.hourPicker) {
        return 24;
    } else if (pickerView == self.minutePicker) {
        return 60;
    } else if (pickerView == self.secondPicker) {
        return 60;
    }
    return 0;
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    if (pickerView == self.statusPicker) {
        return row == 0 ? NSLocalizedString(@"category_roast", nil) : NSLocalizedString(@"category_cool", nil);
    } else if (pickerView == self.hourPicker) {
        return [NSString stringWithFormat:@"%ld", (long)row];
    } else {
        return [NSString stringWithFormat:@"%02ld", (long)row];
    }
    return nil;
}
- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    if (pickerView == self.statusPicker) {
        self.status = row == 0 ? StatusRoasting : StatusCooling;
    } else if (pickerView == self.hourPicker) {
        self.hour = row;
    } else if (pickerView == self.minutePicker) {
        self.minute = row;
    } else if (pickerView == self.secondPicker) {
        self.second = row;
    }
}

@end


@implementation EventButton
@synthesize eventData = _eventData;

- (instancetype)init {
    self = [super initWithFrame:CGRectMake(0, 0, 125, 30)];
    if (self) {
        [self setBackgroundImage:[UIImage imageNamed:@"btn_event_normal_background"] forState:UIControlStateNormal];
        [self setBackgroundImage:[UIImage imageNamed:@"btn_event_selected_background"] forState:UIControlStateSelected];
        
        self.nameLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 7, 40, 16)];
        self.nameLabel.font = [UIFont systemFontOfSize:10.f];
        self.nameLabel.adjustsFontSizeToFitWidth = YES;
        self.nameLabel.minimumFontSize = 7.f;
        self.nameLabel.textColor = [UIColor whiteColor];
        self.nameLabel.textAlignment = NSTextAlignmentCenter;
        [self addSubview:self.nameLabel];
        self.statusLabel = [[UILabel alloc] initWithFrame:CGRectMake(50, 7, 40, 16)];
        self.statusLabel.font = [UIFont systemFontOfSize:10.f];
        self.statusLabel.adjustsFontSizeToFitWidth = YES;
        self.statusLabel.minimumFontSize = 7.f;
        self.statusLabel.textColor = [UIColor whiteColor];
        self.statusLabel.textAlignment = NSTextAlignmentCenter;
        [self addSubview:self.statusLabel];
        
        UIImageView *sep = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"btn_event_seperator"]];
        sep.center = CGPointMake(50, 15);
        [self addSubview:sep];
        
        self.statusLabel.text = NSLocalizedString(@"event_unrecorded", nil);
    }
    return self;
}

- (void)setEventData:(RoastData *)eventData {
    _eventData = eventData;
    
    self.selected = !!eventData;
    self.statusLabel.text = eventData ? NSLocalizedString(@"event_recorded", nil) : NSLocalizedString(@"event_unrecorded", nil);
}

@end
