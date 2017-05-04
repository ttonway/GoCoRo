//
//  AboutViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/24.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "AboutViewController.h"

#import "Constants.h"

@interface AboutViewController ()

@end

@implementation AboutViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"activity_about", nil);
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    
    self.logoLabel.font = [UIFont fontWithName:@"Bauhaus 93" size:20];
    self.versionLabel.font = [UIFont systemFontOfSize:20];
    self.versionLabel.text = [Utils appVersion];
    
    self.textView.backgroundColor = [UIColor clearColor];
    self.textView.textColor = [UIColor whiteColor];
    self.textView.font = [UIFont systemFontOfSize:15.f];
    self.textView.editable = NO;
    self.textView.dataDetectorTypes = UIDataDetectorTypePhoneNumber | UIDataDetectorTypeLink;
    
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
//    [self.textView scrollRectToVisible:CGRectMake(0, 0, 1, 1) animated:NO];
    self.textView.text = NSLocalizedString(@"about_content", nil);
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
