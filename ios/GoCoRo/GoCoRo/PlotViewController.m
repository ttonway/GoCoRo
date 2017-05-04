//
//  PlotViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/30.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "PlotViewController.h"

#import "GoCoRo-Swift.h"
#import <Charts/Charts-swift.h>

#import "Constants.h"
#import "FormViewController.h"

@interface PlotViewController () <ChartViewDelegate, IChartAxisValueFormatter> {
    UIColor *textColor;
    UIColor *limitLineColor;
    UIColor *lineColor;
    UIColor *fireColor;
    CGFloat fireColoralpha;
    UIColor *line2Color;
    UIColor *fire2Color;
    CGFloat fire2Coloralpha;
    
    LineChartDataSet *tempDataSet;
    LineChartDataSet *preHeatDataSet;
    LineChartDataSet *roastDataSet;
    LineChartDataSet *coolDataSet;
    ScatterChartDataSet *eventDataSet;
    LineChartDataSet *fireDataSet;
    
    LineChartDataSet *referenceTempDataSet;
    LineChartDataSet *referenceFireDataSet;
    
    RoastProfile *referenceProfile;
    
    RLMNotificationToken *notificationToken;

    
    CGRect navigationBarFrame;
}

@property (nonatomic, strong) CombinedChartView *chartView;

@end

static const float TEMPERATURE_MAX = 250.f;
static const float TEMPERATURE_MIN = 0;
static const float FIRE_MAX = 10;
static const float FIRE_MIN = 0;
static const float ONE_MIN_IN_SECONDS = 60;

@implementation PlotViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // 触发屏幕旋转
//    UIViewController *fakeController = [[UIViewController alloc] init];
//    [self presentViewController:fakeController animated:NO completion:^(void){
//        NSLog(@"forcing VC has been presented.");
//        dispatch_async(dispatch_get_main_queue(), ^{
//            [self dismissViewControllerAnimated:NO completion:^(void){
//                NSLog(@"forcing VC has been dismissed");
//            }];
//        });
//    }];
    NSNumber *value = [NSNumber numberWithInt:UIInterfaceOrientationLandscapeLeft];
    [[UIDevice currentDevice] setValue:value forKey:@"orientation"];
    
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    
    
    
    referenceProfile = self.profile.referenceProfile;
    
    self.chartView = [[CombinedChartView alloc] initWithFrame:CGRectZero];
    [self.view addSubview:self.chartView];
    [self setupChart];
    [self setChartData];
    
    if (self.roast) {
    } else {
    }
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    navigationBarFrame = self.navigationController.navigationBar.frame;
    CGRect rect = navigationBarFrame;
    rect.size.height = 80;
    self.navigationController.navigationBar.frame = rect;
}
- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    self.navigationController.navigationBar.frame = navigationBarFrame;
}


- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];
    
    self.chartView.frame = self.view.bounds;
}

- (void)setupChart {
    
    textColor = [UIColor lightGrayTextColor];
    limitLineColor = [ChartColorTemplates colorFromString:@"#ff2a2630"];
    lineColor = [ChartColorTemplates colorFromString:@"#fffff100"];
    fireColor = [ChartColorTemplates colorFromString:@"#ff75001b"];
    fireColoralpha = 0.7f;
    line2Color = [ChartColorTemplates colorFromString:@"#ff0075c9"];
    fire2Color = [ChartColorTemplates colorFromString:@"#ffc20430"];
    fire2Coloralpha = 0.2f;
    
    self.chartView.delegate = self;
    
    self.chartView.chartDescription.enabled = NO;

    self.chartView.drawOrder = @[
                             @(CombinedChartDrawOrderBar),
                             @(CombinedChartDrawOrderBubble),
                             @(CombinedChartDrawOrderCandle),
                             @(CombinedChartDrawOrderLine),
                             @(CombinedChartDrawOrderScatter)
                             ];
    
    self.chartView.legend.enabled = NO;
    
    ChartYAxis *leftAxis = self.chartView.leftAxis;
    leftAxis.gridLineDashLengths = @[@10.0, @10.0];
    leftAxis.gridLineDashPhase = 0.f;
    leftAxis.axisMaximum = TEMPERATURE_MAX;
    leftAxis.axisMinimum = TEMPERATURE_MIN;
    leftAxis.labelTextColor = textColor;
    leftAxis.drawZeroLineEnabled = NO;
    leftAxis.valueFormatter = self;
    
    ChartYAxis *rightAxis = self.chartView.rightAxis;
    rightAxis.drawGridLinesEnabled = NO;
    rightAxis.axisMaximum = FIRE_MAX;
    rightAxis.axisMinimum = FIRE_MIN;
    rightAxis.drawZeroLineEnabled = NO;
    rightAxis.granularityEnabled = NO;
    rightAxis.enabled = NO;
    [rightAxis removeAllLimitLines];
    for (int i = 1; i <= 5; i++) {
        NSString *label = [NSString stringWithFormat:NSLocalizedString(@"label_fire_x", nil), i];
        ChartLimitLine *ll = [[ChartLimitLine alloc] initWithLimit:i label:label];
        ll.lineWidth = 1.f;
        ll.lineColor = limitLineColor;
        ll.valueTextColor = textColor;
        ll.valueFont = [UIFont systemFontOfSize:10.0];
        [rightAxis addLimitLine:ll];
    }
    
    ChartXAxis *xAxis = self.chartView.xAxis;
    xAxis.labelPosition = XAxisLabelPositionBottom;
    xAxis.gridLineDashLengths = @[@10.0, @10.0];
    xAxis.gridLineDashPhase = 0.f;
    xAxis.labelTextColor = textColor;
    xAxis.granularity = 60.0;
    xAxis.axisMinimum = 0.0;
    xAxis.valueFormatter = self;
    
    NSNumber *maxTime = [self.profile.plotDatas maxOfProperty:@"time"];
    NSNumber *maxTime2 = [referenceProfile.plotDatas maxOfProperty:@"time"];
    NSInteger max = ONE_MIN_IN_SECONDS;
    if (!maxTime && !maxTime2 && self.roast) {
        max = 10 * ONE_MIN_IN_SECONDS;
    } else {
        max += MAX([maxTime integerValue], [maxTime2 integerValue]);
    }
    xAxis.axisMaximum = max;
}

- (void)setChartData
{
    CombinedChartData *data = [[CombinedChartData alloc] init];
    data.lineData = [self createLineData];
    data.scatterData = [self createScatterData];
    self.chartView.data = data;
    
    [self onProfileChanged];
    __weak typeof(self) weakSelf = self;
    notificationToken = [self.profile addNotificationBlock:^(BOOL deleted, NSArray<RLMPropertyChange *> *changes, NSError *error) {
        if (error) {
            NSLog(@"Failed to open Realm on background worker: %@", error);
            return;
        }
        
        [weakSelf onProfileChanged];
    }];
}

- (void)onProfileChanged {
    NSInteger count = tempDataSet.entryCount;
    RoastData *currentData;// get the lastest one
    for (; count < self.profile.plotDatas.count; count++) {
        currentData = [self.profile.plotDatas objectAtIndex:count];
        [self addPlotData:currentData updateAxis:self.roast];
    }
    [self.chartView.data notifyDataChanged];
    [self.chartView notifyDataSetChanged];
    [self.chartView setNeedsDisplay];
    
}
- (void)addPlotData:(RoastData *)data updateAxis:(BOOL)updateAxis {
    [tempDataSet addEntry:[[ChartDataEntry alloc] initWithX:data.time y:data.temperature icon:nil data:data]];
    [fireDataSet addEntry:[[ChartDataEntry alloc] initWithX:data.time y:data.fire]];
    
    if (data.status == StatusPreheating) {
        [preHeatDataSet addEntry:[[ChartDataEntry alloc] initWithX:data.time y:TEMPERATURE_MAX]];
    } else if (data.status == StatusRoasting) {
        [roastDataSet addEntry:[[ChartDataEntry alloc] initWithX:data.time y:TEMPERATURE_MAX]];
    } else if (data.status == StatusCooling) {
        [coolDataSet addEntry:[[ChartDataEntry alloc] initWithX:data.time y:TEMPERATURE_MAX]];
    }
    
    if (data.event) {
        [eventDataSet addEntry:[[ChartDataEntry alloc] initWithX:data.time y:data.temperature]];
    }
  
    if (updateAxis) {
        ChartXAxis *xAxis = self.chartView.xAxis;
        if (data.time + ONE_MIN_IN_SECONDS > xAxis.axisMaximum) {
            xAxis.axisMaximum = data.time + 5 * ONE_MIN_IN_SECONDS;
        }
    }
}

- (LineChartData *)createLineData {
    UIColor *preheatStartColor = [ChartColorTemplates colorFromString:@"#b2ee9999"];
    UIColor *preheatEndColor = [ChartColorTemplates colorFromString:@"#b2f9dede"];
    UIColor *roastStartColor = [ChartColorTemplates colorFromString:@"#b2fb4948"];
    UIColor *roastEndColor = [ChartColorTemplates colorFromString:@"#b2fdc4c3"];
    UIColor *coolStartColor = [ChartColorTemplates colorFromString:@"#b28268a2"];
    UIColor *coolEndColor = [ChartColorTemplates colorFromString:@"#b2544c5d"];
    
    LineChartData *data = [[LineChartData alloc] init];
    tempDataSet = [self createTemperatureDataSet:@"temperature"];
    preHeatDataSet = [self createStatusLineDataSet:@"preheat" startColor:preheatStartColor endColor:preheatEndColor];
    roastDataSet = [self createStatusLineDataSet:@"roast" startColor:roastStartColor endColor:roastEndColor];
    coolDataSet = [self createStatusLineDataSet:@"cool" startColor:coolStartColor endColor:coolEndColor];
    fireDataSet = [self createFireLineDataSet:@"fire"];
    
    if (referenceProfile) {
        referenceTempDataSet = [self createTemperatureDataSet:@"reference-temperature"];
        [referenceTempDataSet setColor:line2Color];
        referenceTempDataSet.highlightEnabled = NO;
        
        referenceFireDataSet = [self createFireLineDataSet:@"reference-fire"];
        [referenceFireDataSet setColor:fire2Color];
        referenceFireDataSet.fillColor = fire2Color;
        referenceFireDataSet.fillAlpha = fire2Coloralpha;
        
        for (RoastData *data in referenceProfile.plotDatas) {
            [referenceTempDataSet addEntry:[[ChartDataEntry alloc] initWithX:data.time y:data.temperature]];
            [referenceFireDataSet addEntry:[[ChartDataEntry alloc] initWithX:data.time y:data.fire]];
        }
        
        [data addDataSet:referenceFireDataSet];
        [data addDataSet:referenceTempDataSet];
    }
    [data addDataSet:preHeatDataSet];
    [data addDataSet:roastDataSet];
    [data addDataSet:coolDataSet];
    [data addDataSet:fireDataSet];
    [data addDataSet:tempDataSet];
    
    return data;
}
- (ScatterChartData *)createScatterData {
    ScatterChartData *data = [[ScatterChartData alloc] init];
    
    NSMutableArray *entries = [[NSMutableArray alloc] init];
    ScatterChartDataSet *set = [[ScatterChartDataSet alloc] initWithValues:entries label:@"event"];
    [set setColor:lineColor];
    [set setScatterShape:ScatterShapeCircle];
    set.scatterShapeHoleColor = [ChartColorTemplates colorFromString:@"#ffe50014"];
    set.scatterShapeSize = 10.f;
    set.scatterShapeHoleRadius = 3.f;
    set.drawValuesEnabled = NO;
    set.highlightEnabled = NO;
    
    eventDataSet = set;
    [data addDataSet:set];
    
    return data;
}

- (LineChartDataSet *)createTemperatureDataSet:(NSString *)label {
    NSMutableArray *entries = [[NSMutableArray alloc] init];
    LineChartDataSet *set = [[LineChartDataSet alloc] initWithValues:entries label:label];
    [set setColor:lineColor];
    set.lineWidth = 2.f;
    set.drawCirclesEnabled = NO;
    set.drawValuesEnabled = NO;
    set.drawCircleHoleEnabled = NO;
    set.highlightEnabled = YES;
    set.drawHorizontalHighlightIndicatorEnabled = NO;
    set.drawVerticalHighlightIndicatorEnabled = YES;
    
    return set;
}
- (LineChartDataSet *)createStatusLineDataSet:(NSString *)label startColor:(UIColor *)startColor endColor:(UIColor *)endColor {
    
    NSArray *gradientColors = @[(id)startColor.CGColor, (id)endColor.CGColor];
    CGGradientRef gradient = CGGradientCreateWithColors(nil, (CFArrayRef)gradientColors, nil);
    
    NSMutableArray *entries = [[NSMutableArray alloc] init];
    LineChartDataSet *set = [[LineChartDataSet alloc] initWithValues:entries label:label];
    set.axisDependency = AxisDependencyLeft;
    set.lineWidth = 1.f;
    [set setColor:startColor];
    set.highlightEnabled = NO;
    set.drawCirclesEnabled = NO;
    set.drawValuesEnabled = NO;
    set.drawCircleHoleEnabled = NO;
    set.drawFilledEnabled = YES;
    set.fillAlpha = 1.0f;
    set.fill = [ChartFill fillWithLinearGradient:gradient angle:270.f];
    __weak typeof(self) weakSelf = self;
    set.fillFormatter =  [ChartDefaultFillFormatter withBlock:^CGFloat(id<ILineChartDataSet>  _Nonnull dataSet, id<LineChartDataProvider>  _Nonnull dataProvider) {
        return weakSelf.chartView.leftAxis.axisMinimum;
    }];
    
    CGGradientRelease(gradient);
    
    return set;
}
- (LineChartDataSet *)createFireLineDataSet:(NSString *)label {
    NSMutableArray *entries = [[NSMutableArray alloc] init];
    LineChartDataSet *set = [[LineChartDataSet alloc] initWithValues:entries label:label];
    set.axisDependency = AxisDependencyRight;
    set.lineWidth = 1.f;
    [set setColor:fireColor];
    set.highlightEnabled = NO;
    set.drawCirclesEnabled = NO;
    set.drawValuesEnabled = NO;
    set.drawCircleHoleEnabled = NO;
    set.drawFilledEnabled = YES;
    set.fillColor = fireColor;
    set.fillAlpha = fireColoralpha;
    __weak typeof(self) weakSelf = self;
    set.fillFormatter =  [ChartDefaultFillFormatter withBlock:^CGFloat(id<ILineChartDataSet>  _Nonnull dataSet, id<LineChartDataProvider>  _Nonnull dataProvider) {
        return weakSelf.chartView.rightAxis.axisMinimum;
    }];
    
    return set;
}

- (CGFloat)getFillLinePositionWithDataSet:(LineChartDataSet *)dataSet dataProvider:(id<LineChartDataProvider>)dataProvider
{
    return -10.f;
}

- (NSInteger)getTimeInStatus:(NSInteger)time {
    if (self.profile.coolTime != 0 && time >= self.profile.coolTime) {
        time -= self.profile.coolTime;
    } else if (self.profile.roastTime != 0 && time >= self.profile.roastTime) {
        time -= self.profile.roastTime;
    }
    return time;
}

#pragma mark - ChartViewDelegate

- (void)chartValueSelected:(ChartViewBase * __nonnull)chartView entry:(ChartDataEntry * __nonnull)entry highlight:(ChartHighlight * __nonnull)highlight
{
    NSLog(@"chartValueSelected");
}

- (void)chartValueNothingSelected:(ChartViewBase * __nonnull)chartView
{
    NSLog(@"chartValueNothingSelected");
}

#pragma mark - IAxisValueFormatter
- (NSString *)stringForValue:(double)value axis:(ChartAxisBase *)axis {
    if (axis == self.chartView.xAxis) {
        return [Utils formatSeconds:[self getTimeInStatus:value]];
    } else if (axis == self.chartView.leftAxis) {
        if (value < 0) {
            return @"";
        }
        return [NSString stringWithFormat:@"%ld℃", (long)value];
    } else {
        return @"error";
    }
}

#pragma mark - Screen Rotate
- (BOOL)shouldAutorotate {
    return YES;
}
- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskLandscape;
}
- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationLandscapeLeft;
}



- (IBAction)gotoForm:(id)sender {
    FormViewController *controller = [[FormViewController alloc] init];
    controller.hidesBottomBarWhenPushed = YES;
    controller.profile = self.profile;
    [self.navigationController pushViewController:controller animated:YES];
}

@end
