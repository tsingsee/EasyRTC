package com.tsingsee.easyrtc.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.databinding.ActivityWebBinding;

public class WebActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private ActivityWebBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web);

        setSupportActionBar(binding.webToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.webToolbar.setOnMenuItemClickListener(this);
        // 左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        binding.webToolbar.setNavigationIcon(R.drawable.back);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        binding.webView.loadUrl(url);
        binding.webView.addJavascriptInterface(this,"android");//添加js监听 这样html就能调用客户端
        binding.webView.setWebChromeClient(webChromeClient);
        binding.webView.setWebViewClient(webViewClient);

        // 特别注意：5.1以上默认禁止了https和http混用，以下方式是开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // WebSettings对WebView进行配置和管理
        WebSettings webSettings = binding.webView.getSettings();

        // 如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        /**
         * LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
         * LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
         * LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
         * LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
         */
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//不使用缓存，只从网络获取数据.

//        //支持屏幕缩放
//        webSettings.setSupportZoom(true);
//        webSettings.setBuiltInZoomControls(true);

        // 不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 释放资源
        if (binding.webView != null) {
            binding.webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);

            // 清除当前webview访问的历史记录
            // 只会webview访问历史记录里的所有记录除了当前访问记录
            binding.webView.clearHistory();

            ((ViewGroup) binding.webView.getParent()).removeView(binding.webView);
            binding.webView.destroy();
//            binding.webView = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    // 返回的功能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // WebViewClient主要帮助WebView处理各种通知、请求事件
    private WebViewClient webViewClient = new WebViewClient() {

        // 在页面加载结束时调用。我们可以关闭loading 条，切换程序动作。
        @Override
        public void onPageFinished(WebView view, String url) {//页面加载完成
            binding.progressbar.setVisibility(View.GONE);
        }

        // 开始载入页面调用的，我们可以设定一个loading的页面，告诉用户程序在等待网络响应。
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//页面开始加载
            binding.progressbar.setVisibility(View.VISIBLE);
        }

        // 打开网页时不调用系统浏览器， 而是在本WebView中显示；在网页上的所有加载都经过这个方法
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("ansen","拦截url:"+url);

            if(url.equals("http://www.google.com/")){
                Toast.makeText(WebActivity.this,"国内不能访问google,拦截该url",Toast.LENGTH_LONG).show();
                return true;//表示我已经处理过了
            }

            return super.shouldOverrideUrlLoading(view, url);
        }

        // 在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次。
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        // 加载页面的服务器出现错误时（如404）调用。
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        // 处理https请求(webView默认是不处理https请求的，页面显示空白)
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }
    };

    // WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    private WebChromeClient webChromeClient = new WebChromeClient() {

        // 获取网页标题
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);

            Log.i("ansen","网页标题:"+title);
            binding.webToolbarTv.setText(title);
        }

        // 获得网页的加载进度并显示
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            binding.progressbar.setProgress(newProgress);
        }

        //不支持js的alert弹窗，需要自己监听然后通过dialog弹窗
        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton("确定",null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();

            // 注意:
            // 必须要这一句代码:result.confirm()表示:
            // 处理结果为确定状态同时唤醒WebCore线程
            // 否则不能继续点击按钮
            result.confirm();

            return true;
        }

        // 支持javascript的确认框
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(WebActivity.this)
                    .setTitle("JsConfirm")
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    })
                    .setCancelable(false)
                    .show();
            // 返回布尔值：判断点击时确认还是取消
            // true表示点击了确认；false表示点击了取消；
            return true;
        }

        // 支持javascript输入框
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            final EditText et = new EditText(WebActivity.this);
            et.setText(defaultValue);
            new AlertDialog.Builder(WebActivity.this)
                    .setTitle(message)
                    .setView(et)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm(et.getText().toString());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    })
                    .setCancelable(false)
                    .show();

            return true;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("ansen","是否有上一个页面:" + binding.webView.canGoBack());

        if (binding.webView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK){
            //点击返回按钮的时候判断有没有上一页
            binding.webView.goBack(); // goBack()表示返回webView的上一页面
            return true;
        }

        return super.onKeyDown(keyCode,event);
    }

    /**
     * JS调用android的方法
     * @param str
     * @return
     */
    @JavascriptInterface //仍然必不可少
    public void  getClient(String str){
        Log.i("ansen","html调用客户端:"+str);
    }
}
