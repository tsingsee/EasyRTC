package com.tsingsee.easyrtc.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.databinding.ActivitySettingBinding;
import com.tsingsee.easyrtc.http.BaseEntity3;
import com.tsingsee.easyrtc.http.BaseObserver3;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.ServiceInfo;
import com.tsingsee.easyrtc.tool.ToastUtil;

import io.reactivex.Observable;

public class SettingActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        binding.setOnClick(this);

        setSupportActionBar(binding.infoToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.infoToolbar.setOnMenuItemClickListener(this);
        // 左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        binding.infoToolbar.setNavigationIcon(R.drawable.back);

        String text = "Copyright © 2020 <font color='#FFFFFF'>www.tsingsee.com</font> All rights reversed.";
        binding.copyrightTv.setText(Html.fromHtml(text));

        showHub("查询中");
        getServerInfo();

//        SharedHelper helper = new SharedHelper(getApplicationContext());
//        Account account = helper.readAccount();
//        // 登录后才能进入设置页
//        if (account.getToken() == null || account.getToken().equals("")) {
//            binding.quitBtn.setText(R.string.account_login);
//        }
    }

    public void getServerInfo() {
        Observable<BaseEntity3<ServiceInfo>> observable = RetrofitFactory.getRetrofitService().getServerInfo();
        observable.compose(compose(this.<BaseEntity3<ServiceInfo>> bindToLifecycle()))
                .subscribe(new BaseObserver3<ServiceInfo>(this, dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(ServiceInfo serviceInfo) {
                        hideHub();
                        binding.setService(serviceInfo);
                    }

                    @Override
                    protected void loginSuccess() {
                        getServerInfo();
                    }
                });
    }

    public void verifyProductCode(String code) {
        Observable<BaseEntity3<Object>> observable = RetrofitFactory.getRetrofitService().verifyproductcode(code);
        observable.compose(compose(this.<BaseEntity3<Object>> bindToLifecycle()))
                .subscribe(new BaseObserver3<Object>(this, dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(Object obj) {
                        hideHub();
                        ToastUtil.show("提交成功");
                    }

                    @Override
                    protected void loginSuccess() {
                        getServerInfo();
                    }
                });
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.setting_code_btn) {
            String text = binding.settingCodeEt.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                showHub("提交中");
                verifyProductCode(text);
            } else {
                ToastUtil.show("请输入激活码");
            }
        } else if (v.getId() == R.id.quit_btn) {
//            SharedHelper helper = new SharedHelper(getApplicationContext());
//            Account account = helper.readAccount();
//            if (account.getToken() == null || account.getToken().equals("")) {
//                goLogin(true);
//            } else {
//                new AlertDialog.Builder(this)
//                        .setTitle("确定退出当前账号吗？")
//                        .setMessage("")
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                goLogin(false);
//                            }
//                        })
//                        .setNegativeButton("取消", null)
//                        .show();
//            }
        } else if (v.getId() == R.id.below_ll) {
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra("url", "http://www.tsingsee.com");
            startActivity(intent);
        }
    }

//    private void goLogin(boolean isLogin) {
//        SharedHelper helper = new SharedHelper(getApplicationContext());
//        helper.removeAccount();
//
//        Intent intent = new Intent(InfoActivity.this, LoginActivity.class);
//        if (isLogin) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
//        startActivity(intent);
//        finish();
//    }
}
