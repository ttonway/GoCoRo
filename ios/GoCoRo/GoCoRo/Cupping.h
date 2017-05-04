//
//  Cupping.h
//  GoCoRo
//
//  Created by ttonway on 2017/4/26.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <Realm/Realm.h>

#import "RoastProfile.h"

@interface Cupping : RLMObject

@property NSString *uuid;

@property NSString *name;
@property NSString *comment;
@property NSDate *time;

@property RoastProfile *profile;

@property float score1;
@property float score2;
@property float score3;
@property float score4;
@property float score5;
@property float score6;
@property float score7;
@property float score8;
@property float score9;
@property float score10;

// sync with server
@property BOOL dirty;
@property NSInteger sid;

- (float)totalScore;

@end
