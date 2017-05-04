//
//  ViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/24.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "ViewController.h"

#import "MyNavigationController.h"
#import "HomeViewController.h"
#import "ProfileListViewController.h"
#import "CuppingListViewController.h"
#import "MoreViewController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tabBar.translucent = NO;
    
    HomeViewController *controller1 = [[HomeViewController alloc] init];
    ProfileListViewController *controller2 = [[ProfileListViewController alloc] init];
    controller2.profiles = [[RoastProfile objectsWhere:@"startTime != nil"] sortedResultsUsingKeyPath:@"startTime" ascending:NO];
    CuppingListViewController *controller3 = [[CuppingListViewController alloc] init];
    controller3.cuppings = [[Cupping allObjects] sortedResultsUsingKeyPath:@"time" ascending:NO];
    MoreViewController *controller4 = [[MoreViewController alloc] init];
    
    controller1.tabBarItem.title = NSLocalizedString(@"tab_home", nil);
    controller1.tabBarItem.image = [UIImage imageNamed:@"ic_coffee_bean"];
    controller2.tabBarItem.title = NSLocalizedString(@"tab_roast_history", nil);
    controller2.tabBarItem.image = [UIImage imageNamed:@"ic_record"];
    controller3.tabBarItem.title = NSLocalizedString(@"tab_cupping_history", nil);
    controller3.tabBarItem.image = [UIImage imageNamed:@"ic_cup"];
    controller4.tabBarItem.title = NSLocalizedString(@"tab_more", nil);
    controller4.tabBarItem.image = [UIImage imageNamed:@"ic_more"];
    
    UINavigationController *nav1 = [[MyNavigationController alloc] initWithRootViewController:controller1];
    UINavigationController *nav2 = [[MyNavigationController alloc] initWithRootViewController:controller2];
    UINavigationController *nav3 = [[MyNavigationController alloc] initWithRootViewController:controller3];
    UINavigationController *nav4 = [[MyNavigationController alloc] initWithRootViewController:controller4];
    
    self.viewControllers = [NSArray arrayWithObjects:nav1, nav2, nav3, nav4, nil];
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (BOOL)shouldAutorotate {
    return [self.selectedViewController shouldAutorotate];
}
- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return [self.selectedViewController supportedInterfaceOrientations];
}
- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return [self.selectedViewController preferredInterfaceOrientationForPresentation];
}

@end
