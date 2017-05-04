//
//  NewsListViewController.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/25.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "NewsListViewController.h"

#import <SDWebImage/UIImageView+WebCache.h>
#import "WebClient.h"
#import "NewsViewCell.h"
#import "WebViewController.h"

@interface NewsListViewController () {
    NewsViewCell *tempCell;
    
    NSDateFormatter *dateFormatter;
}

@property (nonatomic) NSMutableArray *newsArray;

@end

static NSString *CellIdentifier = @"NewsIdentifier";

@implementation NewsListViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"activity_news", nil);
    self.view.backgroundColor = [UIColor windowBackgroundColor];
    
    dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = NSDateFormatterMediumStyle;
    dateFormatter.timeStyle = NSDateFormatterShortStyle;
    
    tempCell = [[[NSBundle mainBundle] loadNibNamed:@"NewsViewCell" owner:self options:nil] lastObject];
    UINib *nib = [UINib nibWithNibName:@"NewsViewCell" bundle:nil];
    [self.tableView registerNib:nib forCellReuseIdentifier:CellIdentifier];
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.tableView.estimatedRowHeight = 100.0;
    self.tableView.rowHeight = UITableViewAutomaticDimension;
    
    self.newsArray = [NSMutableArray array];
    
    WebClient *client = [WebClient sharedInstance];
    NSURL *URL = [NSURL URLWithString:@"knowledge/list" relativeToURL:client.hostURL];
    NSURLRequest *request = [NSURLRequest requestWithURL:URL];
    
    __weak NewsListViewController *weakSelf = self;
    NSURLSessionDataTask *dataTask = [client.sessionManager dataTaskWithRequest:request completionHandler:^(NSURLResponse *response, id responseObject, NSError *error) {
        if (error) {
            NSLog(@"Get %@ Error: %@", URL, error);
        } else {
            //NSLog(@"%@ %@", response, responseObject);
            if ([responseObject isKindOfClass:[NSArray class]]) {
                [weakSelf.newsArray setArray:responseObject];
                [weakSelf.tableView reloadData];
            }
        }
    }];
    [dataTask resume];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.newsArray.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    CGFloat w = tableView.bounds.size.width;
    
    [self configCell:tempCell forIndexPath:indexPath];
    
    [tempCell setNeedsUpdateConstraints];
    [tempCell updateConstraintsIfNeeded];
    [tempCell setNeedsLayout];
    [tempCell layoutIfNeeded];
//    CGSize size = [tempCell.contentView systemLayoutSizeFittingSize:UILayoutFittingCompressedSize];
    
    CGSize fittingSize = UILayoutFittingCompressedSize;
    fittingSize.width = w;
    CGSize size = [tempCell.contentView systemLayoutSizeFittingSize:fittingSize withHorizontalFittingPriority:UILayoutPriorityRequired verticalFittingPriority:UILayoutPriorityDefaultLow];
//    NSLog(@"%@, %@", indexPath, NSStringFromCGSize(size));
    return size.height;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NewsViewCell *cell = (NewsViewCell *)[tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
    
    
    [self configCell:cell forIndexPath:indexPath];
    [cell setNeedsUpdateConstraints];
    [cell updateConstraintsIfNeeded];
    
    return cell;
}

- (void)configCell:(NewsViewCell *)cell forIndexPath:(NSIndexPath *)indexPath {
    NSDictionary *dic = [self.newsArray objectAtIndex:indexPath.row];
    
    cell.titleLabel.text = dic[@"title"];
    cell.descLabel.text = dic[@"description"];
    
    NSNumber *ms = dic[@"createdAt"];
    NSDate *date = [NSDate dateWithTimeIntervalSince1970:[ms longLongValue] / 1000.f];
    cell.timeLabel.text = [dateFormatter stringFromDate:date];
    
    NSString *poster = dic[@"posterUrl"];
    if (poster.length > 0) {
        [cell.posterImageView sd_setImageWithURL:[NSURL URLWithString:poster] completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
            NSLog(@"%@ %@", imageURL, error);
        }];
    } else {
        cell.posterImageView.image = nil;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    NSDictionary *dic = [self.newsArray objectAtIndex:indexPath.row];
    
    WebViewController *controller = [[WebViewController alloc] init];
    controller.title = dic[@"title"];
    controller.URL = [NSURL URLWithString:dic[@"url"]];
    [self.navigationController pushViewController:controller animated:YES];
}

@end
