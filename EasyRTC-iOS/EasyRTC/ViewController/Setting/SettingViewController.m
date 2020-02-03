//
//  SettingViewController.m
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "SettingViewController.h"
#import "WebViewController.h"
#import "LoginInfoLocalData.h"
#import "LoginViewController.h"
#import "SettingViewModel.h"
#import "InfoModel.h"
#import "RequestKeyModel.h"
#import "SelectView.h"

@interface SettingViewController ()

@property (weak, nonatomic) IBOutlet UILabel *hardLabel;
@property (weak, nonatomic) IBOutlet UILabel *interfaceLabel;
@property (weak, nonatomic) IBOutlet UILabel *timeLabel;
@property (weak, nonatomic) IBOutlet UILabel *softLabel;
@property (weak, nonatomic) IBOutlet UILabel *userLabel;
@property (weak, nonatomic) IBOutlet UILabel *codeLabel;
@property (weak, nonatomic) IBOutlet UIButton *quitBtn;
@property (weak, nonatomic) IBOutlet SelectView *rightView;
@property (weak, nonatomic) IBOutlet UILabel *rightLabel;
@property (weak, nonatomic) IBOutlet UITextField *codeTF;

@property (nonatomic, strong) SettingViewModel *viewModel;

@end

@implementation SettingViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"SettingViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationItem.title = @"设置";
    
    EasyViewBorderRadius(_codeTF, 3, 1, UIColorFromRGB(0xdedede));
    
    [self.rightView setDefaultColor:EasyThemeColor];
    [self.rightView setSelectColor:EasyThemeColor];
//    [self.rightView setClickListener:^(id result) {
//        WebViewController *controller = [[WebViewController alloc] init];
//        controller.url = @"http://www.tsingsee.com";
//        [self basePushViewController:controller];
//    }];
    
    NSString *right = @"Copyright © 2020 www.tsingsee.com \nAll rights reversed.";
    NSMutableAttributedString *attrStr = [[NSMutableAttributedString alloc] initWithString:right];
    [attrStr addAttribute:NSFontAttributeName value:[UIFont systemFontOfSize:12.0f] range:NSMakeRange(0, right.length)];
//    [attrStr addAttribute:NSFontAttributeName value:[UIFont systemFontOfSize:12.0f] range:NSMakeRange(21, 15)];
    [attrStr addAttribute:NSForegroundColorAttributeName value:UIColorFromRGB(0xffffff) range:NSMakeRange(0, right.length)];
//    [attrStr addAttribute:NSForegroundColorAttributeName value:UIColorFromRGB(0xffffff) range:NSMakeRange(21, 15)];
    self.rightLabel.attributedText = attrStr;
    
    // 在iOS7之后，导航控制器中scrollView顶部会添加64的额外滚动区域
    self.automaticallyAdjustsScrollViewInsets = NO;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    NSString *token = self.loginModel.token;
    if (!token || [token isEqualToString:@""]) {
        [self.quitBtn setTitle:@"登录" forState:UIControlStateNormal];
    } else {
        [self.quitBtn setTitle:@"退出登录" forState:UIControlStateNormal];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    [self.viewModel.tokenSubject subscribeNext:^(RACCommand *command) {
        [self loginFirstWithCommend:command];
    }];
    
    [self.viewModel.infoCommand execute:nil];
    [self.viewModel.keyCommand execute:nil];
    
    [self.viewModel.infoSubject subscribeNext:^(id x) {
        if ([x isKindOfClass:[InfoModel class]]) {
            InfoModel *model = (InfoModel *)x;
            
            self.hardLabel.text = model.hardware;
            self.interfaceLabel.text = model.interfaceVersion;
            self.timeLabel.text = model.runningTime;
            self.softLabel.text = @"EasyRTC v2.0 - 202001101620";
            self.userLabel.text = model.validity;
        } else {
            [self showTextHubWithContent:x];
        }
    }];
    
    [self.viewModel.keySubject subscribeNext:^(id x) {
        if ([x isKindOfClass:[RequestKeyModel class]]) {
            RequestKeyModel *model = (RequestKeyModel *)x;
            self.codeLabel.text = model.requestKey;
        } else {
            [self showTextHubWithContent:x];
        }
    }];
    
    [self.viewModel.submitSubject subscribeNext:^(id x) {
        [self hideHub];
        
        if (!x) {
            [self showTextHubWithContent:@"提交成功"];
        } else {
            [self showTextHubWithContent:x];
        }
    }];
}

#pragma mark - click

- (IBAction)quit:(id)sender {
    
}

- (IBAction)submit:(id)sender {
    if ([_codeTF.text isEqualToString:@""]) {
        [self showTextHubWithContent:@"请输入激活码"];
        return;
    }
    
    [self showHubWithLoadText:@"提交中"];
    [self.viewModel.submitCommand execute:_codeTF.text];
}

#pragma mark - getter

- (SettingViewModel *) viewModel {
    if (!_viewModel) {
        _viewModel = [[SettingViewModel alloc] init];
    }
    
    return _viewModel;
}

@end
