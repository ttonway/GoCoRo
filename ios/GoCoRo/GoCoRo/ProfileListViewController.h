//
//  ProfileListViewController.h
//  GoCoRo
//
//  Created by ttonway on 2017/4/24.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "RoastProfile.h"

@protocol ProfilePickerDelegate;

@interface ProfileListViewController : UITableViewController

@property (nonatomic) RLMResults<RoastProfile *> *profiles;

@property (nonatomic, assign) id<ProfilePickerDelegate> pickerDelegate;

@end

@protocol ProfilePickerDelegate <NSObject>

- (void)pickerController:(ProfileListViewController *)controller didPickProfile:(RoastProfile *)profile;

@end
