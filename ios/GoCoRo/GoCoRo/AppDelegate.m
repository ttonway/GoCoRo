//
//  AppDelegate.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/24.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "AppDelegate.h"

#import <SVProgressHUD/SVProgressHUD.h>

#import "Constants.h"
#import "RoastProfile.h"

@interface AppDelegate ()

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    UIColor *barColor = [UIColor colorWithRed:41.0f/255.0f green:41.0f/255.0f blue:41.0f/255.0f alpha:1.0f];
    [[UITabBar appearance] setTintColor:[UIColor customOrangeColor]];
    [[UITabBar appearance] setBarTintColor:barColor];
    
    [[UINavigationBar appearance] setBarStyle:UIBarStyleBlack];
    //[[UINavigationBar appearance] setTranslucent:NO];
    [[UINavigationBar appearance] setBarTintColor:barColor];
    [[UINavigationBar appearance] setTintColor:[UIColor customOrangeColor]];
    [[UINavigationBar appearance] setTitleTextAttributes:@{NSForegroundColorAttributeName : [UIColor customOrangeColor]}];
    
    //[SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
    [SVProgressHUD setMinimumDismissTimeInterval:3.0];
    
    
    [self initMockData];
    
    return YES;
}

- (void)initMockData {
    RLMRealm *realm = [RLMRealm defaultRealm];
    
    RoastProfile *profile = [[RoastProfile objectsWhere:@"uuid == %@", @"test66"] firstObject];
    if (!profile) {
        profile = [[RoastProfile alloc] init];
        profile.uuid = @"test66";
        profile.people = @"55555";
        profile.beanCountry = @"巴拿马";
        profile.beanName  = @"蓝山";
        profile.startTime = [NSDate date];
        profile.endTime = [NSDate dateWithTimeIntervalSinceNow:30];
        profile.startWeight = 500;
        profile.endWeight = 50;
        profile.envTemperature = 27;
        
        profile.startFire = 3;
        profile.startDruation = 30 * 60;
        profile.coolTemperature = 80;
        
        profile.preHeatTime = 0;
        profile.roastTime = 3 * 60;
        profile.coolTime = 15 * 60;
        
        for (int i = 0; i < 30 * 60; i++) {
            RoastData *data = [[RoastData alloc] init];
            data.time = i;
            if (i < 3 * 60) {
                data.status = StatusPreheating;
            } else if (i < 15 * 60) {
                data.status = StatusRoasting;
                data.fire = i < 10 * 60 ? 3 : 5;
            } else {
                data.status = StatusCooling;
            }
            
            if (i == 5 * 60) {
                data.event = EVENT_BURST1_START;
            } else if (i == 10 * 60) {
                data.event = EVENT_BURST1;
            } else if (i == 15 * 60) {
                data.event = EVENT_BURST2_START;
            } else if (i == 20 * 60) {
                data.event = EVENT_BURST2;
            }
            
            data.temperature = (-230.f / 900 / 900 * i * i + 230.f * 2 / 900 * i + 0.5f);
            
            [profile.plotDatas addObject:data];
        }
        
        [realm beginWriteTransaction];
        [realm addObject:profile];
        [realm commitWriteTransaction];
    }
}


- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}


@end
