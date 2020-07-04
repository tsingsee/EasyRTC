//
//  RegisterViewController.m
//  EasyRTC
//
//  Created by liyy on 2020/3/7.
//  Copyright © 2020 easydarwin. All rights reserved.
//

#import "RegisterViewController.h"

@interface RegisterViewController ()

@property (weak, nonatomic) IBOutlet UITextField *nameTF;
@property (weak, nonatomic) IBOutlet UITextField *pwdTF;
@property (weak, nonatomic) IBOutlet UIButton *loginBtn;

@end

@implementation RegisterViewController

- (instancetype) initWithStoryborad {
    return [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"RegisterViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title = @"注册";
    
    EasyViewBorderRadius(_loginBtn, 22, 0, [UIColor clearColor]);
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
    RegisterViewController *vc = [[RegisterViewController alloc] initWithStoryborad];
    [self basePushViewController:vc];
}

#pragma mark - StatusBar

- (UIStatusBarStyle) preferredStatusBarStyle {
    return UIStatusBarStyleDefault;
}

@end
