//
//  StartRoastViewController.h
//  GoCoRo
//
//  Created by ttonway on 2017/4/26.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface StartRoastViewController : UITableViewController

@property (nonatomic) IBOutlet UITableViewCell *cell1;
@property (nonatomic) IBOutlet UITableViewCell *cell2;
@property (nonatomic) IBOutlet UITableViewCell *cell3;
@property (nonatomic) IBOutlet UITableViewCell *cell4;
@property (nonatomic) IBOutlet UITableViewCell *cell5;
@property (nonatomic) IBOutlet UITableViewCell *cell6;

@property (nonatomic) IBOutlet UILabel *countryLabel;
@property (nonatomic) IBOutlet UILabel *beanLabel;
@property (nonatomic) IBOutlet UILabel *peopleLabel;
@property (nonatomic) IBOutlet UILabel *weightLabel;
@property (nonatomic) IBOutlet UILabel *tempLabel;
@property (nonatomic) IBOutlet UILabel *cooltempLabel;

@property (nonatomic) IBOutlet UITextField *countryInput;
@property (nonatomic) IBOutlet UITextField *beanInput;
@property (nonatomic) IBOutlet UITextField *peopleInput;
@property (nonatomic) IBOutlet UITextField *weightInput;
@property (nonatomic) IBOutlet UITextField *tempInput;
@property (nonatomic) IBOutlet UITextField *cooltempInput;

@property (nonatomic) IBOutlet UIButton *roastBtn;

@end
