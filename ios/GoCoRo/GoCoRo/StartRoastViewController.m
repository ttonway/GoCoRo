//
//  StartRoastViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/26.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "StartRoastViewController.h"

#import "Constants.h"
#import "RoastProfile.h"
#import "PlotViewController.h"


@interface PickerViewController : UIViewController
@property (nonatomic) UIPickerView *pickerView;
@end
@implementation PickerViewController
- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.pickerView.frame = self.view.bounds;
    [self.view addSubview:self.pickerView];
}
- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];
    
    self.pickerView.frame = self.view.bounds;
}
@end



@interface StartRoastViewController () <UITextFieldDelegate, UIPopoverPresentationControllerDelegate, UIPickerViewDataSource, UIPickerViewDelegate> {
    NSArray *cellArray;
    
    NSArray *inputArray;
    CGFloat oldContentOffsetY;
    
    PickerViewController *picker;
    UIPickerView *pickerView;
    NSArray *coolTempArray;
    
    NSDateFormatter *dateFormatter;
    RLMResults<RoastProfile *> *profiles;
    RoastProfile *selectedProfile;
    RLMNotificationToken *notificationToken;
}

@end

@implementation StartRoastViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"activity_start_roast", nil);
    
    self.countryLabel.text = NSLocalizedString(@"label_country", nil);
    self.beanLabel.text = NSLocalizedString(@"label_bean", nil);
    self.peopleLabel.text = NSLocalizedString(@"label_people", nil);
    self.weightLabel.text = NSLocalizedString(@"label_begin_weight", nil);
    self.tempLabel.text = NSLocalizedString(@"label_temperature", nil);
    self.cooltempLabel.text = NSLocalizedString(@"label_cool_temperature", nil);
    
    [self.roastBtn setTitle:NSLocalizedString(@"btn_start_roast", nil) forState:UIControlStateNormal];
    self.roastBtn.backgroundColor = [UIColor customOrangeColor];
    self.roastBtn.layer.cornerRadius = 18;
    self.roastBtn.layer.masksToBounds = YES;
    [self.roastBtn addTarget:self action:@selector(startRoast:) forControlEvents:UIControlEventTouchUpInside];
    
    self.cooltempInput.text = @"70";
    self.cooltempInput.delegate = self;
    inputArray = @[self.countryInput, self.beanInput, self.peopleInput, self.weightInput, self.tempInput];
    
    // tableView
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    UIView *footer = [[UIView alloc] initWithFrame:CGRectZero];
    footer.backgroundColor = [UIColor colorWithRed:19.0f/255.0f green:18.0f/255.0f blue:26.0f/255.0f alpha:1.0f];
    self.tableView.tableFooterView = footer;
    
    // section1
    cellArray = [NSMutableArray arrayWithObjects:self.cell1, self.cell2, self.cell3, self.cell4, self.cell5, self.cell6, nil];
    
    // section2
    dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = NSDateFormatterMediumStyle;
    dateFormatter.timeStyle = NSDateFormatterShortStyle;
    
    profiles = [[RoastProfile objectsWhere:@"startTime != nil"] sortedResultsUsingKeyPath:@"startTime" ascending:NO];
    __weak typeof(self) weakSelf = self;
    notificationToken = [profiles addNotificationBlock:^(RLMResults<RoastProfile *> *results, RLMCollectionChange *changes, NSError *error) {
        if (error) {
            NSLog(@"Failed to open Realm on background worker: %@", error);
            return;
        }
        
        UITableView *tableView = weakSelf.tableView;
        // Initial run of the query will pass nil for the change information
        if (!changes) {
            [tableView reloadData];
            return;
        }
        
        // Query results have changed, so apply them to the UITableView
        [tableView beginUpdates];
        [tableView deleteRowsAtIndexPaths:[changes deletionsInSection:0]
                         withRowAnimation:UITableViewRowAnimationAutomatic];
        [tableView insertRowsAtIndexPaths:[changes insertionsInSection:0]
                         withRowAnimation:UITableViewRowAnimationAutomatic];
        [tableView reloadRowsAtIndexPaths:[changes modificationsInSection:0]
                         withRowAnimation:UITableViewRowAnimationAutomatic];
        [tableView endUpdates];
    }];
}

- (IBAction)startRoast:(id)sender {
    RoastProfile *profile = [[RoastProfile alloc] init];
    profile.uuid = [Utils getUUID];
    profile.people = self.peopleInput.text;
    profile.beanCountry = self.countryInput.text;
    profile.beanName  = self.beanInput.text;
    profile.startWeight = [self.weightInput.text integerValue];
    profile.envTemperature = [self.tempInput.text integerValue];
    profile.coolTemperature = [self.cooltempInput.text integerValue];
    
    profile.referenceProfile = selectedProfile;
    
    profile.dirty = YES;
    
    RLMRealm *realm = [RLMRealm defaultRealm];
    [realm beginWriteTransaction];
    [realm addObject:profile];
    [realm commitWriteTransaction];
    
    
    PlotViewController *controller = [[PlotViewController alloc] init];
    controller.hidesBottomBarWhenPushed = YES;
    controller.profile = profile;
    controller.roast = YES;
    NSMutableArray *controllers = [NSMutableArray arrayWithArray:self.navigationController.viewControllers];
    [controllers removeObject:self];
    [controllers addObject:controller];
    [self.navigationController setViewControllers:controllers animated:YES];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - UITextFieldDelegate
- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField {
    if (textField == self.cooltempInput) {
        if (!pickerView) {
            coolTempArray = @[@40, @50, @60, @70, @80];
            
            pickerView = [[UIPickerView alloc] initWithFrame:CGRectZero];
            pickerView.dataSource = self;
            pickerView.delegate = self;
        }
        
        picker = [[PickerViewController alloc] init];
        picker.pickerView = pickerView;
        picker.modalPresentationStyle = UIModalPresentationPopover;
        picker.popoverPresentationController.sourceView = textField;
        picker.popoverPresentationController.sourceRect = textField.bounds;
        picker.popoverPresentationController.permittedArrowDirections = UIPopoverArrowDirectionUp;
        picker.popoverPresentationController.delegate = self;
        picker.preferredContentSize = CGSizeMake(100, 120);
        [self presentViewController:picker animated:YES completion:nil];
        
        return NO;
    }
    return YES;
}

- (UIModalPresentationStyle)adaptivePresentationStyleForPresentationController:(UIPresentationController *)controller {
    return UIModalPresentationNone;
}

- (void)popoverPresentationControllerDidDismissPopover:(UIPopoverPresentationController *)popoverPresentationController {
    if (popoverPresentationController.presentedViewController == picker) {
        NSInteger row = [pickerView selectedRowInComponent:0];
        if (row != -1) {
            NSNumber *num = coolTempArray[row];
            self.cooltempInput.text = [NSString stringWithFormat:@"%@", num];
        }
    }
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    if (scrollView.contentOffset.y < oldContentOffsetY - 16) {
        for (UIView *input in inputArray) {
            if (input.isFirstResponder) {
                [input resignFirstResponder];
                break;
            }
        }
    }
    oldContentOffsetY = scrollView.contentOffset.y;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 0) {
        return cellArray.count;
    } else if (section == 1) {
        return profiles.count;
    }
    return 0;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 0) {
        UITableViewCell *cell = [cellArray objectAtIndex:indexPath.row];
        return cell == self.cell6 ? 60 : 44;
    }
    return 60;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 0) {
        return [cellArray objectAtIndex:indexPath.row];
    } else {
        static NSString *cellIdentifier = @"ProfileIdentifier";
        UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
        
        UIView *indicator;
        UIView *seperator;
        UILabel *timeLabel;
        if (!cell) {
            cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellIdentifier];
            cell.backgroundColor = self.tableView.tableFooterView.backgroundColor;
            cell.textLabel.font = [UIFont boldSystemFontOfSize:18];
            cell.textLabel.textColor = [UIColor lightTextColor];
            cell.detailTextLabel.font = [UIFont systemFontOfSize:15];
            cell.detailTextLabel.textColor = [UIColor lightTextColor];
            
            CGFloat width = CGRectGetWidth(tableView.bounds);
            timeLabel = [[UILabel alloc] initWithFrame:CGRectMake(width - 8 - 200, 30, 200, 21)];
            timeLabel.font = [UIFont systemFontOfSize:15];
            timeLabel.textColor = [UIColor lightTextColor];
            timeLabel.textAlignment = NSTextAlignmentRight;
            timeLabel.tag = 5;
            [cell.contentView addSubview:timeLabel];
            
            indicator = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 8, 60)];
            indicator.backgroundColor = [UIColor colorWithRed:255.0f/255.0f green:74.0f/255.0f blue:73.0f/255.0f alpha:1.0f];
            indicator.tag = 6;
            [cell.contentView addSubview:indicator];
            
            seperator = [[UIView alloc] initWithFrame:CGRectMake(8, 59.5f, width - 8, 0.5f)];
            seperator.backgroundColor = tableView.separatorColor;
            seperator.tag = 7;
            [cell.contentView addSubview:seperator];
        } else {
            timeLabel = [cell.contentView viewWithTag:5];
            indicator = [cell.contentView viewWithTag:6];
            seperator = [cell.contentView viewWithTag:7];
        }
        
        RoastProfile *profile = [profiles objectAtIndex:indexPath.row];
        cell.textLabel.text = [profile fullName];
        cell.detailTextLabel.text = [NSString stringWithFormat:@"%ldg", (long)profile.startWeight];
        timeLabel.text = [dateFormatter stringFromDate:profile.startTime];
        indicator.hidden = ![profile isEqualToObject:selectedProfile];
        
        return cell;
    }
}

- (BOOL)tableView:(UITableView *)tableView shouldHighlightRowAtIndexPath:(NSIndexPath *)indexPath {
    return indexPath.section == 1;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    RoastProfile *profile = [profiles objectAtIndex:indexPath.row];
    if ([profile isEqualToObject:selectedProfile]) {
        selectedProfile = nil;
    } else {
        selectedProfile = profile;
        
        self.countryInput.text = profile.beanCountry;
        self.beanInput.text = profile.beanName;
        self.peopleInput.text = profile.people;
        self.weightInput.text = [NSString stringWithFormat:@"%ld", (long)profile.startWeight];
        self.tempInput.text = [NSString stringWithFormat:@"%ld", (long)profile.envTemperature];
        self.cooltempInput.text = [NSString stringWithFormat:@"%ld", (long)profile.coolTemperature];
    }
    [tableView reloadData];
}

#pragma mark - Picker view data source
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView*)pickerView {
    return 1;
}
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return coolTempArray.count;
}
- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    NSNumber *num = coolTempArray[row];
    return [NSString stringWithFormat:@"%@℃", num];
}

@end
