//
//  LogoBackgroundView.m
//  GoCoRo
//
//  Created by ttonway on 2017/5/9.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "LogoBackgroundView.h"

@implementation LogoBackgroundView

- (instancetype)init {
    self = [super init];
    if (self) {
        self.logoLabel = [[UILabel alloc] initWithFrame:CGRectZero];
        self.logoLabel.font = [UIFont fontWithName:@"Bauhaus 93" size:24];
        self.logoLabel.textColor = [UIColor colorWithRed:123.0f/255.0f green:123.0f/255.0f blue:123.0f/255.0f alpha:1.0f];
        self.logoLabel.text = @"GoCoRo";
        [self.logoLabel sizeToFit];
        
        [self addSubview:self.logoLabel];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    CGRect bounds = self.bounds;
    CGRect rect = self.logoLabel.frame;
    rect.origin.x = bounds.size.width - rect.size.width - 16;
    rect.origin.y = bounds.size.height - rect.size.height - 16;
    self.logoLabel.frame = rect;
}

@end
