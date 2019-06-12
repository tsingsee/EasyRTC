//
//  ListViewController.m
//  venus
//
//  Created by liyy on 2019/1/12.
//  Copyright © 2019年 easydarwin. All rights reserved.
//

#import "ListViewController.h"
#import "ListTableViewCell.h"
#import <EasyRTC/EasyRTC.h>

@interface ListViewController () <UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITableView *userTable;

@end

@implementation ListViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationController.navigationBar.hidden = NO;
    // Do any additional setup after loading the view.
}

- (IBAction)backBtnClicked:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)updateTable {
    [self.userTable reloadData];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 55.0;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.userList count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UserInfo *user = [self.userList objectAtIndex:indexPath.row];
    
    ListTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ListTableViewCell" forIndexPath:indexPath];
    [cell configWithUserInfo:user];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    return cell;
}

@end
