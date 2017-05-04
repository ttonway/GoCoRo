//
//  WebClient.m
//  petkeeper
//
//  Created by ttonway on 15/1/4.
//  Copyright (c) 2015年 com.wcare.apple.petkeeper. All rights reserved.
//

#import "WebClient.h"

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

@end
