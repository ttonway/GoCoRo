//
//  CuppingViewController.h
//  GoCoRo
//
//  Created by ttonway on 2017/5/2.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "GoCoRo-Swift.h"
#import <Charts/Charts-swift.h>

#import "Cupping.h"

@interface CuppingViewController : UITableViewController

@property (nonatomic) IBOutlet UITableViewCell *cell1;
@property (nonatomic) IBOutlet UITableViewCell *cell2;
@property (nonatomic) IBOutlet UITableViewCell *cell3;
@property (nonatomic) IBOutlet UITableViewCell *cell4;
@property (nonatomic) IBOutlet UITableViewCell *cell5;
@property (nonatomic) IBOutlet UITableViewCell *cell6;

@property (nonatomic) IBOutlet UITableViewCell *scell1;
@property (nonatomic) IBOutlet UITableViewCell *scell2;
@property (nonatomic) IBOutlet UITableViewCell *scell3;
@property (nonatomic) IBOutlet UITableViewCell *scell4;
@property (nonatomic) IBOutlet UITableViewCell *scell5;
@property (nonatomic) IBOutlet UITableViewCell *scell6;
@property (nonatomic) IBOutlet UITableViewCell *scell7;

@property (nonatomic) IBOutlet UILabel *nameLabel;
@property (nonatomic) IBOutlet UILabel *profileLabel;
@property (nonatomic) IBOutlet UILabel *totalLabel;
@property (nonatomic) IBOutlet UILabel *timeLabel;
@property (nonatomic) IBOutlet UILabel *commentLabel;
@property (nonatomic) IBOutlet UITextField *nameInput;
@property (nonatomic) IBOutlet UITextField *profileInput;
@property (nonatomic) IBOutlet UILabel *totalInput;
@property (nonatomic) IBOutlet UITextField *timeInput;
@property (nonatomic) IBOutlet UITextView *commentInput;
@property (nonatomic) IBOutlet RadarChartView *chartView;

@property (nonatomic) Cupping *cupping;
@property (nonatomic) RoastProfile *selectProfile;
@property (nonatomic) BOOL editMode;

@end
