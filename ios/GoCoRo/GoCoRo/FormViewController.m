//
//  FormViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/5/4.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "FormViewController.h"

#import "Constants.h"
#import "LogoBackgroundView.h"

@interface FormViewController () <UITextFieldDelegate> {
    NSMutableArray<RoastData *> *preheatData;
    NSMutableArray<RoastData *> *roastData;
    NSMutableArray<RoastData *> *coolData;
    NSInteger roastStartTime;
    NSInteger coolStartTime;
    NSMutableArray<RoastData *> *fireChangedData;
    RoastData *completeData;
    
    NSArray *cellArray;
    UITextField *peopleInput;
    UITextField *countryInput;
    UITextField *beanInput;
    UITextField *beginWeightInput;
    UITextField *endWeightInput;
    UITextField *weightRatioInput;
    UITextField *tempInput;
    UITextField *dateInput;
    UITextField *beginTimeInput;
    UITextField *endTimeInput;
    UITextField *startFireInput;
    
    UILabel *tempLabel;
    
    NSArray *inputArray;
    CGFloat oldContentOffsetY;
    
    BOOL changed;
}
@end

static const CGFloat formInset = 16;

@implementation FormViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"activity_form", nil);
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ic_menu_share"] style:UIBarButtonItemStylePlain target:self action:@selector(shareProfile:)];
    
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    self.tableView.allowsSelection = NO;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
//    self.tableView.contentInset = UIEdgeInsetsMake(formInset, 0, formInset, 0);
    UIView *header = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 100, formInset)];
    header.backgroundColor = [UIColor clearColor];
    self.tableView.tableHeaderView = header;
    UIView *footer = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 100, formInset)];
    footer.backgroundColor = [UIColor clearColor];
    self.tableView.tableFooterView = footer;
    self.tableView.backgroundView = [[LogoBackgroundView alloc] init];
    
    peopleInput = [[UITextField alloc] initWithFrame:CGRectZero];
    countryInput = [[UITextField alloc] initWithFrame:CGRectZero];
    beanInput = [[UITextField alloc] initWithFrame:CGRectZero];
    beginWeightInput = [[UITextField alloc] initWithFrame:CGRectZero];
    endWeightInput = [[UITextField alloc] initWithFrame:CGRectZero];
    weightRatioInput = [[UITextField alloc] initWithFrame:CGRectZero];
    tempInput = [[UITextField alloc] initWithFrame:CGRectZero];
    dateInput = [[UITextField alloc] initWithFrame:CGRectZero];
    beginTimeInput = [[UITextField alloc] initWithFrame:CGRectZero];
    endTimeInput = [[UITextField alloc] initWithFrame:CGRectZero];
    startFireInput = [[UITextField alloc] initWithFrame:CGRectZero];
    
    NSArray *titles = @[@"label_people", @"label_country", @"label_bean", @"label_begin_weight", @"label_end_weight", @"label_weight_ratio", @"label_temperature", @"label_date", @"label_begin_time", @"label_end_time", @"label_start_fire"];
    inputArray = @[peopleInput, countryInput, beanInput, beginWeightInput, endWeightInput, weightRatioInput, tempInput, dateInput, beginTimeInput, endTimeInput, startFireInput];
    NSMutableArray *array = [NSMutableArray arrayWithCapacity:11];
    for (NSInteger i = 0; i < 11; i++) {
        UITextField *input = inputArray[i];
        input.delegate = self;
        [array addObject:[self newInputCell:NSLocalizedString(titles[i], nil) input:input]];
    }
    cellArray = array;
    
    beginWeightInput.keyboardType = UIKeyboardTypeNumberPad;
    endWeightInput.keyboardType = UIKeyboardTypeNumberPad;
    weightRatioInput.enabled = NO;
    tempInput.keyboardType = UIKeyboardTypeNumberPad;
    dateInput.enabled = NO;
    beginTimeInput.enabled = NO;
    endTimeInput.enabled = NO;
    startFireInput.enabled = NO;
    peopleInput.text = self.profile.people;
    countryInput.text = self.profile.beanCountry;
    beanInput.text = self.profile.beanName;
    beginWeightInput.text = [NSString stringWithFormat:@"%ld", (long)self.profile.startWeight];
    endWeightInput.text = [NSString stringWithFormat:@"%ld", (long)self.profile.endWeight];
    weightRatioInput.text = [self formatWeightRatio:self.profile.startWeight with:self.profile.endWeight];
    tempInput.text = [NSString stringWithFormat:@"%ld", (long)self.profile.envTemperature];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = NSDateFormatterMediumStyle;
    dateFormatter.timeStyle = NSDateFormatterNoStyle;
    dateInput.text = [dateFormatter stringFromDate:self.profile.startTime];
    dateFormatter.dateStyle = NSDateFormatterNoStyle;
    dateFormatter.timeStyle = NSDateFormatterMediumStyle;
    beginTimeInput.text = [dateFormatter stringFromDate:self.profile.startTime];
    endTimeInput.text = self.profile.endTime ? [dateFormatter stringFromDate:self.profile.endTime] : @"-";
    startFireInput.text = [NSString stringWithFormat:@"%ld", (long)self.profile.startFire];
    
    [self buildData];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [self saveProfile];
}

- (void)saveProfile {
    [self endEditing];
    if (changed) {
        
        RLMRealm *realm = [RLMRealm defaultRealm];
        [realm beginWriteTransaction];
        self.profile.people = peopleInput.text;
        self.profile.beanCountry = countryInput.text;
        self.profile.beanName = beanInput.text;
        self.profile.startWeight = [beginWeightInput.text integerValue];
        self.profile.endWeight = [endWeightInput.text integerValue];
        self.profile.envTemperature = [tempInput.text integerValue];
        self.profile.dirty = YES;
        [realm commitWriteTransaction];
    }
}

- (NSString *)formatWeightRatio:(NSInteger)startWeight  with:(NSInteger)endWeight {
    return startWeight <= 0 ? @"-" : [NSString stringWithFormat:@"%.2f%%", (1.f - endWeight / (float) startWeight) * 100];
}

- (UITableViewCell *)newInputCell:(NSString *)title input:(UITextField *)input {
    UITableViewCell *cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:nil];
    cell.backgroundColor = [UIColor clearColor];
    
    UIView *border1 = [[UIView alloc] initWithFrame:CGRectZero];
    border1.layer.borderWidth = 0.5f;
    border1.layer.borderColor = [UIColor lightTextColor].CGColor;
    UIView *border2 = [[UIView alloc] initWithFrame:CGRectZero];
    border2.layer.borderWidth = 0.5f;
    border2.layer.borderColor = [UIColor lightTextColor].CGColor;
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectZero];
    label.font = [UIFont systemFontOfSize:15];
    label.textColor = [UIColor lightTextColor];
    label.text = title;
    
    input.font = [UIFont systemFontOfSize:15];
    input.textColor = [UIColor lightTextColor];
    input.tintColor = [UIColor lightTextColor];
    
    border1.tag = 1;
    border2.tag = 2;
    label.tag = 9;
    input.tag = 9;
    [border1 addSubview:label];
    [border2 addSubview:input];
    [cell.contentView addSubview:border1];
    [cell.contentView addSubview:border2];

    return cell;
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    if (textField == beginWeightInput || textField == endWeightInput) {
        NSInteger start = [beginWeightInput.text integerValue];
        NSInteger end = [endWeightInput.text integerValue];
        weightRatioInput.text = [self formatWeightRatio:start with:end];
    }
    changed = YES;
}

- (void)endEditing {
    for (UIView *input in inputArray) {
        if (input.isFirstResponder) {
            [input resignFirstResponder];
            break;
        }
    }
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    if (scrollView.contentOffset.y < oldContentOffsetY - 16) {
        [self endEditing];
    }
    oldContentOffsetY = scrollView.contentOffset.y;
}

- (void)buildData {
    roastStartTime = self.profile.roastTime;
    coolStartTime = self.profile.coolTime;
    preheatData = [NSMutableArray array];
    roastData = [NSMutableArray array];
    coolData = [NSMutableArray array];
    fireChangedData = [NSMutableArray array];
    
    BOOL hasEvents;
    NSInteger lastFire = -1;
    RoastData *last = self.profile.plotDatas.lastObject;
    if (self.profile.complete) {
        completeData = last;
    }
    for (RoastData *entry in self.profile.plotDatas) {
        BOOL fireChanged = NO;
        if (entry.status == StatusRoasting) {
            if (lastFire != -1 && lastFire != entry.fire) {
                fireChanged = YES;
                [fireChangedData addObject:entry];
            }
            lastFire = entry.fire;
        }
        
        hasEvents = entry.event || fireChanged || entry.manualCool || [entry isEqualToObject:completeData];
        
        NSInteger time = entry.time;
        if (entry.status == StatusPreheating) {
            hasEvents = NO;
        } else if (entry.status == StatusRoasting) {
            time = time - roastStartTime;
        } else if (entry.status == StatusCooling) {
            time = time - coolStartTime;
        }
        
        if (hasEvents || (time > 0 && time % 60 == 0)) {
            // add this entry
        } else {
            continue;
        }
        
        if (entry.status == StatusPreheating) {
            [preheatData addObject:entry];
        } else if (entry.status == StatusRoasting) {
            [roastData addObject:entry];
        } else if (entry.status == StatusCooling) {
            [coolData addObject:entry];
        }
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source
- (NSArray *)datasInSection:(NSInteger)section {
    switch (section) {
        case 1:
            return preheatData;
        case 2:
            return roastData;
        case 3:
            return coolData;
        default:
            return nil;
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 4;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return section == 0 ? 0 : 30;
}
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        return nil;
    }
    
    CGFloat w = CGRectGetWidth(tableView.bounds);
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, 30)];
    headerView.backgroundColor = [UIColor clearColor];
    
    UIView *border = [[UIView alloc] initWithFrame:CGRectMake(formInset, 0, w - formInset * 2, 30)];
    border.backgroundColor = [UIColor customOrangeColor];
    border.layer.borderWidth = 0.5f;
    border.layer.borderColor = [UIColor lightTextColor].CGColor;
    [headerView addSubview:border];
    UIView *leftSep = [[UIView alloc] initWithFrame:CGRectMake(formInset, 0, 1, 30)];
    leftSep.backgroundColor = [UIColor lightTextColor];
    [headerView addSubview:leftSep];
    UIView *rightSep = [[UIView alloc] initWithFrame:CGRectMake(w - formInset - 1, 0, 1, 30)];
    rightSep.backgroundColor = [UIColor lightTextColor];
    [headerView addSubview:rightSep];

    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(8, 0, w - formInset * 2 - 8, 30)];
    label.backgroundColor = [UIColor customOrangeColor];
    label.textColor = [UIColor darkTextColor];
    label.font = [UIFont boldSystemFontOfSize:17.f];
    [border addSubview:label];
    
    if (section == 1) {
        label.text = NSLocalizedString(@"category_preheat", nil);
    } else if (section == 2) {
        label.text = NSLocalizedString(@"category_roast", nil);
    } else if (section == 3) {
        label.text = NSLocalizedString(@"category_cool", nil);
    }
    return headerView;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return section == 0 ? cellArray.count : [self datasInSection:section].count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (!tempLabel) {
        tempLabel = [[UILabel alloc] initWithFrame:CGRectZero];
        tempLabel.font = [UIFont systemFontOfSize:15];
        tempLabel.textColor = [UIColor lightTextColor];
        tempLabel.numberOfLines = 0;
        tempLabel.lineBreakMode = NSLineBreakByWordWrapping;
    }
    
    if (indexPath.section == 0) {
        return 44;
    } else if (indexPath.section == 1) {
        return 44;
    } else if (indexPath.section == 2) {
        CGFloat width = CGRectGetWidth(tableView.bounds) - formInset * 2;
        CGFloat w = width / 4 - 16;
        CGSize constraintSize = CGSizeMake(w, MAXFLOAT);
        
        NSArray *datas = [self datasInSection:indexPath.section];
        RoastData *data = [datas objectAtIndex:indexPath.row];
        tempLabel.text = [self eventsText:data];
        CGSize size = [tempLabel sizeThatFits:constraintSize];
        return MAX(44, size.height);
    } else if (indexPath.section == 3) {
        CGFloat width = CGRectGetWidth(tableView.bounds) - formInset * 2;
        CGFloat w = width / 3 - 16;
        CGSize constraintSize = CGSizeMake(w, MAXFLOAT);
        
        NSArray *datas = [self datasInSection:indexPath.section];
        RoastData *data = [datas objectAtIndex:indexPath.row];
        tempLabel.text = [self eventsText:data];
        CGSize size = [tempLabel sizeThatFits:constraintSize];
        return MAX(44, size.height);
    }
    return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell;
    CGFloat width = CGRectGetWidth(tableView.bounds) - formInset * 2;
    CGFloat height = [self tableView:tableView heightForRowAtIndexPath:indexPath];
    
    if (indexPath.section == 0) {
        cell = [cellArray objectAtIndex:indexPath.row];
        
        UIView *border1 = [cell.contentView viewWithTag:1];
        UIView *border2 = [cell.contentView viewWithTag:2];
        border1.frame = CGRectMake(formInset, 0, 120, height);
        border2.frame = CGRectMake(formInset + 120, 0, width - 120, height);
        UILabel *label1 = [border1 viewWithTag:9];
        UILabel *label2 = [border2 viewWithTag:9];
        label1.frame = CGRectInset(border1.bounds, 8, 0);
        label2.frame = CGRectInset(border2.bounds, 8, 0);
    } else {
        
        static NSString *cellIdentifier = @"FormIdentifier";
        cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
        
        UIView *border1, *border2, *border3, *border4;
        UILabel *label1, *label2, *label3, *label4;
        if (!cell) {
            cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellIdentifier];
            cell.backgroundColor = [UIColor clearColor];
            
            border1 = [[UIView alloc] initWithFrame:CGRectZero];
            border2 = [[UIView alloc] initWithFrame:CGRectZero];
            border3 = [[UIView alloc] initWithFrame:CGRectZero];
            border4 = [[UIView alloc] initWithFrame:CGRectZero];
            label1 = [[UILabel alloc] initWithFrame:CGRectZero];
            label2 = [[UILabel alloc] initWithFrame:CGRectZero];
            label3 = [[UILabel alloc] initWithFrame:CGRectZero];
            label4 = [[UILabel alloc] initWithFrame:CGRectZero];
            
            for (UILabel *label in @[label1, label2, label3, label4]) {
                label.font = [UIFont systemFontOfSize:15];
                label.textColor = [UIColor lightTextColor];
                label.numberOfLines = 0;
                label.lineBreakMode = NSLineBreakByWordWrapping;
                label.tag = 9;
            }
            NSInteger tag = 1;
            for (UIView *border in @[border1, border2, border3, border4]) {
                border.layer.borderWidth = 0.5f;
                border.layer.borderColor = [UIColor lightTextColor].CGColor;
                border.tag = tag++;
                [cell.contentView addSubview:border];
            }
            [border1 addSubview:label1];
            [border2 addSubview:label2];
            [border3 addSubview:label3];
            [border4 addSubview:label4];
        } else {
            border1 = [cell.contentView viewWithTag:1];
            border2 = [cell.contentView viewWithTag:2];
            border3 = [cell.contentView viewWithTag:3];
            border4 = [cell.contentView viewWithTag:4];
            label1 = [border1 viewWithTag:9];
            label2 = [border2 viewWithTag:9];
            label3 = [border3 viewWithTag:9];
            label4 = [border4 viewWithTag:9];
        }
        
        NSArray *datas = [self datasInSection:indexPath.section];
        RoastData *data = [datas objectAtIndex:indexPath.row];
        NSString *events = [self eventsText:data];
        CGRect rect;
        if (datas == preheatData) {
            rect = CGRectMake(0, 0, width / 2, height);
            label1.frame = CGRectInset(rect, 8, 0);
            label2.frame = CGRectInset(rect, 8, 0);
            label3.frame = CGRectInset(rect, 8, 0);
            label4.frame = CGRectInset(rect, 8, 0);
            rect.origin.x = formInset;
            border1.frame = rect;
            rect.origin.x += rect.size.width;
            border2.frame = rect;
            border3.frame = CGRectZero;
            border4.frame = CGRectZero;
            
            label1.text = [Utils formatSeconds:data.time];
            label2.text = [NSString stringWithFormat:@"%ld℃", (long)data.temperature];
            label3.text = nil;
            label4.text = nil;
        } else if (datas == roastData) {
            rect = CGRectMake(0, 0, width / 4, height);
            label1.frame = CGRectInset(rect, 8, 0);
            label2.frame = CGRectInset(rect, 8, 0);
            label3.frame = CGRectInset(rect, 8, 0);
            label4.frame = CGRectInset(rect, 8, 0);
            rect.origin.x = formInset;
            border1.frame = rect;
            rect.origin.x += rect.size.width;
            border2.frame = rect;
            rect.origin.x += rect.size.width;
            border3.frame = rect;
            rect.origin.x += rect.size.width;
            border4.frame = rect;
            
            label1.text = [Utils formatSeconds:data.time - roastStartTime];
            label2.text = [NSString stringWithFormat:@"%ld℃", (long)data.temperature];
            label3.text = [NSString stringWithFormat:NSLocalizedString(@"label_fire_x", nil), (long)data.fire];
            label4.text = events;
        } else if (datas == coolData) {
            rect = CGRectMake(0, 0, width / 3, height);
            label1.frame = CGRectInset(rect, 8, 0);
            label2.frame = CGRectInset(rect, 8, 0);
            label3.frame = CGRectInset(rect, 8, 0);
            label4.frame = CGRectInset(rect, 8, 0);
            rect.origin.x = formInset;
            border1.frame = rect;
            rect.origin.x += rect.size.width;
            border2.frame = rect;
            rect.origin.x += rect.size.width;
            border3.frame = rect;
            border4.frame = CGRectZero;
            
            label1.text = [Utils formatSeconds:data.time - coolStartTime];
            label2.text = [NSString stringWithFormat:@"%ld℃", (long)data.temperature];
            label3.text = events;
            label4.text = nil;
        }
    }
    
    UIView *leftSep = [cell.contentView viewWithTag:555];
    UIView *rightSep = [cell.contentView viewWithTag:556];
    if (!leftSep) {
        leftSep = [[UIView alloc] initWithFrame:CGRectZero];
        leftSep.backgroundColor = [UIColor lightTextColor];
        leftSep.tag = 555;
        [cell.contentView addSubview:leftSep];
    }
    if (!rightSep) {
        rightSep = [[UIView alloc] initWithFrame:CGRectZero];
        rightSep.backgroundColor = [UIColor lightTextColor];
        rightSep.tag = 556;
        [cell.contentView addSubview:rightSep];
    }
    leftSep.frame = CGRectMake(formInset, 0, 1, height);
    rightSep.frame = CGRectMake(formInset + width, 0, 1, height);
    
    UIView *tbSep = [cell.contentView viewWithTag:557];
    if (!tbSep) {
        tbSep = [[UIView alloc] initWithFrame:CGRectZero];
        tbSep.backgroundColor = [UIColor lightTextColor];
        tbSep.tag = 557;
        [cell.contentView addSubview:tbSep];
    }
    if (indexPath.section == 0 || indexPath.row == 0) {
        tbSep.frame = CGRectMake(formInset, 0, width, 1);
    } else if (indexPath.section == 3 || indexPath.row == [self datasInSection:indexPath.section].count - 1) {
        tbSep.frame = CGRectMake(formInset, height - 1, width, 1);
    } else {
        tbSep.frame = CGRectZero;
    }
    
    return cell;
}

- (NSString *)eventsText:(RoastData *)data {
    NSMutableArray *events = [NSMutableArray array];
    if (data.event) {
        [events addObject:[data eventName]];
    }
    if ([fireChangedData containsObject:data]) {
        [events addObject:NSLocalizedString(@"event_change_fire", nil)];
    }
    if (data.manualCool) {
        [events addObject:NSLocalizedString(@"event_cool_set", nil)];
    }
    if ([data isEqualToObject:completeData]) {
        [events addObject:NSLocalizedString(@"event_cool_end", nil)];
    }
    return [events componentsJoinedByString:@" "];
}

- (IBAction)shareProfile:(id)sender {
    [self saveProfile];
}

@end
