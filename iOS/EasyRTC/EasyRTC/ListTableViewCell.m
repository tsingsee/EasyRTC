//
//  ListTableViewCell.m
//  VenusApp
//
//  Created by liyy on 2019/1/12.
//  Copyright © 2019年 easydarwin. All rights reserved.
//

#import "ListTableViewCell.h"

@interface ListTableViewCell()

@property (weak, nonatomic) IBOutlet UILabel *userName;
@property (weak, nonatomic) IBOutlet UILabel *userEmail;
@property (weak, nonatomic) IBOutlet UIProgressView *progress;
@property (weak, nonatomic) IBOutlet UIButton *muteBtn;
@property (weak, nonatomic) IBOutlet UILabel *energyLabel;

@property (strong, nonatomic) UserInfo *info;

@end

@implementation ListTableViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
}

- (void)configWithUserInfo:(UserInfo *)info {
    NSLog(@"user energy is : %@", @(info.energy));
    
    self.info = info;
    [self.muteBtn setImage:(info.isMute ? [UIImage imageNamed:@"speakeroff"] : [UIImage imageNamed:@"speakeron"]) forState:UIControlStateNormal];
    CGFloat userEnergy = (info.energy / 100.0) <= 100 ? (info.energy / 100.0) : 100.0;
    self.userName.text = info.displayName;
    self.userEmail.text = info.userEmail;
    self.progress.progress = userEnergy / 100.0;
    self.energyLabel.text = [NSString stringWithFormat:@"%u", (unsigned int)userEnergy];
    
    self.muteBtn.hidden = YES;
}

- (IBAction)muteBtnClicked:(id)sender {    
    [[NSNotificationCenter defaultCenter] postNotificationName:@"kMuteBtnClicked" object:self.info];
}

@end
