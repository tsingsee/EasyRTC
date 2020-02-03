//
//  WebViewController.h
//  EasyRTMP
//
//  Created by mac on 2018/7/9.
//  Copyright © 2018年 phylony. All rights reserved.
//

#import "BaseViewController.h"
#import <WebKit/WebKit.h>

@interface WebViewController : BaseViewController<UIWebViewDelegate, WKUIDelegate, WKNavigationDelegate>

@property (nonatomic, copy) NSString *url;

// WKNavigationDelegate主要处理一些跳转、加载处理操作，WKUIDelegate主要处理JS脚本，确认框，警告框等
@property (retain, nonatomic) WKWebView *wkWebView;
@property (nonatomic, strong) WKWebViewConfiguration *wkConfig;
@property (nonatomic, strong) UIProgressView *progressView;

@end
