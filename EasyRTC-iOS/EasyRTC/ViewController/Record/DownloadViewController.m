//
//  DownloadViewController.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/10/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "DownloadViewController.h"
#import "DownloadViewModel.h"

@interface DownloadViewController ()

@property (weak, nonatomic) IBOutlet UILabel *label;
@property (weak, nonatomic) IBOutlet UISlider *slider;

@property (nonatomic, strong) DownloadViewModel *viewModel;

@end

@implementation DownloadViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Record" bundle:nil] instantiateViewControllerWithIdentifier:@"DownloadViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = UIColorFromRGBA(0x000000, 0.88);
    
    self.label.text = @"0.0%";
    
    [self.slider setEnabled:NO];
    self.slider.maximumValue = 100;
    self.slider.minimumValue = 0;
    
    self.viewModel.channel = self.channel;
    self.viewModel.curRecord = self.curRecord;
    [self.viewModel downLoadVedio];
}

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    [self.viewModel.downloadSubject subscribeNext:^(NSNumber *pro) {
        if (pro.floatValue >= 100) {
            [self showTextHubWithContent:@"下载已完成"];
            [self dismissViewControllerAnimated:YES completion:nil];
        }
        
        self.slider.value = pro.floatValue;
        self.label.text = [NSString stringWithFormat:@"%.1f%%", pro.floatValue];
    }];
}

- (DownloadViewModel *) viewModel {
    if (!_viewModel) {
        _viewModel = [[DownloadViewModel alloc] init];
    }
    
    return _viewModel;
}

@end
