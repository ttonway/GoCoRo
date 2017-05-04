//
//  CuppingViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/5/2.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "CuppingViewController.h"
#import <SVProgressHUD/SVProgressHUD.h>

#import "Constants.h"
#import "ProfileListViewController.h"

@interface CuppingViewController () <ChartViewDelegate, IChartAxisValueFormatter, UITextViewDelegate, UITextFieldDelegate, ProfilePickerDelegate> {
    NSMutableArray *cellArray;
    NSArray *scoreCellArray;
    NSArray<UISlider *> *scoreSliderArray;
    NSArray<UILabel *> *scoreLabelArray;
    
    NSArray *inputArray;
    CGFloat commentHeight;
    CGFloat oldContentOffsetY;
    
    UIBarButtonItem *editItem;
    UIBarButtonItem *shareItem;
    
    NSDateFormatter *dateFormatter;
    
    NSArray<NSString *> *activities;
    RadarChartDataSet *radarDataSet;
}

@end


static const float CUPPING_SCORE_MIN = 6.f;
static const float CUPPING_SCORE_MAX = 10.f;

@implementation CuppingViewController
@synthesize editMode = _editMode;

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"activity_cupping", nil);
    shareItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ic_menu_share"] style:UIBarButtonItemStylePlain target:self action:@selector(shareCupping:)];
    editItem = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStyleDone target:self action:@selector(toggleEditMode:)];
    if (self.cupping) {
        self.navigationItem.rightBarButtonItems = @[editItem, shareItem];
    } else {
        self.navigationItem.rightBarButtonItems = @[editItem];
    }
    activities = @[NSLocalizedString(@"cupping_item1", nil),
                   NSLocalizedString(@"cupping_item2", nil),
                   NSLocalizedString(@"cupping_item3", nil),
                   NSLocalizedString(@"cupping_item4", nil),
                   NSLocalizedString(@"cupping_item5", nil),
                   NSLocalizedString(@"cupping_item6", nil),
                   NSLocalizedString(@"cupping_item7", nil)];
    
    // tableView
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    self.tableView.allowsSelection = NO;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    
    cellArray = [NSMutableArray arrayWithObjects:self.cell1, self.cell2, self.cell3, self.cell4, self.cell5, self.cell6, nil];
    scoreCellArray = [NSArray arrayWithObjects:self.scell1, self.scell2, self.scell3, self.scell4, self.scell5, self.scell6, self.scell7, nil];
    NSMutableArray *sliders = [NSMutableArray arrayWithCapacity:7];
    NSMutableArray *labels = [NSMutableArray arrayWithCapacity:7];
    NSInteger index = 0;
    for (UITableViewCell *scell in scoreCellArray) {
        UILabel *nameLabel = [scell.contentView viewWithTag:1];
        UISlider *slider = [scell.contentView viewWithTag:2];
        UILabel *valueLabel = [scell.contentView viewWithTag:3];
        
        slider.maximumValue = CUPPING_SCORE_MAX;
        slider.minimumValue = CUPPING_SCORE_MIN;
        [slider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventValueChanged];
        
        nameLabel.text = activities[index++];
        [sliders addObject:slider];
        [labels addObject:valueLabel];
    }
    scoreSliderArray = sliders;
    scoreLabelArray = labels;
    
    // data
    dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = NSDateFormatterMediumStyle;
    dateFormatter.timeStyle = NSDateFormatterShortStyle;
    if (self.cupping) {
        self.selectProfile = self.cupping.profile;
        
        self.editMode = NO;
    } else {
        self.cupping = [[Cupping alloc] init];
        self.cupping.uuid = [Utils getUUID];
        self.cupping.time = [NSDate date];
        self.cupping.score1 = 6.f;
        self.cupping.score2 = 6.f;
        self.cupping.score3 = 6.f;
        self.cupping.score4 = 6.f;
        self.cupping.score5 = 6.f;
        self.cupping.score6 = 6.f;
        self.cupping.score7 = 6.f;
        self.cupping.score8 = 10.f;
        self.cupping.score9 = 10.f;
        self.cupping.score10 = 10.f;
        
        self.editMode = YES;
    }
    
    self.nameLabel.text = NSLocalizedString(@"label_cupping_name", nil);
    self.profileLabel.text = NSLocalizedString(@"label_cupping_profile", nil);
    self.totalLabel.text = NSLocalizedString(@"label_total_score", nil);
    self.totalLabel.textColor = [UIColor customOrangeColor];
    self.totalInput.textColor = [UIColor customOrangeColor];
    self.timeLabel.text = NSLocalizedString(@"label_cupping_time", nil);
    self.timeInput.enabled = NO;
    self.commentLabel.text = NSLocalizedString(@"label_cupping_comment", nil);
    self.profileInput.delegate = self;
    self.commentInput.delegate = self;
    
    inputArray = @[self.nameInput, self.commentInput];
    self.nameInput.text = self.cupping.name;
    self.profileInput.text = [self.selectProfile fullName];
    self.totalInput.text = [NSString stringWithFormat:NSLocalizedString(@"label_score_x", nil), [self.cupping totalScore]];
    self.timeInput.text = [dateFormatter stringFromDate:self.cupping.time];
    self.commentInput.text = self.cupping.comment;
    
    scoreSliderArray[0].value = self.cupping.score1;
    scoreSliderArray[1].value = self.cupping.score2;
    scoreSliderArray[2].value = self.cupping.score3;
    scoreSliderArray[3].value = self.cupping.score4;
    scoreSliderArray[4].value = self.cupping.score5;
    scoreSliderArray[5].value = self.cupping.score6;
    scoreSliderArray[6].value = self.cupping.score7;
    scoreLabelArray[0].text = [NSString stringWithFormat:@"%.2f", self.cupping.score1];
    scoreLabelArray[1].text = [NSString stringWithFormat:@"%.2f", self.cupping.score2];
    scoreLabelArray[2].text = [NSString stringWithFormat:@"%.2f", self.cupping.score3];
    scoreLabelArray[3].text = [NSString stringWithFormat:@"%.2f", self.cupping.score4];
    scoreLabelArray[4].text = [NSString stringWithFormat:@"%.2f", self.cupping.score5];
    scoreLabelArray[5].text = [NSString stringWithFormat:@"%.2f", self.cupping.score6];
    scoreLabelArray[6].text = [NSString stringWithFormat:@"%.2f", self.cupping.score7];

    
    // chart
    [self setupChart];
    self.chartView.data = [self createRadarData];
}

- (void)setEditMode:(BOOL)editMode {
    _editMode = editMode;
    
    self.nameInput.enabled = editMode;
    self.profileInput.enabled = editMode;
    self.commentInput.editable = editMode;
    if (editMode) {
        editItem.title = NSLocalizedString(@"action_done", nil);
        
        [cellArray removeObjectsInArray:scoreCellArray];
        [cellArray addObjectsFromArray:scoreCellArray];
    } else {
        editItem.title = NSLocalizedString(@"action_edit", nil);
        
        [cellArray removeObjectsInArray:scoreCellArray];
    }
    [self.tableView reloadData];
}

- (float)getScore:(UISlider *)slider {
    float score = slider.value;
    int m = (int) (score / 0.25f + 0.5f);
    score = m * 0.25f;
    return score;
}

- (IBAction)sliderValueChanged:(id)sender {
    NSUInteger index = [scoreSliderArray indexOfObject:sender];
    if (index != NSNotFound) {
        UILabel *valueLabel = [scoreLabelArray objectAtIndex:index];
        valueLabel.text = [NSString stringWithFormat:@"%.2f", [self getScore:sender]];
    }
    
    [self updateRadarChart];
}

#pragma mark - UITextFieldDelegate
- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField {
    if (textField == self.profileInput) {
        ProfileListViewController *controller = [[ProfileListViewController alloc] init];
        controller.profiles = [[RoastProfile objectsWhere:@"startTime != nil"] sortedResultsUsingKeyPath:@"startTime" ascending:NO];
        controller.pickerDelegate = self;
        [self.navigationController pushViewController:controller animated:YES];
        return NO;
    }
    return YES;
}
- (void)pickerController:(ProfileListViewController *)controller didPickProfile:(RoastProfile *)profile {
    self.selectProfile = profile;
    self.profileInput.text = [profile fullName];
    [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - UITextViewDelegate
- (void)textViewDidChange:(UITextView *)textView {
    CGRect frame = textView.frame;
    CGSize constraintSize = CGSizeMake(frame.size.width, MAXFLOAT);
    CGSize size = [textView sizeThatFits:constraintSize];
    if (commentHeight != size.height) {
        commentHeight = size.height;
        [self.tableView beginUpdates];
        [self.tableView endUpdates];
    }
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    if (scrollView.contentOffset.y < oldContentOffsetY - 16) {
        for (UIView *input in inputArray) {
            if (input.isFirstResponder) {
                [input resignFirstResponder];
                break;
            }
        }
    }
    oldContentOffsetY = scrollView.contentOffset.y;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)setupChart {
    self.chartView.delegate = self;
    
    self.chartView.chartDescription.enabled = NO;
    self.chartView.webLineWidth = 1.0f;
    self.chartView.innerWebLineWidth = 1.0f;
    self.chartView.webColor = UIColor.lightGrayColor;
    self.chartView.innerWebColor = UIColor.lightGrayColor;
    self.chartView.webAlpha = 1.0;

//    RadarMarkerView *marker = (RadarMarkerView *)[RadarMarkerView viewFromXib];
//    marker.chartView = self.chartView;
//    self.chartView.marker = marker;
    
    ChartXAxis *xAxis = _chartView.xAxis;
    xAxis.labelFont = [UIFont systemFontOfSize:14.f];
    xAxis.xOffset = 0.0f;
    xAxis.yOffset = 0.0f;
    xAxis.valueFormatter = self;
    xAxis.labelTextColor = [UIColor lightGrayTextColor];
    
    ChartYAxis *yAxis = _chartView.yAxis;
    yAxis.labelFont = [UIFont systemFontOfSize:8.f];
    yAxis.labelCount = (NSInteger) CUPPING_SCORE_MAX - (NSInteger) CUPPING_SCORE_MIN + 2;
    yAxis.forceLabelsEnabled = YES;
    yAxis.axisMinimum = CUPPING_SCORE_MIN - 1;
    yAxis.axisMaximum = CUPPING_SCORE_MAX;
    yAxis.drawLabelsEnabled = YES;
    yAxis.valueFormatter = self;
    yAxis.labelTextColor = [ChartColorTemplates colorFromString:@"#ff545472"];
    
    self.chartView.legend.enabled = NO;
}

- (RadarChartData *)createRadarData {
    
    NSMutableArray *entries = [[NSMutableArray alloc] init];
    [entries addObject:[[RadarChartDataEntry alloc] initWithValue:self.cupping.score1]];
    [entries addObject:[[RadarChartDataEntry alloc] initWithValue:self.cupping.score2]];
    [entries addObject:[[RadarChartDataEntry alloc] initWithValue:self.cupping.score3]];
    [entries addObject:[[RadarChartDataEntry alloc] initWithValue:self.cupping.score4]];
    [entries addObject:[[RadarChartDataEntry alloc] initWithValue:self.cupping.score5]];
    [entries addObject:[[RadarChartDataEntry alloc] initWithValue:self.cupping.score6]];
    [entries addObject:[[RadarChartDataEntry alloc] initWithValue:self.cupping.score7]];

    
    UIColor *fillColor = [ChartColorTemplates colorFromString:@"#ffb83f2e"];
    UIColor *circleColor = [ChartColorTemplates colorFromString:@"#fffff100"];
    
    RadarChartDataSet *set = [[RadarChartDataSet alloc] initWithValues:entries label:@"cupping"];
    set.lineWidth = 2.0f;
    [set setColor:fillColor];
    set.fillColor = fillColor;
    set.fillAlpha = 0.56f;
    set.drawFilledEnabled = YES;
    set.drawHighlightCircleEnabled = YES;
    set.highlightCircleStrokeWidth = 1.5f;
    set.highlightCircleInnerRadius = 0.f;
    set.highlightCircleOuterRadius = 3.f;
    set.highlightCircleStrokeColor = circleColor;
    set.highlightCircleStrokeAlpha = 1.f;
    set.highlightCircleFillColor = fillColor;
    [set setDrawHighlightIndicators:NO];
    
    radarDataSet = set;
    
    RadarChartData *data = [[RadarChartData alloc] initWithDataSets:@[set]];
    [data setDrawValues:NO];
    data.valueTextColor = UIColor.whiteColor;
    return data;
}

- (void)updateRadarChart {
    
    float total = 30;
    [radarDataSet clear];
    for (NSUInteger i = 0; i < 7; i++) {
        float s = [self getScore:scoreSliderArray[i]];
        [radarDataSet addEntry:[[RadarChartDataEntry alloc] initWithValue:s]];
        total += s;
    }
    [self.chartView.data notifyDataChanged];
    [self.chartView notifyDataSetChanged];
    [self.chartView setNeedsDisplay];
    
    self.totalInput.text = [NSString stringWithFormat:NSLocalizedString(@"label_score_x", nil), total];
}

#pragma mark - IAxisValueFormatter
- (NSString *)stringForValue:(double)value axis:(ChartAxisBase *)axis {
    if (axis == self.chartView.xAxis) {
        return activities[(int) value % activities.count];
    } else {
        if (value >= CUPPING_SCORE_MIN && value <= CUPPING_SCORE_MAX) {
            return [NSString stringWithFormat:@"%ld", (long) value];
        }
        return @"";
    }
    return @"error";
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return cellArray.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [cellArray objectAtIndex:indexPath.row];
    
    if (cell == self.cell3) {
        return CGRectGetWidth(tableView.bounds);
    } else if (cell == self.cell6) {
        [cell setNeedsUpdateConstraints];
        [cell updateConstraintsIfNeeded];
        [cell setNeedsLayout];
        [cell layoutIfNeeded];
        
        CGSize fittingSize = UILayoutFittingCompressedSize;
        fittingSize.width = CGRectGetWidth(tableView.bounds);
        CGSize size = [cell.contentView systemLayoutSizeFittingSize:fittingSize withHorizontalFittingPriority:UILayoutPriorityRequired verticalFittingPriority:UILayoutPriorityDefaultLow];
        
        commentHeight = self.commentInput.frame.size.height;
        return size.height;
    }
    return 44;
}
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    return [cellArray objectAtIndex:indexPath.row];
}



- (IBAction)toggleEditMode:(id)sender {
    if (self.editMode) {
        if ([self saveCupping]) {
            [self.navigationController popViewControllerAnimated:YES];
        }
    } else {
        self.editMode = YES;
    }
}

- (BOOL)saveCupping {
    if (self.nameInput.text.length == 0) {
        [SVProgressHUD showErrorWithStatus:NSLocalizedString(@"toast_cupping_name_empty", nil)];
        return NO;
    }
    
    RLMRealm *realm = [RLMRealm defaultRealm];
    [realm beginWriteTransaction];
    
    self.cupping.name = self.nameInput.text;
    self.cupping.comment = self.commentInput.text;
    self.cupping.profile = self.selectProfile;
    self.cupping.score1 = [self getScore:scoreSliderArray[0]];
    self.cupping.score2 = [self getScore:scoreSliderArray[1]];
    self.cupping.score3 = [self getScore:scoreSliderArray[2]];
    self.cupping.score4 = [self getScore:scoreSliderArray[3]];
    self.cupping.score5 = [self getScore:scoreSliderArray[4]];
    self.cupping.score6 = [self getScore:scoreSliderArray[5]];
    self.cupping.score7 = [self getScore:scoreSliderArray[6]];
    self.cupping.dirty = YES;
    
    [realm addOrUpdateObject:self.cupping];
    [realm commitWriteTransaction];
    return YES;
}

- (IBAction)shareCupping:(id)sender {
    NSURL *URL = [NSURL URLWithString:@"http://beta.wcare.cn:3003/profile/1/chart"];
    
    UIActivityViewController *activityViewController = [[UIActivityViewController alloc] initWithActivityItems:@[URL] applicationActivities:nil];
    [self.navigationController presentViewController:activityViewController
                                       animated:YES
                                     completion:nil];
}

@end
