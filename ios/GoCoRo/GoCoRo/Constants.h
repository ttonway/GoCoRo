//
//  Constants.h
//  petkeeper
//
//  Created by ttonway on 14-8-6.
//  Copyright (c) 2014年 com.wcare.apple.petkeeper. All rights reserved.
//

#import <UIKit/UIKit.h>


extern NSString * const WEB_HOST;

@interface Constants : NSObject

@end


@interface UIColor (Custom)

+ (UIColor *)windowBackgroundColor;
+ (UIColor *)customOrangeColor;
+ (UIColor *)lightGrayTextColor;

@end



@interface Utils : NSObject

+ (NSString *)documentPath:(NSString *)fileName;

+ (UIImage *)imageFromColor:(UIColor *)color;
+ (UIImage *)imageFromColor:(UIColor *)color size:(CGSize)size;

+ (void)showAlert:(NSString *)message;

+ (NSString *)getUUID;

+ (NSString *)formatAgeFromBirthday:(NSDate *)birthday;
+ (NSString *)formatTimeIntervalSinceNow:(NSDate *)date;
+ (NSString *)formatDate:(NSDate *)date;
+ (NSString *)formatSeconds:(NSInteger)seconds;

+ (BOOL)isValidateEmail:(NSString *)email;

+ (NSString *)appVersion;
+ (NSComparisonResult)compareVersion:(NSString *)local to:(NSString *)remote;

@end

@interface UIImage (Custom)

+ (UIImage *)checkmark;
+ (UIImage *)uncheckmark;

- (UIImage *)imageLimitedBy:(CGSize)limit;
- (UIImage *)imageByOverlay:(UIImage *)overlay;
- (UIImage *)resizedImageForSize:(CGSize)size;

@end


typedef NS_ENUM(NSUInteger, MKButtonEdgeInsetsStyle) {
    MKButtonEdgeInsetsStyleTop, // image在上，label在下
    MKButtonEdgeInsetsStyleLeft, // image在左，label在右
    MKButtonEdgeInsetsStyleBottom, // image在下，label在上
    MKButtonEdgeInsetsStyleRight // image在右，label在左
};

@interface UIButton (ImageTitleSpacing)

/**
 *  设置button的titleLabel和imageView的布局样式，及间距
 *
 *  @param style titleLabel和imageView的布局样式
 *  @param space titleLabel和imageView的间距
 */
- (void)layoutButtonWithEdgeInsetsStyle:(MKButtonEdgeInsetsStyle)style imageTitleSpace:(CGFloat)space;

@end

