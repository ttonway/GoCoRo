//
//  Constants.m
//  petkeeper
//
//  Created by ttonway on 14-8-6.
//  Copyright (c) 2014年 com.wcare.apple.petkeeper. All rights reserved.
//

#import "Constants.h"

#import <SystemConfiguration/CaptiveNetwork.h>
#include <sys/sysctl.h>
#include <net/if.h>
#include <net/if_dl.h>


NSString * const WEB_HOST = @"http://beta.wcare.cn:3003/";


@implementation Constants

@end



@implementation UIColor (Custom)

+ (UIColor *)windowBackgroundColor {
    return [UIColor colorWithRed:51.0f/255.0f green:51.0f/255.0f blue:51.0f/255.0f alpha:1.0f];
}

+ (UIColor *)customOrangeColor {
    return [UIColor colorWithRed:250.0f/255.0f green:90.0f/255.0f blue:68.0f/255.0f alpha:1.0f];
}

+ (UIColor *)lightGrayTextColor {
    return [UIColor colorWithRed:139.0f/255.0f green:135.0f/255.0f blue:134.0f/255.0f alpha:1.0f];
}

@end


@implementation Utils

+ (NSString *)documentPath:(NSString *)fileName {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    return [documentsDirectory stringByAppendingPathComponent:fileName];
}


+ (UIImage *)imageFromColor:(UIColor *)color {
    return [Utils imageFromColor:color size:CGSizeMake(1, 1)];
}

+ (UIImage *)imageFromColor:(UIColor *)color size:(CGSize)size {
    CGRect rect = CGRectMake(0, 0, size.width, size.height);
    UIGraphicsBeginImageContext(rect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetFillColorWithColor(context, [color CGColor]);
    CGContextFillRect(context, rect);
    UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return img;
}

+ (void)showAlert:(NSString *)message
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil message:message delegate:nil cancelButtonTitle:NSLocalizedString(@"btn_ok", nil) otherButtonTitles:nil];
    [alert show];
}


+ (NSString *)getUUID {
    CFUUIDRef puuid = CFUUIDCreate(nil);
    CFStringRef uuidString = CFUUIDCreateString(nil, puuid);
    NSString * result = (__bridge NSString *)CFStringCreateCopy(NULL, uuidString);
    CFRelease(puuid);
    CFRelease(uuidString);
    return result;
}

+ (NSString *)formatAgeFromBirthday:(NSDate *)birthday
{
    NSTimeInterval seconds = -[birthday timeIntervalSinceNow];
    if (seconds < 0) {
        return @"error";
    }
    int days = seconds / 60 / 60 / 24;
    if (days < 30) {
        return [NSString stringWithFormat:@"%d days old", days];
    } else {
        int years = days / 365;
        if (years == 0) {
            int months = days / 30;
            return [NSString stringWithFormat:@"%d months old", months];
        } else {
            return [NSString stringWithFormat:@"%d years old", years];
        }
    }
}

+ (NSString *)formatTimeIntervalSinceNow:(NSDate *)date
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-M-d HH:mm:ss"];
    NSTimeInterval seconds = -[date timeIntervalSinceNow];
    if (seconds < 0) {
        return [dateFormatter stringFromDate:date];
    }
    if (seconds < 60) {
        return NSLocalizedString(@"right_now", nil);
    }
    NSTimeInterval minutes = seconds / 60;
    if (minutes < 60) {
        return [NSString stringWithFormat:NSLocalizedString(@"x_minutes_before", nil), (int)minutes];
    }
    NSTimeInterval hours = minutes / 60;
    if (hours < 24) {
        return [NSString stringWithFormat:NSLocalizedString(@"x_hours_before", nil), (int)hours];
    }
    NSTimeInterval days = hours / 24;
    if (days < 30) {
        return [NSString stringWithFormat:NSLocalizedString(@"x_days_before", nil), (int)days];
    }
    
    return [dateFormatter stringFromDate:date];
}

+ (NSString *)formatDate:(NSDate *)date
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-M-d HH:mm:ss"];
    return [dateFormatter stringFromDate:date];
}

+ (NSString *)formatSeconds:(NSInteger)seconds {
    if (seconds == 0) {
        return @"0";
    }
    
    NSInteger hour = seconds / 3600;
    NSInteger min = seconds / 60 - hour * 60;
    NSInteger sec = seconds - min * 60 - hour * 3600;
    return [NSString stringWithFormat:@"%ld:%02ld", (long)min, (long)sec];
}

+ (BOOL)isValidateEmail:(NSString *)email {
    NSString *emailRegex = @"[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
    NSPredicate *emailTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", emailRegex];
    return [emailTest evaluateWithObject:email];
}

+ (NSString *)appVersion {
    NSDictionary* infoDict =[[NSBundle mainBundle] infoDictionary];
    NSString* versionNum =[infoDict objectForKey:@"CFBundleShortVersionString"];
    return versionNum;
}


+ (NSComparisonResult)compareVersion:(NSString *)local to:(NSString *)remote
{
    NSCharacterSet *set = [NSCharacterSet characterSetWithCharactersInString:@"V"];
    local = [local stringByTrimmingCharactersInSet:set];
    remote = [remote stringByTrimmingCharactersInSet:set];
    
    NSArray *array1 = [local componentsSeparatedByString:@"."];
    NSArray *array2 = [remote componentsSeparatedByString:@"."];
    
    for (NSInteger i = 0; i < array1.count && i < array2.count; i++) {
        int lv = [[array1 objectAtIndex:i] intValue];
        int rv = [[array2 objectAtIndex:i] intValue];
        if (rv > lv) {
            return NSOrderedAscending;
        } else if (rv < lv) {
            return NSOrderedDescending;
        }
    }
    
    return array2.count == array1.count ? NSOrderedSame : (array2.count > array1.count ? NSOrderedAscending : NSOrderedDescending);
}

@end


@implementation UIImage (Custom)

+ (UIImage *)checkmark {
    return [UIImage imageNamed:@"checkmark"];
}
+ (UIImage *)uncheckmark {
    return [Utils imageFromColor:[UIColor clearColor] size:CGSizeMake(16, 16)];
}

- (UIImage *)imageLimitedBy:(CGSize)limit
{
    CGSize size = self.size;
    if (size.width > limit.width) {
        size.height = size.height * limit.width / size.width;
        size.width = limit.width;
    }
    if (size.height > limit.height) {
        size.width = size.width * limit.height / size.height;
        size.height = limit.height;
    }
    UIGraphicsBeginImageContext(size);
    [self drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return scaledImage;
}

- (UIImage *)imageByOverlay:(UIImage *)overlay
{
    CGSize size = self.size;
    CGSize overlaySize = overlay.size;
    
    UIGraphicsBeginImageContext(size);
    
    [self drawInRect:CGRectMake(0, 0, size.width, size.height)];
    [overlay drawInRect:CGRectMake((size.width - overlaySize.width) / 2, (size.height - overlaySize.height) / 2, overlaySize.width, overlaySize.height)];
    
    UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return newImage;
}

- (UIImage *)resizedImageForSize:(CGSize)size
{
    UIGraphicsBeginImageContextWithOptions(size, NO, self.scale);
    
    [self drawInRect:CGRectMake(0, 0, size.width, size.height)];
    
    UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return newImage;
}

@end




@implementation UIButton (ImageTitleSpacing)

- (void)layoutButtonWithEdgeInsetsStyle:(MKButtonEdgeInsetsStyle)style
                        imageTitleSpace:(CGFloat)space
{
    // 1. 得到imageView和titleLabel的宽、高
    CGFloat imageWith = self.imageView.frame.size.width;
    CGFloat imageHeight = self.imageView.frame.size.height;
    
    CGFloat labelWidth = 0.0;
    CGFloat labelHeight = 0.0;
    if ([UIDevice currentDevice].systemVersion.floatValue >= 8.0) {
        // 由于iOS8中titleLabel的size为0，用下面的这种设置
        labelWidth = self.titleLabel.intrinsicContentSize.width;
        labelHeight = self.titleLabel.intrinsicContentSize.height;
    } else {
        labelWidth = self.titleLabel.frame.size.width;
        labelHeight = self.titleLabel.frame.size.height;
    }
    
    // 2. 声明全局的imageEdgeInsets和labelEdgeInsets
    UIEdgeInsets imageEdgeInsets = UIEdgeInsetsZero;
    UIEdgeInsets labelEdgeInsets = UIEdgeInsetsZero;
    
    // 3. 根据style和space得到imageEdgeInsets和labelEdgeInsets的值
    switch (style) {
        case MKButtonEdgeInsetsStyleTop:
        {
            imageEdgeInsets = UIEdgeInsetsMake(-labelHeight-space/2.0, 0, 0, -labelWidth);
            labelEdgeInsets = UIEdgeInsetsMake(0, -imageWith, -imageHeight-space/2.0, 0);
        }
            break;
        case MKButtonEdgeInsetsStyleLeft:
        {
            imageEdgeInsets = UIEdgeInsetsMake(0, -space/2.0, 0, space/2.0);
            labelEdgeInsets = UIEdgeInsetsMake(0, space/2.0, 0, -space/2.0);
        }
            break;
        case MKButtonEdgeInsetsStyleBottom:
        {
            imageEdgeInsets = UIEdgeInsetsMake(0, 0, -labelHeight-space/2.0, -labelWidth);
            labelEdgeInsets = UIEdgeInsetsMake(-imageHeight-space/2.0, -imageWith, 0, 0);
        }
            break;
        case MKButtonEdgeInsetsStyleRight:
        {
            imageEdgeInsets = UIEdgeInsetsMake(0, labelWidth+space/2.0, 0, -labelWidth-space/2.0);
            labelEdgeInsets = UIEdgeInsetsMake(0, -imageWith-space/2.0, 0, imageWith+space/2.0);
        }
            break;
        default:
            break;
    }
    // 4. 赋值
    self.titleEdgeInsets = labelEdgeInsets;
    self.imageEdgeInsets = imageEdgeInsets;
}

@end
