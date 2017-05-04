//
//  ProfileListViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/24.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "ProfileListViewController.h"

#import "Constants.h"
#import "CuppingListViewController.h"
#import "PlotViewController.h"


@interface ProfileListViewController () {
    NSDateFormatter *dateFormatter;

    RLMNotificationToken *notificationToken;
}

@end

@implementation ProfileListViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = self.pickerDelegate ? NSLocalizedString(@"activity_profile_picker", nil) : NSLocalizedString(@"activity_profile_list", nil);
    
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    self.tableView.rowHeight = 60;
    self.tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectZero];
    
    dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = NSDateFormatterMediumStyle;
    dateFormatter.timeStyle = NSDateFormatterShortStyle;
    
    
    __weak typeof(self) weakSelf = self;
    notificationToken = [self.profiles addNotificationBlock:^(RLMResults<RoastProfile *> *results, RLMCollectionChange *changes, NSError *error) {
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

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.profiles.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellIdentifier = @"ProfileIdentifier";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    
    UILabel *timeLabel;
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellIdentifier];
        cell.backgroundColor = [UIColor clearColor];
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
    } else {
        timeLabel = [cell.contentView viewWithTag:5];
    }
    
    RoastProfile *profile = [self.profiles objectAtIndex:indexPath.row];
    cell.textLabel.text = [profile fullName];
    cell.detailTextLabel.text = [NSString stringWithFormat:@"%ldg", (long)profile.startWeight];
    timeLabel.text = [dateFormatter stringFromDate:profile.startTime];
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    RoastProfile *profile = [self.profiles objectAtIndex:indexPath.row];
    
    if (self.pickerDelegate) {
        [self.pickerDelegate pickerController:self didPickProfile:profile];
    } else {
        BOOL roast = NO;
        //    RoastProfile profile = GoCoRoDevice.getInstance(context).getProfile();
        //    if (profile != null && TextUtils.equals(profileUuid, profile.getUuid())) {
        //        roast = true;
        //    }
        
        PlotViewController *controller = [[PlotViewController alloc] init];
        controller.hidesBottomBarWhenPushed = YES;
        controller.profile = profile;
        controller.roast = roast;
        [self.navigationController pushViewController:controller animated:YES];
    }
}


- (NSArray<UITableViewRowAction *> *)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (self.pickerDelegate) {
        return nil;
    }
    
    __weak typeof(self) weakSelf = self;
    UITableViewRowAction *editAction = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleNormal title:NSLocalizedString(@"btn_cupping", nil) handler:^(UITableViewRowAction *action, NSIndexPath *indexPath) {
        
        RoastProfile *profile = [self.profiles objectAtIndex:indexPath.row];
        CuppingListViewController *controller = [[CuppingListViewController alloc] init];
        controller.cuppings = [[Cupping objectsWhere:@"profile == %@", profile] sortedResultsUsingKeyPath:@"time" ascending:NO];
        controller.profileForCreate = profile;
        [weakSelf.navigationController pushViewController:controller animated:YES];
    }];
    editAction.backgroundColor = [UIColor colorWithRed:179.0f/255.0f green:179.0f/255.0f blue:19.0f/255.0f alpha:1.0f];
    
    UITableViewRowAction *deleteAction = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleNormal title:NSLocalizedString(@"btn_delete", nil)  handler:^(UITableViewRowAction *action, NSIndexPath *indexPath) {
        
        RLMRealm *realm = self.profiles.realm;
        [realm beginWriteTransaction];
        RoastProfile *profile = [self.profiles objectAtIndex:indexPath.row];
        [realm deleteObject:profile];
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
        NSError *error;
        [realm commitWriteTransactionWithoutNotifying:@[notificationToken] error:&error];
        if (error) {
            NSLog(@"commitWriteTransactionWithoutNotifying fail. %@", error);
        }
    }];
    deleteAction.backgroundColor = [UIColor colorWithRed:185.0f/255.0f green:61.0f/255.0f blue:48.0f/255.0f alpha:1.0f];
    
    return @[deleteAction,editAction];
}

@end
