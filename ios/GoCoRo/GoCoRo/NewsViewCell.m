//
//  NewsViewCell.m
//  GoCoRo
//
//  Created by ttonway on 2017/4/25.
//  Copyright © 2017年 wcare. All rights reserved.
//

#import "NewsViewCell.h"

#import "Constants.h"

@implementation NewsViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    
    self.backgroundColor = [UIColor clearColor];
    
    self.timeLabel.textColor = [UIColor lightGrayTextColor];
    self.descLabel.textColor = [UIColor lightGrayTextColor];
    
    self.cardView.backgroundColor = [UIColor whiteColor];
    self.cardView.layer.cornerRadius = 8;
    self.cardView.layer.masksToBounds = YES;
    
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
}

- (void)setHighlighted:(BOOL)highlighted animated:(BOOL)animated {
    [super setHighlighted:highlighted animated:animated];
    
    self.cardView.backgroundColor = highlighted ? [UIColor lightGrayColor] : [UIColor whiteColor];
}

@end
