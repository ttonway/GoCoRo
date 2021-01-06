//
//  MoreViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/24.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "MoreViewController.h"
#import <SVProgressHUD/SVProgressHUD.h>

#import "Constants.h"
#import "NewsListViewController.h"
#import "CoporateViewController.h"
#import "AboutViewController.h"

@interface MoreViewController () {
    UITableViewCell *cell1;
    UITableViewCell *cell2;
    
    UIColor *headerBackgroundColor;
    
    UIButton *btnKnowledge;
    UIButton *btnBuyDevice;
    UIButton *btnBuyBean;
    UIButton *btnStore;
    UIButton *btnNews;
    UIButton *btnCoporate;
    UIButton *btnAbout;
    
    UIButton *btnFacebook;
    UIButton *btnWeibo;
}

@end

@implementation MoreViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"activity_more", nil);
    
    headerBackgroundColor = [UIColor colorWithWhite:1.f alpha:0.1f];
    
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    self.tableView.allowsSelection = NO;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    
    
    cell1 = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                   reuseIdentifier:nil];
    cell1.backgroundColor = [UIColor clearColor];
    cell2 = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                  reuseIdentifier:nil];
    cell2.backgroundColor = headerBackgroundColor;
    
    btnKnowledge = [UIButton buttonWithType:UIButtonTypeSystem];
    btnKnowledge.tintColor = [UIColor lightTextColor];
    [btnKnowledge setImage:[UIImage imageNamed:@"ic_knowledges"] forState:UIControlStateNormal];
    [btnKnowledge setTitle:NSLocalizedString(@"btn_knowledge", nil) forState:UIControlStateNormal];
    btnBuyDevice = [UIButton buttonWithType:UIButtonTypeSystem];
    btnBuyDevice.tintColor = [UIColor lightTextColor];
    [btnBuyDevice setImage:[UIImage imageNamed:@"ic_shopping"] forState:UIControlStateNormal];
    [btnBuyDevice setTitle:NSLocalizedString(@"btn_buy_device", nil) forState:UIControlStateNormal];
    btnBuyBean = [UIButton buttonWithType:UIButtonTypeSystem];
    btnBuyBean.tintColor = [UIColor lightTextColor];
    [btnBuyBean setImage:[UIImage imageNamed:@"ic_buy_bean"] forState:UIControlStateNormal];
    [btnBuyBean setTitle:NSLocalizedString(@"btn_buy_bean", nil) forState:UIControlStateNormal];
    btnStore = [UIButton buttonWithType:UIButtonTypeSystem];
    btnStore.tintColor = [UIColor lightTextColor];
    [btnStore setImage:[UIImage imageNamed:@"ic_store"] forState:UIControlStateNormal];
    [btnStore setTitle:NSLocalizedString(@"btn_store", nil) forState:UIControlStateNormal];
    btnNews = [UIButton buttonWithType:UIButtonTypeSystem];
    btnNews.tintColor = [UIColor lightTextColor];
    [btnNews setImage:[UIImage imageNamed:@"ic_news"] forState:UIControlStateNormal];
    [btnNews setTitle:NSLocalizedString(@"btn_news", nil) forState:UIControlStateNormal];
    btnCoporate = [UIButton buttonWithType:UIButtonTypeSystem];
    btnCoporate.tintColor = [UIColor lightTextColor];
    [btnCoporate setImage:[UIImage imageNamed:@"ic_coporate"] forState:UIControlStateNormal];
    [btnCoporate setTitle:NSLocalizedString(@"btn_coporate", nil) forState:UIControlStateNormal];
    btnAbout = [UIButton buttonWithType:UIButtonTypeSystem];
    btnAbout.tintColor = [UIColor lightTextColor];
    [btnAbout setImage:[UIImage imageNamed:@"ic_about"] forState:UIControlStateNormal];
    [btnAbout setTitle:NSLocalizedString(@"btn_about", nil) forState:UIControlStateNormal];
    
    btnKnowledge.titleLabel.adjustsFontSizeToFitWidth = YES;
    btnBuyDevice.titleLabel.adjustsFontSizeToFitWidth = YES;
    btnBuyBean.titleLabel.adjustsFontSizeToFitWidth = YES;
    btnStore.titleLabel.adjustsFontSizeToFitWidth = YES;
    btnNews.titleLabel.adjustsFontSizeToFitWidth = YES;
    btnCoporate.titleLabel.adjustsFontSizeToFitWidth = YES;
    btnAbout.titleLabel.adjustsFontSizeToFitWidth = YES;
    
    
    btnFacebook = [UIButton buttonWithType:UIButtonTypeSystem];
    btnFacebook.tintColor = [UIColor lightTextColor];
    [btnFacebook setImage:[UIImage imageNamed:@"ic_facebook"] forState:UIControlStateNormal];
    [btnFacebook setTitle:@"Facebook" forState:UIControlStateNormal];
    btnWeibo = [UIButton buttonWithType:UIButtonTypeSystem];
    btnWeibo.tintColor = [UIColor lightTextColor];
    [btnWeibo setImage:[UIImage imageNamed:@"ic_sina_weibo"] forState:UIControlStateNormal];
    [btnWeibo setTitle:@"微博" forState:UIControlStateNormal];
    
    [cell1.contentView addSubview:btnKnowledge];
    [cell1.contentView addSubview:btnBuyDevice];
    [cell1.contentView addSubview:btnBuyBean];
    [cell1.contentView addSubview:btnStore];
    [cell1.contentView addSubview:btnNews];
    [cell1.contentView addSubview:btnCoporate];
    [cell1.contentView addSubview:btnAbout];
    [cell2.contentView addSubview:btnFacebook];
    [cell2.contentView addSubview:btnWeibo];
    
    
    [btnKnowledge addTarget:self action:@selector(gotoCoffeeKnowledges:) forControlEvents:UIControlEventTouchUpInside];
    [btnBuyDevice addTarget:self action:@selector(gotoBuyDevice:) forControlEvents:UIControlEventTouchUpInside];
    [btnBuyBean addTarget:self action:@selector(gotoBuyBean:) forControlEvents:UIControlEventTouchUpInside];
    [btnStore addTarget:self action:@selector(gotoShoppingStore:) forControlEvents:UIControlEventTouchUpInside];
    [btnNews addTarget:self action:@selector(gotoNews:) forControlEvents:UIControlEventTouchUpInside];
    [btnCoporate addTarget:self action:@selector(gotoCoporate:) forControlEvents:UIControlEventTouchUpInside];
    [btnAbout addTarget:self action:@selector(gotoAbout:) forControlEvents:UIControlEventTouchUpInside];
    [btnFacebook addTarget:self action:@selector(gotoFacebook:) forControlEvents:UIControlEventTouchUpInside];
    [btnWeibo addTarget:self action:@selector(gotoWeibo:) forControlEvents:UIControlEventTouchUpInside];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)gotoCoffeeKnowledges:(id)sender {
    [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_coming_soon", nil)];
}

- (IBAction)gotoNews:(id)sender {
//    NewsListViewController *controller = [[NewsListViewController alloc] init];
//    [self.navigationController pushViewController:controller animated:YES];
    [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_coming_soon", nil)];
}

- (IBAction)gotoBuyDevice:(id)sender {
//    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"https://abudodo.world.taobao.com/"]];
    [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_coming_soon", nil)];
}

- (IBAction)gotoBuyBean:(id)sender {
    [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_coming_soon", nil)];
}

- (IBAction)gotoShoppingStore:(id)sender {
    [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_coming_soon", nil)];
}

- (IBAction)gotoAbout:(id)sender {
    AboutViewController *controller = [[AboutViewController alloc] init];
    [self.navigationController pushViewController:controller animated:YES];
}

- (IBAction)gotoCoporate:(id)sender {
//    CoporateViewController *controller = [[CoporateViewController alloc] init];
//    [self.navigationController pushViewController:controller animated:YES];
    [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_coming_soon", nil)];
}

- (IBAction)gotoFacebook:(id)sender {
//    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"https://www.facebook.com/groups/gocoro/"]];
    [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_coming_soon", nil)];
}

- (IBAction)gotoWeibo:(id)sender {
//    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"http://weibo.com/gocoro"]];
    [SVProgressHUD showInfoWithStatus:NSLocalizedString(@"toast_coming_soon", nil)];
}


#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 30;
}
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    CGFloat w = CGRectGetWidth(tableView.bounds);
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, 30)];
    headerView.backgroundColor = headerBackgroundColor;
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(8, 0, w - 16, 30)];
    label.backgroundColor = [UIColor clearColor];
    label.textColor = [UIColor whiteColor];
    label.text = section == 0 ? NSLocalizedString(@"label_more", nil) : NSLocalizedString(@"label_socialization", nil);
    
    [headerView addSubview:label];
    return headerView;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 1;
}

#define BUTTON_WIDTH 80
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 0) {
        CGFloat height = CGRectGetHeight(tableView.bounds) - tableView.contentInset.top - tableView.contentInset.bottom;;
        return MAX(height - 60 - 44, BUTTON_WIDTH * 2 + 24);
    }
    
    return 44;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    CGFloat width = CGRectGetWidth(tableView.bounds);
    if (indexPath.section == 0) {
        CGFloat space = (width - BUTTON_WIDTH * 4) / 5;
        CGRect rect = CGRectMake(space, 8, BUTTON_WIDTH, BUTTON_WIDTH);
        btnKnowledge.frame = rect;
        rect.origin.x += space + BUTTON_WIDTH;
        btnBuyDevice.frame = rect;
        rect.origin.x += space + BUTTON_WIDTH;
        btnBuyBean.frame = rect;
        rect.origin.x += space + BUTTON_WIDTH;
        btnStore.frame = rect;
        rect.origin.x = space;
        rect.origin.y += 8 + BUTTON_WIDTH;
        btnNews.frame = rect;
        rect.origin.x += space + BUTTON_WIDTH;
        btnCoporate.frame = rect;
        rect.origin.x += space + BUTTON_WIDTH;
        btnAbout.frame = rect;
        
        [btnKnowledge layoutButtonWithEdgeInsetsStyle:MKButtonEdgeInsetsStyleTop imageTitleSpace:0];
        [btnBuyDevice layoutButtonWithEdgeInsetsStyle:MKButtonEdgeInsetsStyleTop imageTitleSpace:0];
        [btnBuyBean layoutButtonWithEdgeInsetsStyle:MKButtonEdgeInsetsStyleTop imageTitleSpace:0];
        [btnStore layoutButtonWithEdgeInsetsStyle:MKButtonEdgeInsetsStyleTop imageTitleSpace:0];
        [btnNews layoutButtonWithEdgeInsetsStyle:MKButtonEdgeInsetsStyleTop imageTitleSpace:0];
        [btnCoporate layoutButtonWithEdgeInsetsStyle:MKButtonEdgeInsetsStyleTop imageTitleSpace:0];
        [btnAbout layoutButtonWithEdgeInsetsStyle:MKButtonEdgeInsetsStyleTop imageTitleSpace:0];
        
        return cell1;
    } else if (indexPath.section == 1) {
        CGFloat w = (width - 8 * 3) / 2;
        btnFacebook.frame = CGRectMake(8, 5, w, 34);
        btnWeibo.frame = CGRectMake(8 + w + 8, 5, w, 34);
        return cell2;
    }
    
    return nil;
}
#undef BUTTON_WIDTH

@end
