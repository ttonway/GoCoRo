//
//  HomeViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/26.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "HomeViewController.h"

#import "Constants.h"
#import "RoastProfile.h"
#import "StartRoastViewController.h"

@interface HomeViewController () {
    NSArray *cellArray;
    
    NSDateFormatter *dateFormatter;
    RLMResults<RoastProfile *> *profiles;
}

@end

@implementation HomeViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = @"GoCoRo";
    
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    self.tableView.allowsSelection = NO;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    
    cellArray = [NSArray arrayWithObjects:self.cell1, self.cell2, self.cell3, self.cell4, nil];
    self.cell1.backgroundColor = [UIColor clearColor];
    self.cell3.backgroundColor = [UIColor clearColor];
    
    
    self.roastBtn.backgroundColor = [UIColor customOrangeColor];
    self.roastBtn.layer.cornerRadius = 18;
    self.roastBtn.layer.masksToBounds = YES;
    [self.roastBtn addTarget:self action:@selector(gotoStartRoast:) forControlEvents:UIControlEventTouchUpInside];
    
    self.cell3.textLabel.font = [UIFont systemFontOfSize:15];
    self.cell3.textLabel.textColor = [UIColor customOrangeColor];
    self.cell3.textLabel.text = NSLocalizedString(@"label_lasttime", nil);
    
    self.cell4.textLabel.textColor = [UIColor lightTextColor];
    self.cell4.detailTextLabel.textColor = [UIColor lightTextColor];
    
    dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = NSDateFormatterMediumStyle;
    dateFormatter.timeStyle = NSDateFormatterShortStyle;
    profiles = [[RoastProfile objectsWhere:@"startTime != nil"] sortedResultsUsingKeyPath:@"startTime" ascending:NO];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    RoastProfile *profile = profiles.firstObject;
    if (profile) {
        self.cell4.textLabel.text = [profile fullName];
        self.cell4.detailTextLabel.text = [dateFormatter stringFromDate:profile.startTime];
    } else {
        self.cell4.textLabel.text = NSLocalizedString(@"label_no_profile", nil);
        self.cell4.detailTextLabel.text = nil;
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)gotoStartRoast:(id)sender {
    StartRoastViewController *controller = [[StartRoastViewController alloc] init];
    [self.navigationController pushViewController:controller animated:YES];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return cellArray.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    static CGFloat rowHeights[] = {180.f, 100.f, 30.f, 60.f};
    CGFloat h = rowHeights[indexPath.row];
    if (indexPath.row < 2) {
        CGFloat total = rowHeights[0] + rowHeights[1] + rowHeights[2] + rowHeights[3];
        CGFloat height = CGRectGetHeight(tableView.bounds) - tableView.contentInset.top - tableView.contentInset.bottom;
        h += (height - total) / 2;
    }
    return h;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    return [cellArray objectAtIndex:indexPath.row];
}

@end
