//
//  RoastProfile.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/26.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "RoastProfile.h"


NSString * const EVENT_BURST1_START = @"BURST1_START";
NSString * const EVENT_BURST1 = @"BURST1";
NSString * const EVENT_BURST2_START = @"BURST2_START";
NSString * const EVENT_BURST2 = @"BURST2";

@implementation RoastData

- (NSString *)eventName {
    if ([EVENT_BURST1_START isEqualToString:self.event]) {
        return NSLocalizedString(@"event_burst1_start", nil);
    } else if ([EVENT_BURST1 isEqualToString:self.event]) {
        return NSLocalizedString(@"event_burst1", nil);
    } else if ([EVENT_BURST2_START isEqualToString:self.event]) {
        return NSLocalizedString(@"event_burst2_start", nil);
    } else if ([EVENT_BURST2 isEqualToString:self.event]) {
        return NSLocalizedString(@"event_burst2", nil);
    }
    return nil;
}

@end

@implementation RoastProfile

+ (NSString *)primaryKey {
    return @"uuid";
}

- (NSString *)fullName {
    NSMutableString *str = [[NSMutableString alloc] init];
    if (self.beanCountry) {
        [str appendString:self.beanCountry];
        [str appendString:@"-"];
    }
    [str appendString:self.beanName];
    if (self.people) {
        [str appendFormat:@" (%@)", self.people];
    }
    return str;
}

@end
