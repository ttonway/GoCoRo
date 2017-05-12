//
//  Bridge.m
//  GoCoRo
//
//  Created by ttonway on 2017/5/11.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "Bridge.h"

#import "Constants.h"
#import "RoastProfile.h"

@implementation Bridge

+ (NSString *)formatScore:(double)score {
    return [NSString stringWithFormat:NSLocalizedString(@"label_score_x", nil), score];
}

+ (NSString *)formatPlotData:(NSDictionary *)dic {
    RoastProfile *profile = [dic objectForKey:@"profile"];
    RoastData *data = [dic objectForKey:@"data"];
    NSMutableString *str = [[NSMutableString alloc] initWithString:[Utils formatSeconds:[profile getTimeInStatus:data.time]]];
    [str appendString:@" "];
    [str appendFormat:@"%ld℃", (long)data.temperature];
    if (data.fire != 0) {
        [str appendString:@"-"];
        [str appendFormat:NSLocalizedString(@"label_fire_x", nil), data.fire];
    }
    if (data.event) {
        [str appendString:@"-"];
        [str appendString:[data eventName]];
    }
    return str;
}

@end
