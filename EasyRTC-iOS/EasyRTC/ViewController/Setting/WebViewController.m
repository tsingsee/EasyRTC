//
//  WebViewController.m
//  EasyRTMP
//
//  Created by mac on 2018/7/9.
//  Copyright © 2018年 phylony. All rights reserved.
//

#import "WebViewController.h"
#import "Masonry.h"

@interface WebViewController ()

@end

@implementation WebViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationItem.title = self.title;
    [self addView];
}

- (void) dealloc {
    if ([self isViewLoaded]) {
        [self.wkWebView removeObserver:self forKeyPath:NSStringFromSelector(@selector(estimatedProgress))];
    }
    
    // if you have set either WKWebView delegate also set these to nil here
    [self.wkWebView setNavigationDelegate:nil];
    [self.wkWebView setUIDelegate:nil];
}

#pragma mark - getter/setter

- (WKWebView *) wkWebView {
    if (!_wkWebView) {
        CGFloat top = 0;
        CGRect frame = CGRectMake(0, top, EasyScreenWidth, EasyScreenHeight - top);
        _wkWebView = [[WKWebView alloc] initWithFrame:frame configuration:self.wkConfig];
        _wkWebView.navigationDelegate = self;
        _wkWebView.UIDelegate = self;
        [self.view addSubview:_wkWebView];
    }
    return _wkWebView;
}

- (WKWebViewConfiguration *)wkConfig {
    if (!_wkConfig) {
        _wkConfig = [[WKWebViewConfiguration alloc] init];
        _wkConfig.allowsInlineMediaPlayback = YES;
        _wkConfig.allowsPictureInPictureMediaPlayback = YES;
    }
    return _wkConfig;
}

#pragma mark - private method

- (void) addView {
    // 初始化progressView
    CGFloat top = EasyBarHeight + EasyNavHeight;
    CGRect frame = CGRectMake(0, top, EasyScreenWidth, 2);
    self.progressView = [[UIProgressView alloc] initWithFrame:frame];
    self.progressView.tintColor = UIColorFromRGB(0xff0000);
    //设置进度条的高度，下面这句代码表示进度条的宽度变为原来的1倍，高度变为原来的1.5倍.
    self.progressView.transform = CGAffineTransformMakeScale(1.0f, 1.5f);
    [self.view addSubview:self.progressView];
    
    // 添加KVO，WKWebView有一个属性estimatedProgress，就是当前网页加载的进度，所以监听这个属性。
    [self.wkWebView addObserver:self forKeyPath:NSStringFromSelector(@selector(estimatedProgress)) options:NSKeyValueObservingOptionNew context:nil];
    
    [self startLoad];
}

- (void)startLoad {
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:self.url]];
    request.timeoutInterval = 15.0f;
    // 添加缓存策略
    request.cachePolicy = NSURLRequestUseProtocolCachePolicy;
    
    [self.wkWebView loadRequest:request];
}

//在监听方法中获取网页加载的进度，并将进度赋给progressView.progress
- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSString *,id> *)change context:(void *)context {
    if ([keyPath isEqualToString:@"estimatedProgress"]) {
        self.progressView.progress = self.wkWebView.estimatedProgress;
        if (self.progressView.progress == 1) {
            // 添加一个简单的动画，将progressView的Height变为1.4倍，在开始加载网页的代理中会恢复为1.5倍
            [UIView animateWithDuration:0.25f delay:0.3f options:UIViewAnimationOptionCurveEaseOut animations:^{
                self.progressView.transform = CGAffineTransformMakeScale(1.0f, 1.4f);
            } completion:^(BOOL finished) {
                self.progressView.hidden = YES;
            }];
        }
    } else {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

#pragma mark - WKNavigationDelegate

// 页面开始加载时调用
- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation {
    //开始加载网页时展示出progressView
    self.progressView.hidden = NO;
    //开始加载网页的时候将progressView的Height恢复为1.5倍
    self.progressView.transform = CGAffineTransformMakeScale(1.0f, 1.5f);
    //防止progressView被网页挡住
    [self.view bringSubviewToFront:self.progressView];
    
    NSString *path= [webView.URL absoluteString];
    NSString * newPath = [path lowercaseString];
    
    if ([newPath hasPrefix:@"sms:"] || [newPath hasPrefix:@"tel:"]) {
        NSURL *url = [NSURL URLWithString:newPath];
        if (@available(iOS 10.0, *)) {
            // 大于等于10.0系统使用此openURL方法
            [[UIApplication sharedApplication] openURL:url options:@{} completionHandler:nil];
        } else {
            [[UIApplication sharedApplication] openURL:url];
        }
        
//        CGFloat version = [[[UIDevice currentDevice] systemVersion] floatValue];
//        if (version >= 10.0) {
//            
//        } else {
//            [[UIApplication sharedApplication] openURL:url];
//        }
    }
}

// 当内容开始返回时调用
- (void)webView:(WKWebView *)webView didCommitNavigation:(WKNavigation *)navigation {
    
}

// 页面加载完成之后调用
- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation {
    // 页面加载完成 后将navigationBar的标题修改成 网页的标题
    if(webView.title && webView.title.length > 0) {
        self.navigationItem.title = webView.title;
    }
    
    //加载完成后隐藏progressView
    self.progressView.hidden = YES;
}

// 页面加载失败时调用
- (void)webView:(WKWebView *)webView didFailProvisionalNavigation:(null_unspecified WKNavigation *)navigation withError:(NSError *)error {
    //加载失败同样需要隐藏progressView
    self.progressView.hidden = YES;
    
    /*  以下url都是 有针对性的适配的：
     itms-appss://itunes.apple.com/cn/app/tong-cheng-lu-you-jing-dian/id475966832?mt=8
     itms-appss://itunes.apple.com/app/apple-store/id1019481423?mt=8
     */
    
    if (error) {
        if ([error.userInfo.allKeys containsObject:@"NSLocalizedDescription"]) {
            NSString *descKey = error.userInfo[@"NSLocalizedDescription"];
            
            if ([descKey isEqualToString:@"unsupported URL"]) {
                // 显示 网络开小差界面
            }
        }
        
        id urlKey = error.userInfo[@"NSErrorFailingURLKey"];
        if ([urlKey isKindOfClass:[NSURL class]]) {
            NSURL *url = (NSURL *)urlKey;
            if ([url.absoluteString containsString:@"tel:"] || [url.absoluteString containsString:@"sms:"] || [url.absoluteString containsString:@"zaapp:"] ||
                [url.absoluteString isEqualToString:@"itms-appss://itunes.apple.com/cn/app/tong-cheng-lu-you-jing-dian/id475966832?mt=8"]) {
            } else {
                if (![url.absoluteString isEqualToString:@"itms-appss://itunes.apple.com/app/apple-store/id1019481423?mt=8"]) {
                    
                }
            }
        }
    }
}

// 页面加载失败时调用
- (void)webView:(WKWebView *)webView didFailNavigation:(null_unspecified WKNavigation *)navigation withError:(NSError *)error {
    //加载失败同样需要隐藏progressView
    self.progressView.hidden = YES;
}

// 接收到服务器跳转请求之后调用
- (void)webView:(WKWebView *)webView didReceiveServerRedirectForProvisionalNavigation:(WKNavigation *)navigation {
    
}

// 在收到响应后，决定是否跳转
- (void)webView:(WKWebView *)webView decidePolicyForNavigationResponse:(WKNavigationResponse *)navigationResponse decisionHandler:(void (^)(WKNavigationResponsePolicy))decisionHandler {
    
    self.url = navigationResponse.response.URL.absoluteString;
    NSLog(@"%@",navigationResponse.response.URL.absoluteString);
    
    decisionHandler(WKNavigationResponsePolicyAllow);       // 允许跳转
}

// 在发送请求之前，决定是否跳转
- (void)webView:(WKWebView *)webView decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction decisionHandler:(void (^)(WKNavigationActionPolicy))decisionHandler {
    
    self.url = navigationAction.request.URL.absoluteString;
    NSLog(@"%@",navigationAction.request.URL.absoluteString);
    
    //如果是跳转一个新页面
    if (navigationAction.targetFrame == nil) {
        [webView loadRequest:navigationAction.request];
    }
    
    decisionHandler(WKNavigationActionPolicyAllow); //允许跳转
}

#pragma mark - WKUIDelegate

// 创建一个新的WebView
- (WKWebView *)webView:(WKWebView *)webView createWebViewWithConfiguration:(WKWebViewConfiguration *)configuration forNavigationAction:(WKNavigationAction *)navigationAction windowFeatures:(WKWindowFeatures *)windowFeatures {
    return [[WKWebView alloc] init];
}

// 警告框
- (void)webView:(WKWebView *)webView runJavaScriptAlertPanelWithMessage:(NSString *)message initiatedByFrame:(WKFrameInfo *)frame completionHandler:(void (^)(void))completionHandler {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"提示"
                                                                   message:message ? message : @""
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *action = [UIAlertAction actionWithTitle:@"确认"
                                                     style:UIAlertActionStyleDefault
                                                   handler:^(UIAlertAction * _Nonnull action) {
                                                       completionHandler();
                                                   }];
    [alert addAction:action];
    
    [self presentViewController:alert animated:YES completion:nil];
}

// 确认框
- (void)webView:(WKWebView *)webView runJavaScriptConfirmPanelWithMessage:(NSString *)message initiatedByFrame:(WKFrameInfo *)frame completionHandler:(void (^)(BOOL))completionHandler {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"提示"
                                                                   message:message ? message : @""
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *sureAction = [UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        completionHandler(YES);
    }];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        completionHandler(NO);
    }];
    
    [alert addAction:cancelAction];
    [alert addAction:sureAction];
    
    [self presentViewController:alert animated:YES completion:nil];
}

// 输入框
- (void)webView:(WKWebView *)webView runJavaScriptTextInputPanelWithPrompt:(NSString *)prompt defaultText:(NSString *)defaultText initiatedByFrame:(WKFrameInfo *)frame completionHandler:(void (^)(NSString * _Nullable))completionHandler {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:prompt
                                                                   message:prompt ? prompt : @""
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    [alert addTextFieldWithConfigurationHandler:^(UITextField * _Nonnull textField) {
        textField.text = defaultText;
    }];
    
    UIAlertAction *action = [UIAlertAction actionWithTitle:@"完成" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        completionHandler(alert.textFields[0].text?:@"");
    }];
    
    [alert addAction:action];
    
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - EasyViewControllerProtocol

- (void)bindViewModel {
    
}

@end
