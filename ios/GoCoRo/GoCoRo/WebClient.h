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

@interface WebClient : NSObject

@property (nonatomic, readonly) AFURLSessionManager *sessionManager;

@property (nonatomic, readonly) NSURL *hostURL;

+ (WebClient *)sharedInstance;

@end
