//
//  LoginViewController.m
//  VenusApp
//
//  Created by leo on 2019/1/12.
//  Copyright © 2019年 easydarwin. All rights reserved.
//

#import "LoginViewController.h"

@interface LoginViewController ()

@property (weak, nonatomic) IBOutlet UITextField *nameTF;
@property (weak, nonatomic) IBOutlet UITextField *pwdTF;
@property (weak, nonatomic) IBOutlet UIButton *loginBtn;

@end

@implementation LoginViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"LoginViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.navigationController setNavigationBarHidden:YES];
    
    EasyViewBorderRadius(_loginBtn, 22, 0, [UIColor clearColor]);
    
    NSString *name = [[LoginInfoLocalData sharedInstance] gainName];
    NSString *pwd = [[LoginInfoLocalData sharedInstance] gainPWD];
    
    self.nameTF.text = name ? name : @"1008";
    self.pwdTF.text = pwd ? pwd : @"1111";
}

- (IBAction)loginBtnClicked:(id)sender {
    if ([self.nameTF.text isEqualToString:@""]) {
        [self showTextHubWithContent:@"请输入用户名或账号"];
        return;
    }
    
    if ([self.pwdTF.text isEqualToString:@""]) {
        [self showTextHubWithContent:@"请输入密码"];
        return;
    }
    
    [[LoginInfoLocalData sharedInstance] saveName:self.nameTF.text psw:self.pwdTF.text];
    
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    
}

#pragma mark - StatusBar

- (UIStatusBarStyle) preferredStatusBarStyle {
    return UIStatusBarStyleDefault;
}

@end
