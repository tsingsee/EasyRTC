//
//  ListViewController.h
//  venus
//
//  Created by leo on 2019/1/12.
//  Copyright © 2019年 easydarwin. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ListViewController : UIViewController

@property (atomic, strong) NSMutableArray *userList;

- (void)updateTable;

@end
