//
//  Cupping.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/26.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "Cupping.h"

@implementation Cupping

+ (NSString *)primaryKey {
    return @"uuid";
}

- (float)totalScore {
    return self.score1 + self.score2 + self.score3 + self.score4 + self.score5 + self.score6 + self.score7 + self.score8 + self.score9 + self.score10;
}

@end
