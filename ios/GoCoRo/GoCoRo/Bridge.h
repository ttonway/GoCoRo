//
//  Bridge.h
//  GoCoRo
//
//  Created by ttonway on 2017/5/11.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Bridge : NSObject

+ (NSString *)formatScore:(double)score;
+ (NSString *)formatPlotData:(NSDictionary *)dic;

@end
