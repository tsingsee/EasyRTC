//
//  CalendarViewController.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/27.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "CalendarViewController.h"
#import "JTCalendar.h"
#import "RecordViewModel.h"
#import "DateUtil.h"

@interface CalendarViewController ()<JTCalendarDelegate>

@property (weak, nonatomic) IBOutlet JTCalendarMenuView *calendarMenuView;
@property (weak, nonatomic) IBOutlet JTCalendarWeekDayView *weekDayView;
@property (weak, nonatomic) IBOutlet JTVerticalCalendarView *calendarContentView;

@property (strong, nonatomic) JTCalendarManager *calendarManager;

@property (nonatomic, strong) RecordViewModel *viewModel;

// 有事件的时间数组
@property (strong, nonatomic) NSMutableDictionary *eventsByDate;

// 选中的时间数组，添加时间到这个数组里则可以显示红圈，也就是选中状态
@property (strong, nonatomic) NSDate *dateSelected;

@end

@implementation CalendarViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Record" bundle:nil] instantiateViewControllerWithIdentifier:@"CalendarViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationItem.title = @"选择日期";
    
    _calendarManager = [JTCalendarManager new];
    _calendarManager.delegate = self;
    
    _calendarManager.settings.pageViewHaveWeekDaysView = NO;
    _calendarManager.settings.pageViewNumberOfWeeks = 0; // Automatic
    
    _weekDayView.manager = _calendarManager;
    [_weekDayView reload];
    
    [_calendarManager setMenuView:_calendarMenuView];
    [_calendarManager setContentView:_calendarContentView];
    [_calendarManager setDate:[NSDate date]];
    
    _calendarMenuView.scrollView.scrollEnabled = NO; // Scroll not supported with JTVerticalCalendarView
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - CalendarManager delegate

// 改变日历的代理方法
- (void)calendar:(JTCalendarManager *)calendar prepareDayView:(JTCalendarDayView *)dayView {
    dayView.hidden = NO;
    
    // Hide if from another month
    if([dayView isFromAnotherMonth]) {
        dayView.hidden = YES;
    } else if([_calendarManager.dateHelper date:[NSDate date] isTheSameDayThan:dayView.date]) {
        // 日期为今天的样式
        dayView.circleView.hidden = NO;
        dayView.circleView.backgroundColor = [UIColor blueColor];
        dayView.dotView.backgroundColor = [UIColor redColor];
        dayView.textLabel.textColor = [UIColor whiteColor];
    } else if(_dateSelected && [_calendarManager.dateHelper date:_dateSelected isTheSameDayThan:dayView.date]) {
        // 日期为选中模式的样式
        dayView.circleView.hidden = NO;
        dayView.circleView.backgroundColor = [UIColor redColor];
        dayView.dotView.backgroundColor = [UIColor whiteColor];
        dayView.textLabel.textColor = [UIColor whiteColor];
    } else if(![_calendarManager.dateHelper date:_calendarContentView.date isTheSameMonthThan:dayView.date]) {
        // 这个为本月内第一个星期里上月日期的样式
        dayView.circleView.hidden = YES;
        dayView.dotView.backgroundColor = [UIColor redColor];
        dayView.textLabel.textColor = UIColorFromRGB(0xf5f5f5);
    } else {
        // 这个为下月内第一个星期里今天的样式
        dayView.circleView.hidden = YES;
        dayView.dotView.backgroundColor = [UIColor redColor];
        dayView.textLabel.textColor = UIColorFromRGB(0xf5f5f5);
    }
    
    // 日期有事件则显示个小红点，没有就不显示
    if ([self haveEventForDay:dayView.date]) {
        dayView.dotView.hidden = NO;
        dayView.textLabel.textColor = UIColorFromRGB(0x111111);
    } else {
        dayView.dotView.hidden = YES;
        dayView.textLabel.textColor = UIColorFromRGB(0x999999);
    }
}

- (void)calendar:(JTCalendarManager *)calendar didTouchDayView:(JTCalendarDayView *)dayView {
    if (![self haveEventForDay:dayView.date]) {
        return;
    }
    
    NSTimeZone *zone = [NSTimeZone systemTimeZone];
    NSInteger interval = [zone secondsFromGMTForDate:dayView.date];
    NSDate *localeDate = [dayView.date dateByAddingTimeInterval: interval];
    
    self.dateSelected = localeDate;
    [self.subject sendNext:self.dateSelected];
    
    [self.navigationController popViewControllerAnimated:YES];
    
//    // Animation for the circleView
//    dayView.circleView.transform = CGAffineTransformScale(CGAffineTransformIdentity, 0.1, 0.1);
//    [UIView transitionWithView:dayView
//                      duration:.3
//                       options:0
//                    animations:^{
//                        dayView.circleView.transform = CGAffineTransformIdentity;
//                        [self.calendarManager reload];
//                    } completion:nil];
//
//
//    // Don't change page in week mode because block the selection of days in first and last weeks of the month
//    if (_calendarManager.settings.weekModeEnabled) {
//        return;
//    }
//
//    // Load the previous or next page if touch a day from another month
//    if (![_calendarManager.dateHelper date:_calendarContentView.date isTheSameMonthThan:dayView.date]) {
//        if ([_calendarContentView.date compare:dayView.date] == NSOrderedAscending) {
//            [_calendarContentView loadNextPageWithAnimation];
//        } else {
//            [_calendarContentView loadPreviousPageWithAnimation];
//        }
//    }
}

#pragma mark - Fake data

// Used only to have a key for _eventsByDate
- (NSDateFormatter *)dateFormatter {
    static NSDateFormatter *dateFormatter;
    if(!dateFormatter){
        dateFormatter = [NSDateFormatter new];
        dateFormatter.dateFormat = @"dd-MM-yyyy";
    }
    
    return dateFormatter;
}

- (BOOL)haveEventForDay:(NSDate *)date {
    NSString *key = [[self dateFormatter] stringFromDate:date];
    
    if (_eventsByDate[key] && [_eventsByDate[key] count] > 0) {
        return YES;
    }
    
    return NO;
}

/**
 标记当月每一天是否有录像, 0 - 没有录像, 1 - 有录像

 @param res res
 */
- (void)createRandomEvents:(NSString *)res {
    _eventsByDate = [NSMutableDictionary new];
    
    // 把每个月的每一天为1的都标记出来
    for (int i = 0; i < res.length; i++) {
        int ch = [[res substringWithRange:NSMakeRange(i, 1)] intValue];
        
        if (ch == 1) {
            NSString *period = [DateUtil dateYYYYMM:self.chooseMonth];
            NSString *dateStr = [NSString stringWithFormat:@"%@%02d", period, i+1];
            NSDate *randomDate = [DateUtil dateFormatYYYYMMDD:dateStr];
            
            // Use the date as key for eventsByDate
            NSString *key = [[self dateFormatter] stringFromDate:randomDate];
            
            if(!_eventsByDate[key]) {
                _eventsByDate[key] = [NSMutableArray new];
            }
            
            [_eventsByDate[key] addObject:randomDate];
        }
    }
    
    [_calendarManager setDate:[NSDate date]];
}

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    [self showHub];
    
    self.viewModel.chooseMonth = self.chooseMonth;
    [self.viewModel.querymonthlyCommand execute:self.recordId];
    
    [self.viewModel.querymonthlySubject subscribeNext:^(NSString *res) {
        [self hideHub];
        
        [self createRandomEvents:res];
    }];
    
    [self.viewModel.querymonthlySubject subscribeError:^(NSError *error) {
        [self hideHub];
        [self showTextHubWithContent:error.domain];
    }];
}

- (RecordViewModel *) viewModel {
    if (!_viewModel) {
        _viewModel = [[RecordViewModel alloc] init];
    }
    
    return _viewModel;
}

- (RACSubject *)subject {
    if (!_subject) {
        _subject = [[RACSubject alloc] init];
    }
    
    return _subject;
}

@end
