//
//  WebClient.m
//  petkeeper
//
//  Created by ttonway on 15/1/4.
//  Copyright (c) 2015年 com.wcare.apple.petkeeper. All rights reserved.
//

#import "WebClient.h"

NSString * const PROFILE_WEB_URL = @"http://beta.wcare.cn:3003/profile/%d/chart";
NSString * const CUPPING_WEB_URL = @"http://beta.wcare.cn:3003/cupping/%d/chart";

@implementation WebClient

+ (WebClient *)sharedInstance
{
    static  WebClient *sharedInstance = nil ;
    static  dispatch_once_t onceToken;
    dispatch_once (& onceToken, ^ {
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

- (instancetype)init
{
    if (self = [super init]) {
        
        _hostURL = [NSURL URLWithString:WEB_HOST];
        
        NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
        _sessionManager = [[AFURLSessionManager alloc] initWithSessionConfiguration:configuration];
    }
    return self;
}

- (NSURLRequest *)requestWithCupping:(Cupping *)cupping {
    NSMutableDictionary *dic = [NSMutableDictionary dictionary];
    [dic setObject:cupping.uuid forKey:@"uuid"];
    if (cupping.name) {
        [dic setObject:cupping.name forKey:@"name"];
    }
    if (cupping.comment) {
        [dic setObject:cupping.comment forKey:@"comment"];
    }
    [dic setObject:[NSNumber numberWithLongLong:[cupping.time timeIntervalSince1970] * 1000] forKey:@"time"];
    if (cupping.profile) {
        [dic setObject:cupping.profile.uuid forKey:@"profile"];
    }
    [dic setObject:[NSNumber numberWithFloat:cupping.score1] forKey:@"score1"];
    [dic setObject:[NSNumber numberWithFloat:cupping.score2] forKey:@"score2"];
    [dic setObject:[NSNumber numberWithFloat:cupping.score3] forKey:@"score3"];
    [dic setObject:[NSNumber numberWithFloat:cupping.score4] forKey:@"score4"];
    [dic setObject:[NSNumber numberWithFloat:cupping.score5] forKey:@"score5"];
    [dic setObject:[NSNumber numberWithFloat:cupping.score6] forKey:@"score6"];
    [dic setObject:[NSNumber numberWithFloat:cupping.score7] forKey:@"score7"];
    [dic setObject:[NSNumber numberWithFloat:cupping.score8] forKey:@"score8"];
    [dic setObject:[NSNumber numberWithFloat:cupping.score9] forKey:@"score9"];
    [dic setObject:[NSNumber numberWithFloat:cupping.score10] forKey:@"score10"];
    
    NSURL *URL = [NSURL URLWithString:@"cupping/upload" relativeToURL:self.hostURL];
    return [[AFJSONRequestSerializer serializer] requestWithMethod:@"POST" URLString:URL.absoluteString parameters:dic error:nil];;
}

- (NSURLRequest *)requestWithProfile:(RoastProfile *)profile {
    NSMutableDictionary *dic = [NSMutableDictionary dictionary];
    [dic setObject:profile.uuid forKey:@"uuid"];
    if (profile.deviceId) {
        [dic setObject:profile.deviceId forKey:@"deviceId"];
    }
    if (profile.people) {
        [dic setObject:profile.people forKey:@"people"];
    }
    if (profile.beanCountry) {
        [dic setObject:profile.beanCountry forKey:@"beanCountry"];
    }
    if (profile.beanName) {
        [dic setObject:profile.beanName forKey:@"beanName"];
    }
    [dic setObject:[NSNumber numberWithLongLong:[profile.startTime timeIntervalSince1970] * 1000] forKey:@"startTime"];
    [dic setObject:[NSNumber numberWithLongLong:[profile.endTime timeIntervalSince1970] * 1000] forKey:@"endTime"];
    [dic setObject:[NSNumber numberWithInteger:profile.startWeight] forKey:@"startWeight"];
    [dic setObject:[NSNumber numberWithInteger:profile.endWeight] forKey:@"endWeight"];
    [dic setObject:[NSNumber numberWithInteger:profile.envTemperature] forKey:@"envTemperature"];
    [dic setObject:[NSNumber numberWithInteger:profile.startFire] forKey:@"startFire"];
    [dic setObject:[NSNumber numberWithInteger:profile.startDruation] forKey:@"startDruation"];
    [dic setObject:[NSNumber numberWithInteger:profile.coolTemperature] forKey:@"coolTemperature"];
    [dic setObject:[NSNumber numberWithInteger:profile.preHeatTime] forKey:@"preHeatTime"];
    [dic setObject:[NSNumber numberWithInteger:profile.roastTime] forKey:@"roastTime"];
    [dic setObject:[NSNumber numberWithInteger:profile.coolTime] forKey:@"coolTime"];
    [dic setObject:[NSNumber numberWithBool:profile.complete] forKey:@"complete"];
    
    NSMutableArray *plotDatas = [NSMutableArray arrayWithCapacity:profile.plotDatas.count];
    for (RoastData *data in profile.plotDatas) {
        NSMutableDictionary *dic1 = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                     [NSNumber numberWithInteger:data.time], @"time",
                                     [NSNumber numberWithInteger:data.fire], @"fire",
                                     [NSNumber numberWithInteger:data.temperature], @"temperature",
                                     [NSNumber numberWithInteger:data.status], @"status",
                                     [NSNumber numberWithBool:data.manualCool], @"manualCool",
                                     nil];
        if (data.event) {
            [dic1 setObject:data.event forKey:@"event"];
        }
        [plotDatas addObject:dic1];
    }
    [dic setObject:plotDatas forKey:@"plotDatas"];
    
    NSURL *URL = [NSURL URLWithString:@"profile/upload" relativeToURL:self.hostURL];
    return [[AFJSONRequestSerializer serializer] requestWithMethod:@"POST" URLString:URL.absoluteString parameters:dic error:nil];;
}

- (void)uploadProfile:(RoastProfile *)profile {
    NSURLRequest *request = [self requestWithProfile:profile];
    NSURLSessionDataTask *dataTask = [self.sessionManager dataTaskWithRequest:request completionHandler:^(NSURLResponse *response, id responseObject, NSError *error) {
        if (error) {
            NSLog(@"uploadProfile fail. %@", error);
        } else {
            //NSLog(@"%@ %@", response, responseObject);
            if ([responseObject isKindOfClass:[NSDictionary class]]) {
                NSNumber *num = [responseObject objectForKey:@"sid"];
                NSInteger sid = [num integerValue];
                
                // TODO: this may conflict with user change
                RLMRealm *realm = [RLMRealm defaultRealm];
                [realm transactionWithBlock:^{
                    profile.sid = sid;
                    profile.dirty = NO;
                }];
            }
        }
    }];
    [dataTask resume];
}

- (void)uploadCupping:(Cupping *)cupping {
    NSURLRequest *request = [self requestWithCupping:cupping];
    NSURLSessionDataTask *dataTask = [self.sessionManager dataTaskWithRequest:request completionHandler:^(NSURLResponse *response, id responseObject, NSError *error) {
        if (error) {
            NSLog(@"uploadCupping fail. %@", error);
        } else {
            //NSLog(@"%@ %@", response, responseObject);
            if ([responseObject isKindOfClass:[NSDictionary class]]) {
                NSNumber *num = [responseObject objectForKey:@"sid"];
                NSInteger sid = [num integerValue];
                
                // TODO: this may conflict with user change
                RLMRealm *realm = [RLMRealm defaultRealm];
                [realm transactionWithBlock:^{
                    cupping.sid = sid;
                    cupping.dirty = NO;
                }];
            }
        }
    }];
    [dataTask resume];
}

@end
