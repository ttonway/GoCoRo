//
//  PlotViewController.h
//  GoCoRo
//
//  Created by ttonway on 2017/5/7.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "RoastProfile.h"

@interface PlotViewController : UIViewController

@property (nonatomic) RoastProfile *profile;
@property (nonatomic) BOOL roast;

- (void)restoreRoast;

@end
