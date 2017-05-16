//
//  WebClient.h
//  petkeeper
//
//  Created by ttonway on 15/1/4.
//  Copyright (c) 2015年 com.wcare.apple.petkeeper. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <AFNetworking/AFNetworking.h>
#import "Constants.h"
#import "RoastProfile.h"
#import "Cupping.h"


extern NSString * const PROFILE_WEB_URL;
extern NSString * const CUPPING_WEB_URL;

@interface WebClient : NSObject

@property (nonatomic, readonly) AFURLSessionManager *sessionManager;

@property (nonatomic, readonly) NSURL *hostURL;

+ (WebClient *)sharedInstance;


- (NSURLRequest *)requestWithCupping:(Cupping *)cupping;
- (void)uploadCupping:(Cupping *)cupping;

- (NSURLRequest *)requestWithProfile:(RoastProfile *)profile;
- (void)uploadProfile:(RoastProfile *)profile;

@end
