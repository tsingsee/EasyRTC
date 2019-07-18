//
//  LoginViewController.m
//  VenusApp
//
//  Created by leo on 2019/1/12.
//  Copyright © 2019年 easydarwin. All rights reserved.
//

#import "LoginViewController.h"
#import "MainViewController.h"

@interface LoginViewController ()

@property (weak, nonatomic) IBOutlet UITextField *serverTF;
@property (weak, nonatomic) IBOutlet UITextField *nameTF;
@property (weak, nonatomic) IBOutlet UITextField *pwdTF;
@property (weak, nonatomic) IBOutlet UITextField *roomTF;

@property (weak, nonatomic) IBOutlet UIButton *loginBtn;

@end

@implementation LoginViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleDefault;
    
    [self initView];
    
    [self.navigationController setNavigationBarHidden:YES];
}

- (void)initView {
    self.loginBtn.layer.cornerRadius = 5.0;
    self.loginBtn.layer.shadowColor = [UIColor blackColor].CGColor;
    self.loginBtn.layer.shadowOffset = CGSizeMake(0, 2);
    self.loginBtn.layer.shadowOpacity = 0.8;
    self.loginBtn.layer.shadowRadius = 2;
}

- (IBAction)loginBtnClicked:(id)sender {
    Options *options = [[Options alloc] init];
    options.username = self.nameTF.text.length > 0 ? self.nameTF.text : @"1009";
    options.password = self.pwdTF.text.length > 0 ? self.pwdTF.text : @"iOS@easydss.com";
    options.roomNumber = self.roomTF.text.length > 0 ? self.roomTF.text : @"3581";
    options.displayName = options.username;
    options.userEmail = [NSString stringWithFormat:@"%@@easydarwin.org", options.username];
    
    options.serverAddress = self.serverTF.text.length > 0 ? self.serverTF.text : @"easyrtc.easydss.com";
    
    UIStoryboard *mainStoryboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    MainViewController *mainVC  = [mainStoryboard instantiateViewControllerWithIdentifier:@"MainViewController"];
    mainVC.options = options;
    [self.navigationController pushViewController:mainVC animated:YES];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

@end
