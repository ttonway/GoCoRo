//
//  NewsViewCell.h
//  GoCoRo
//
//  Created by ttonway on 2017/4/25.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface NewsViewCell : UITableViewCell

@property (nonatomic) IBOutlet UIView *cardView;
@property (nonatomic) IBOutlet UILabel *titleLabel;
@property (nonatomic) IBOutlet UILabel *timeLabel;
@property (nonatomic) IBOutlet UIImageView *posterImageView;
@property (nonatomic) IBOutlet UILabel *descLabel;

@end
