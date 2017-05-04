//
//  CuppingListViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/24.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "CuppingListViewController.h"

#import "Constants.h"
#import "CuppingViewController.h"

@interface CuppingListViewController () {
    NSDateFormatter *dateFormatter;

    RLMNotificationToken *notificationToken;
}

@end

@implementation CuppingListViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"activity_cupping_list", nil);
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(createCupping:)];
    
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    self.tableView.rowHeight = 60;
    self.tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectZero];
    
    dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = NSDateFormatterMediumStyle;
    dateFormatter.timeStyle = NSDateFormatterShortStyle;
    
    __weak typeof(self) weakSelf = self;
    notificationToken = [self.cuppings addNotificationBlock:^(RLMResults<Cupping *> *results, RLMCollectionChange *changes, NSError *error) {
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

- (IBAction)createCupping:(id)sender {
    CuppingViewController *controller = [[CuppingViewController alloc] init];
    controller.hidesBottomBarWhenPushed = YES;
    controller.selectProfile = self.profileForCreate;
    [self.navigationController pushViewController:controller animated:YES];
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
    return self.cuppings.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellIdentifier = @"CuppingIdentifier";
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
    
    Cupping *cupping = [self.cuppings objectAtIndex:indexPath.row];
    cell.textLabel.text = cupping.name;
    cell.detailTextLabel.text = [NSString stringWithFormat:NSLocalizedString(@"x_score", nil), [cupping totalScore]];
    timeLabel.text = [dateFormatter stringFromDate:cupping.time];
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    Cupping *cupping = [self.cuppings objectAtIndex:indexPath.row];
    CuppingViewController *controller = [[CuppingViewController alloc] init];
    controller.hidesBottomBarWhenPushed = YES;
    controller.cupping = cupping;
    [self.navigationController pushViewController:controller animated:YES];
}

- (NSArray<UITableViewRowAction *> *)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    UITableViewRowAction *deleteAction = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleNormal title:NSLocalizedString(@"btn_delete", nil)  handler:^(UITableViewRowAction *action, NSIndexPath *indexPath) {
        
        RLMRealm *realm = self.cuppings.realm;
        [realm beginWriteTransaction];
        Cupping *cupping = [self.cuppings objectAtIndex:indexPath.row];
        [realm deleteObject:cupping];
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
        NSError *error;
        [realm commitWriteTransactionWithoutNotifying:@[notificationToken] error:&error];
        if (error) {
            NSLog(@"commitWriteTransactionWithoutNotifying fail. %@", error);
        }
    }];
    deleteAction.backgroundColor = [UIColor colorWithRed:185.0f/255.0f green:61.0f/255.0f blue:48.0f/255.0f alpha:1.0f];
    
    return @[deleteAction];
}

@end
