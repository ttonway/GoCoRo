//
//  CuppingListViewController.h
//  GoCoRo
//
//  Created by ttonway on 2017/4/24.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "Cupping.h"

@interface CuppingListViewController : UITableViewController

@property (nonatomic) RLMResults<Cupping *> *cuppings;
@property (nonatomic) RoastProfile *profileForCreate;

@end
