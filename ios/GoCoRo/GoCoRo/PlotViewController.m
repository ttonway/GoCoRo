//
//  PlotViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/30.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "PlotViewController.h"
#import <SVProgressHUD/SVProgressHUD.h>

#import "GoCoRo-Swift.h"
#import <Charts/Charts-swift.h>

#import "Constants.h"
#import "EventButton.h"
#import "AWERatingBar.h"
#import "FormViewController.h"
#import "ScanViewController.h"
#import "GoCoRoDevice.h"

@interface PlotViewController () <ChartViewDelegate, IChartAxisValueFormatter, UIPopoverPresentationControllerDelegate, RatingBarDelegate> {
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
    
    NSInteger lineDataIndex;
    NSInteger tempDataSetIndex;
    
    LineChartDataSet *referenceTempDataSet;
    LineChartDataSet *referenceFireDataSet;
    
    RLMRealm *realm;
    RoastProfile *referenceProfile;
    NSInteger referenceIndex;
    RLMNotificationToken *notificationToken;
    
    GoCoRoDevice *device;
    BOOL autoChangeFire;
    BOOL completeDialogShowed;
    
    BOOL autoHighlightEnabled;
    
    CGRect navigationBarFrame;
    UIBarButtonItem *shareItem;
    UIBarButtonItem *formItem;
    UIBarButtonItem *connectItem;
    UIActivityIndicatorView *indicator;
    UIBarButtonItem *indicatorItem;
    
    TimePickerController *roastTimePicker;
    TimePickerController *eventTimePicker;
    EventButton *eventButton;
}

@property (nonatomic) IBOutlet UIView *roastBar;
@property (nonatomic) IBOutlet UIImageView *part1Background;
@property (nonatomic) IBOutlet UIImageView *part2Background;
@property (nonatomic) IBOutlet UIImageView *part3Background;
@property (nonatomic) IBOutlet UILabel *minuteLabel;
@property (nonatomic) IBOutlet UILabel *colonLabel;
@property (nonatomic) IBOutlet UILabel *secondLabel;
@property (nonatomic) IBOutlet UIButton *roastBtn;
@property (nonatomic) IBOutlet UIButton *coolBtn;
@property (nonatomic) IBOutlet UIView *fireBarContainer;
@property (nonatomic) AWERatingBar *fireBar;

@property (nonatomic) IBOutlet UIView *topBar;
@property (nonatomic) IBOutlet UILabel *nameLabel;
@property (nonatomic) IBOutlet UILabel *roastTimeLabel;
@property (nonatomic) IBOutlet UILabel *weightLabel;

@property (nonatomic) IBOutlet UILabel *logoLabel;
@property (nonatomic) IBOutlet CombinedChartView *chartView;

@property (nonatomic) IBOutlet UIToolbar *bottomBar;
@property (nonatomic) EventButton *eventBtn1;
@property (nonatomic) EventButton *eventBtn2;
@property (nonatomic) EventButton *eventBtn3;
@property (nonatomic) EventButton *eventBtn4;

@property (nonatomic) NSInteger minute;
@property (nonatomic) NSInteger second;
@property (nonatomic) NSInteger fire;

@end

static const float TEMPERATURE_MAX = 250.f;
static const float TEMPERATURE_MIN = 0;
static const float FIRE_MAX = 10;
static const float FIRE_MIN = 0;
static const float ONE_MIN_IN_SECONDS = 60;

@implementation PlotViewController
@synthesize minute = _minute;
@synthesize second = _second;

- (void)setMinute:(NSInteger)minute {
    _minute = minute;
    self.minuteLabel.text = [NSString stringWithFormat:@"%02ld", (long)minute];
}
- (void)setSecond:(NSInteger)second {
    _second = second;
    self.secondLabel.text = [NSString stringWithFormat:@"%02ld", (long)second];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    shareItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ic_menu_share"] style:UIBarButtonItemStylePlain target:self action:@selector(shareProfile:)];
    formItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ic_menu_form"] style:UIBarButtonItemStylePlain target:self action:@selector(gotoForm:)];
    indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
    indicatorItem = [[UIBarButtonItem alloc] initWithCustomView:indicator];
    connectItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ic_menu_unconnected"] style:UIBarButtonItemStylePlain target:self action:@selector(gotoDeviceScan:)];
    
    realm = [RLMRealm defaultRealm];
    device = [GoCoRoDevice sharedInstance];
    referenceProfile = self.profile.referenceProfile;
    autoHighlightEnabled = YES;
    self.minute = 0;
    self.second = 0;
    self.fire = 3;
    if (referenceProfile) {
        self.minute = referenceProfile.startDruation / 60;
        self.second = referenceProfile.startDruation % 60;
        self.fire = referenceProfile.startFire;
    }
    if (self.roast) {
        [device openDevice];
        if (!self.profile.complete && self.profile.startDruation > 0) {
            [self restoreRoast];
            
            self.fire = self.profile.startFire;
        }
    }
    
    if (self.roast) {
        self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ic_back"] style:UIBarButtonItemStylePlain target:self action:@selector(navigationback:)];
    }
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    [self initRoastBar];
    [self initBottomBar];
    
    self.logoLabel.font = [UIFont fontWithName:@"Bauhaus 93" size:150];
    [self setupChart];
    [self setChartData];
    
    
    [self onProfileChanged];
    __weak typeof(self) weakSelf = self;
    notificationToken = [self.profile addNotificationBlock:^(BOOL deleted, NSArray<RLMPropertyChange *> *changes, NSError *error) {
        if (error) {
            NSLog(@"Failed to open Realm on background worker: %@", error);
            return;
        }
        
        [weakSelf onProfileChanged];
    }];
    
    if (self.roast) {
        [self updateConnectItem];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateConnectItem) name:NotificationStateChange object:nil];
    }
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onError:) name:NotificationDeviceError object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onProfileComplete:) name:NotificationProfile object:nil];
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)initRoastBar {
    if (self.roast) {
        self.roastBar.backgroundColor = [UIColor clearColor];
        self.navigationItem.titleView = self.roastBar;
        //    [self.navigationController.navigationBar addSubview:self.roastBar];
        
        UIImage *image = [[UIImage imageNamed:@"part_background"] resizableImageWithCapInsets:UIEdgeInsetsMake(3, 3, 3, 3)];
        self.part1Background.image = image;
        self.part2Background.image = image;
        self.part3Background.image = image;
        
        image = [[UIImage imageNamed:@"btn_dark_background"] resizableImageWithCapInsets:UIEdgeInsetsMake(5, 5, 5, 5)];
        [self.roastBtn setBackgroundImage:image forState:UIControlStateNormal];
        self.roastBtn.tintColor = [UIColor customOrangeColor];
        [self.roastBtn addTarget:self action:@selector(startRoast:) forControlEvents:UIControlEventTouchUpInside];
        [self.coolBtn setBackgroundImage:image forState:UIControlStateNormal];
        self.coolBtn.tintColor = [UIColor whiteColor];
        [self.coolBtn setTitle:NSLocalizedString(@"btn_cool", nil) forState:UIControlStateNormal];
        [self.coolBtn addTarget:self action:@selector(stopRoast:) forControlEvents:UIControlEventTouchUpInside];
        
        // 54 w * 68 h
        self.fireBar = [[AWERatingBar alloc] initWithFrame:CGRectMake(0, 0, 135, 34)];
        [self.fireBarContainer addSubview:self.fireBar];
        [self.fireBar setStarImageWithNormalStar:[UIImage imageNamed:@"fire_empty"] selectedStar: [UIImage imageNamed:@"fire_full"]];
        self.fireBar.delegate = self;
        [self.fireBar displayRating:self.fire isIndicator:NO];
        
        for (UILabel *label in @[self.minuteLabel, self.secondLabel]) {
            UITapGestureRecognizer* tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(showRoastPicker:)];
            tapRecognizer.numberOfTapsRequired = 1;
            label.userInteractionEnabled = YES;
            [label addGestureRecognizer:tapRecognizer];
        }
    } else {
        self.navigationItem.rightBarButtonItems = @[formItem, shareItem];
    }
}
- (void)updateConnectItem {
    switch (device.state) {
        case StateOpen:
            connectItem.image = [UIImage imageNamed:@"ic_menu_connected"];
            connectItem.tintColor = [UIColor whiteColor];
            self.navigationItem.rightBarButtonItem = connectItem;
            [indicator stopAnimating];
            break;
        case StateClose:
            connectItem.image = [UIImage imageNamed:@"ic_menu_unconnected"];
            connectItem.tintColor = [UIColor customOrangeColor];
            self.navigationItem.rightBarButtonItem = connectItem;
            [indicator stopAnimating];
            break;
        case StateOpenging:
        case StateClosing:
            self.navigationItem.rightBarButtonItem = indicatorItem;
            [indicator startAnimating];
            break;
        default:
            break;
    }
}

- (void)initBottomBar {
    if (self.roast) {
        self.eventBtn1 = [[EventButton alloc] init];
        self.eventBtn2 = [[EventButton alloc] init];
        self.eventBtn3 = [[EventButton alloc] init];
        self.eventBtn4 = [[EventButton alloc] init];
        self.eventBtn1.event = EVENT_BURST1_START;
        self.eventBtn2.event = EVENT_BURST1;
        self.eventBtn3.event = EVENT_BURST2_START;
        self.eventBtn4.event = EVENT_BURST2;
        self.eventBtn1.nameLabel.text = NSLocalizedString(@"event_burst1_start_abbr", nil);
        self.eventBtn2.nameLabel.text = NSLocalizedString(@"event_burst1_abbr", nil);
        self.eventBtn3.nameLabel.text = NSLocalizedString(@"event_burst2_start_abbr", nil);
        self.eventBtn4.nameLabel.text = NSLocalizedString(@"event_burst2_abbr", nil);
        [self.eventBtn1 addTarget:self action:@selector(addEvent:) forControlEvents:UIControlEventTouchUpInside];
        [self.eventBtn2 addTarget:self action:@selector(addEvent:) forControlEvents:UIControlEventTouchUpInside];
        [self.eventBtn3 addTarget:self action:@selector(addEvent:) forControlEvents:UIControlEventTouchUpInside];
        [self.eventBtn4 addTarget:self action:@selector(addEvent:) forControlEvents:UIControlEventTouchUpInside];
        
        NSArray *buttons = @[self.eventBtn1, self.eventBtn2, self.eventBtn3, self.eventBtn4];
        for (RoastData *entry in self.profile.plotDatas) {
            if (entry.event) {
                for (EventButton *btn in buttons) {
                    if ([entry.event isEqualToString:btn.event]) {
                        btn.eventData = entry;
                        break;
                    }
                }
            }
        }
        
#define FLEXIBLE_ITEM [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil]
        self.bottomBar.items = @[FLEXIBLE_ITEM,
                                 [[UIBarButtonItem alloc] initWithCustomView:self.eventBtn1],
                                 FLEXIBLE_ITEM,
                                 [[UIBarButtonItem alloc] initWithCustomView:self.eventBtn2],
                                 FLEXIBLE_ITEM,
                                 [[UIBarButtonItem alloc] initWithCustomView:self.eventBtn3],
                                 FLEXIBLE_ITEM,
                                 [[UIBarButtonItem alloc] initWithCustomView:self.eventBtn4],
                                 FLEXIBLE_ITEM];
#undef FLEXIBLE_ITEM
    } else {
        [self.bottomBar removeFromSuperview];
    }
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
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
    
    if (self.roast) {
        CGFloat barHeight = 42;
        navigationBarFrame = self.navigationController.navigationBar.frame;
        CGRect rect = navigationBarFrame;
        CGFloat delta = barHeight - navigationBarFrame.size.height;
        rect.size.height = barHeight;
        self.navigationController.navigationBar.frame = rect;
        [self.navigationController.navigationBar setTitleVerticalPositionAdjustment:-delta/2 forBarMetrics:UIBarMetricsCompact];
        //    for (UIBarButtonItem *item in self.navigationItem.rightBarButtonItems) {
        //        [item setBackgroundVerticalPositionAdjustment:-delta/2 forBarMetrics:UIBarMetricsCompact];
        //    }
        
        NSArray* constrains = self.view.constraints;
        for (NSLayoutConstraint* constraint in constrains) {
            if ([constraint.identifier isEqualToString:@"topbar_top_space"]) {
                constraint.constant = delta;
            }
        }
        // [self.view setNeedsUpdateConstraints];
    }
    
}
- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    if (self.roast) {
        self.navigationController.navigationBar.frame = navigationBarFrame;
        [self.navigationController.navigationBar setTitleVerticalPositionAdjustment:0 forBarMetrics:UIBarMetricsCompact];
    }
}

- (void)onError:(NSNotification *)notification {
    NSError *error = notification.object;
    NSString *message = [NSString stringWithFormat:NSLocalizedString(@"error_connect_x", nil), error.localizedDescription];
    if (error.code == ERROR_WRONG_DEVICE) {
        message = NSLocalizedString(@"error_wrong_device", nil);
    } else if (error.code == ERROR_TIMEOUT) {
        message = NSLocalizedString(@"error_timeout", nil);
    } else if (error.code == ERROR_CONNECTION_FAIL) {
        message = NSLocalizedString(@"error_connection_error", nil);
    }
    
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"btn_ok", nil) style:UIAlertActionStyleCancel handler:nil];
    [alertController addAction:cancelAction];
    [self presentViewController:alertController animated:YES completion:nil];
}
- (void)onProfileComplete:(NSNotification *)notification {
    RoastProfile *p = notification.object;
    if (self.roast && !p) {
        if (self.profile.complete && !completeDialogShowed) {
            completeDialogShowed = YES;
            
            UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil message:NSLocalizedString(@"roast_completed", nil) preferredStyle:UIAlertControllerStyleAlert];
            UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"btn_ok", nil) style:UIAlertActionStyleCancel handler:nil];
            [alertController addAction:cancelAction];
            [self presentViewController:alertController animated:YES completion:nil];
        }
    }
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
    
    XYMarkerView *marker = [[XYMarkerView alloc]
                            initWithFont: [UIFont systemFontOfSize:12.f]
                            textColor: UIColor.whiteColor
                            insets: UIEdgeInsetsMake(4.f, 12.f, 12.f, 12.f)];
    marker.chartView = _chartView;
    self.chartView.marker = marker;
    
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

- (void)setChartData {
    CombinedChartData *data = [[CombinedChartData alloc] init];
    data.lineData = [self createLineData];
    data.scatterData = [self createScatterData];
    self.chartView.data = data;
    
    lineDataIndex = [data.allData indexOfObject:data.lineData];
    tempDataSetIndex = [data indexOfDataSet:tempDataSet];
}

- (void)onProfileChanged {
    self.nameLabel.text = [self.profile fullName];
    self.weightLabel.text = [NSString stringWithFormat:@"%ldg", self.profile.startWeight];
    if (self.profile.startDruation > 0) {
        self.roastTimeLabel.text = [NSString stringWithFormat:NSLocalizedString(@"label_roast_time_x", nil), [Utils formatSeconds:self.profile.startDruation]];
    } else {
        self.roastTimeLabel.text = nil;
    }
    
    NSString *str = self.profile.plotDatas.count == 0 ? NSLocalizedString(@"btn_start", nil) : NSLocalizedString(@"btn_set", nil);
    [self.roastBtn setTitle:str forState:UIControlStateNormal];
    
    NSInteger count = tempDataSet.entryCount;
    RoastData *currentData;// get the lastest one
    for (; count < self.profile.plotDatas.count; count++) {
        currentData = [self.profile.plotDatas objectAtIndex:count];
        [self addPlotData:currentData updateAxis:self.roast];
    }
    
    if (self.roast && autoHighlightEnabled && currentData) {
        ChartHighlight *highlight = [[ChartHighlight alloc] initWithX:currentData.time y:NAN dataSetIndex:tempDataSetIndex];
        highlight.dataIndex = lineDataIndex;
        [self.chartView highlightValue:highlight];
    }
    
    [self.chartView.data notifyDataChanged];
    [self.chartView notifyDataSetChanged];
    [self.chartView setNeedsDisplay];
    
    // 自动调整火力
    if (autoChangeFire && referenceProfile && currentData && currentData.status == StatusRoasting) {
        NSInteger currentTime = currentData.time;
        NSInteger currentFire = currentData.fire;
        NSInteger targetFire = -1;
        for (; referenceIndex < referenceProfile.plotDatas.count; referenceIndex++) {
            RoastData *refData = [referenceProfile.plotDatas objectAtIndex:referenceIndex];
            if (refData.status == StatusRoasting && refData.time > currentTime) {
                targetFire = refData.fire;
                break;
            }
        }
        if (targetFire != -1 && currentFire != targetFire) {
            [device setRoast:0 fire:targetFire];
        }
    }
}
- (void)addPlotData:(RoastData *)data updateAxis:(BOOL)updateAxis {
    NSDictionary *dic = [NSDictionary dictionaryWithObjectsAndKeys:self.profile, @"profile", data, @"data", nil];
    [tempDataSet addEntry:[[ChartDataEntry alloc] initWithX:data.time y:data.temperature icon:nil data:dic]];
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

#pragma mark - ChartViewDelegate

- (void)chartValueSelected:(ChartViewBase * __nonnull)chartView entry:(ChartDataEntry * __nonnull)entry highlight:(ChartHighlight * __nonnull)highlight
{
    NSLog(@"chartValueSelected");
    
    [PlotViewController cancelPreviousPerformRequestsWithTarget:self selector:@selector(enableAutoHighlight) object:nil];
    [self performSelector:@selector(enableAutoHighlight) withObject:nil afterDelay:5.0];
    autoHighlightEnabled = NO;
}

- (void)chartValueNothingSelected:(ChartViewBase * __nonnull)chartView
{
    NSLog(@"chartValueNothingSelected");
}

- (void)enableAutoHighlight {
    autoHighlightEnabled = YES;
}

#pragma mark - IAxisValueFormatter
- (NSString *)stringForValue:(double)value axis:(ChartAxisBase *)axis {
    if (axis == self.chartView.xAxis) {
        return [Utils formatSeconds:[self.profile getTimeInStatus:value]];
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


- (IBAction)showRoastPicker:(id)sender {
    roastTimePicker = [[TimePickerController alloc] init];
    roastTimePicker.enableStatus = NO;
    roastTimePicker.enableHour = NO;
    roastTimePicker.minute = self.minute;
    roastTimePicker.second = self.second;
    
    roastTimePicker.modalPresentationStyle = UIModalPresentationPopover;
    roastTimePicker.preferredContentSize = CGSizeMake(120, 90);
    UIPopoverPresentationController *popover = roastTimePicker.popoverPresentationController;
    popover.permittedArrowDirections = UIPopoverArrowDirectionUp;
    popover.delegate = self;
    popover.sourceView = self.colonLabel;
    popover.sourceRect = self.colonLabel.bounds;
    [self presentViewController:roastTimePicker animated:YES completion:nil];
}

- (UIModalPresentationStyle)adaptivePresentationStyleForPresentationController:(UIPresentationController *)controller {
    return UIModalPresentationNone;
}
- (void)popoverPresentationControllerDidDismissPopover:(UIPopoverPresentationController *)popoverPresentationController {
    if (popoverPresentationController.presentedViewController == roastTimePicker) {
        self.minute = roastTimePicker.minute;
        self.second = roastTimePicker.second;
    } else if (popoverPresentationController.presentedViewController == eventTimePicker) {
        
        EventButton *btn = eventButton;
        RoastStatus status = eventTimePicker.status;
        NSInteger seconds = ((eventTimePicker.hour * 60) + eventTimePicker.minute) * 60 + eventTimePicker.second;
        if (!btn) {
            return;
        }
        
        if (status == StatusRoasting) {
            if (self.profile.roastTime == 0) {
                [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_event_time_invalid", nil)];
                return;
            }
            seconds += self.profile.roastTime;
        } else if (status == StatusCooling) {
            if (self.profile.coolTime == 0) {
                [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_event_time_invalid", nil)];
                return;
            }
            seconds += self.profile.coolTime;
        }
            
        RoastData *oldData = btn.eventData;
        RoastData *newData;
        for (RoastData *data in self.profile.plotDatas) {
            if (data.event) {
                continue;
            }
            NSInteger distance = labs(data.time - seconds);
            if (data.status == status && distance < 3) {
                if (!newData) {
                    newData = data;
                } else if (distance < labs(newData.time - seconds)) {
                    newData = data;
                }
                
                if (distance == 0) {
                    break;
                }
            } else if (newData) {
                break;
            }
        }
        
        if (!newData) {
            [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_event_time_invalid", nil)];
            return;
        }
        
        [realm transactionWithBlock:^{
            self.profile.dirty = YES;
            oldData.event = nil;
            newData.event = btn.event;
        }];
        btn.eventData = newData;
        
        [eventDataSet removeEntryWithX:oldData.time];
        [eventDataSet addEntryOrdered:[[ChartDataEntry alloc] initWithX:newData.time y:newData.temperature]];
        [self.chartView.data notifyDataChanged];
        [self.chartView notifyDataSetChanged];
        [self.chartView setNeedsDisplay];
    }
}


- (void)ratingBar:(AWERatingBar *)ratingBar ratingChanged:(CGFloat)rating{
    NSLog(@"onRatingChanged %f", rating);
    self.fire = rating;

    [PlotViewController cancelPreviousPerformRequestsWithTarget:self selector:@selector(changeFire) object:nil];
    [self performSelector:@selector(changeFire) withObject:nil afterDelay:2.0];
}
- (void)changeFire {
    BOOL roasting = self.profile.roastTime != 0 && self.profile.coolTime == 0;
    if ([device isOpen] && roasting && !self.profile.complete) {
        RoastData *data = self.profile.plotDatas.lastObject;
        if (data) {
            if (autoChangeFire) {
                autoChangeFire = NO;
            }
            
            [device setRoast:0 fire:self.fire];
        }
    }
}

- (IBAction)startRoast:(id)sender {
    NSInteger seconds = self.minute * 60 + self.second;
    NSInteger fire = self.fire;
    
    if (seconds == 0) {
        [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_roast_time_zero", nil)];
        return;
    }
    if (![device isOpen]) {
        [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_device_unconnected", nil)];
        return;
    }
    
    if (self.profile.plotDatas.count > 0) {
        BOOL roasting = self.profile.roastTime != 0 && self.profile.coolTime == 0;
        if (self.profile.complete) {
            [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_already_completed", nil)];
            return;
        } else if (!roasting) {
            return;
        }
        
        if (autoChangeFire) {
            autoChangeFire = NO;
        }
        
        [device setRoast:seconds fire:fire];
    } else {
        if (device.profile || [device isDeviceBusy]) {
            [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_now_roasting", nil)];
            return;
        }
        
        if (referenceProfile && referenceProfile.startDruation == seconds && referenceProfile.startFire == fire) {
            autoChangeFire = true;
        }
        
        [device readyProfile:self.profile];
        [device startRoast:seconds fire:fire cool:self.profile.coolTemperature];
        
        [realm transactionWithBlock:^{
            self.profile.deviceId = device.dirver.currentPeripheral.identifier.UUIDString;
            self.profile.startTime = [NSDate date];
            self.profile.startFire = fire;
            self.profile.startDruation = seconds;
            self.profile.dirty = YES;
        }];
    }
    
    self.minute = 0;
    self.second = 0;
}

- (IBAction)stopRoast:(id)sender {
    if (![device isOpen]) {
        [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_device_unconnected", nil)];
    } else {
        RoastData *data = self.profile.plotDatas.lastObject;
        if (data) {
            [device stopRoast];
            
            [realm transactionWithBlock:^{
                data.manualCool = YES;
            }];
        }
    }
}

- (void)restoreRoast {
    if ([device isOpen] && !device.profile) {
        [device readyProfile:self.profile];
    }
}

- (IBAction)addEvent:(id)sender {
    EventButton *btn = (EventButton *)sender;
    RoastData *lastData = self.profile.plotDatas.lastObject;
    if (btn.eventData) {
        RoastData *data = btn.eventData;
        NSInteger seconds = [self.profile getTimeInStatus:data.time];
        NSInteger roastTime = self.profile.coolTime != 0 ? self.profile.coolTime : lastData.time + 1;
        roastTime = roastTime - self.profile.roastTime;
        NSInteger coolTime = self.profile.coolTime != 0 ? lastData.time + 1 - self.profile.coolTime : 0;
        BOOL enableHour = NO;
        if (roastTime > 3600 || coolTime > 3600) {
            enableHour = YES;
        }
        
        eventButton = btn;
        NSInteger hour = seconds / 3600;
        NSInteger min = seconds / 60 - hour * 60;
        NSInteger sec = seconds - min * 60 - hour * 3600;
        eventTimePicker = [[TimePickerController alloc] init];
        eventTimePicker.enableHour = enableHour;
        eventTimePicker.status = data.status;
        eventTimePicker.hour = hour;
        eventTimePicker.minute = min;
        eventTimePicker.second = sec;
        
        eventTimePicker.modalPresentationStyle = UIModalPresentationPopover;
        eventTimePicker.preferredContentSize = CGSizeMake(180, 90);
        UIPopoverPresentationController *popover = eventTimePicker.popoverPresentationController;
        popover.permittedArrowDirections = UIPopoverArrowDirectionUp;
        popover.delegate = self;
        popover.sourceView = btn;
        popover.sourceRect = btn.bounds;
        [self presentViewController:eventTimePicker animated:YES completion:nil];
        return;
    }
    
    if (!lastData || lastData.status == StatusPreheating || lastData.event) {
        return;
    }
    
    btn.eventData = lastData;
    [realm transactionWithBlock:^{
        lastData.event = btn.event;
    }];
    
    [eventDataSet addEntry:[[ChartDataEntry alloc] initWithX:lastData.time y:lastData.temperature]];
    [self.chartView.data notifyDataChanged];
    [self.chartView notifyDataSetChanged];
    [self.chartView setNeedsDisplay];
}

- (IBAction)gotoForm:(id)sender {
    FormViewController *controller = [[FormViewController alloc] init];
    controller.hidesBottomBarWhenPushed = YES;
    controller.profile = self.profile;
    [self.navigationController pushViewController:controller animated:YES];
}
- (IBAction)shareProfile:(id)sender {

}

- (IBAction)gotoDeviceScan:(id)sender {
    ScanViewController *controller = [[ScanViewController alloc] init];
    controller.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:controller animated:YES];
}

- (IBAction)navigationback:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

@end
