package com.tsingsee.easyrtc.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.databinding.ActivityLoginBinding;
import com.tsingsee.easyrtc.http.BaseEntity3;
import com.tsingsee.easyrtc.http.BaseObserver3;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.Account;
import com.tsingsee.easyrtc.tool.MD5Util;
import com.tsingsee.easyrtc.tool.SharedHelper;
import com.tsingsee.easyrtc.tool.ToastUtil;

import io.reactivex.Observable;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setOnClick(this);

        SharedHelper sp = new SharedHelper(this);
        Account account = sp.readAccount();

        binding.ipEt.setText(account.getServerAddress());
        binding.portEt.setText(account.getPort());
        binding.accountEt.setText(account.getUserName());
        binding.pwdEt.setText(account.getPwd());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.account_btn:
                next();
                break;
            default:
                break;
        }
    }

    private void next() {
        if (TextUtils.isEmpty(binding.ipEt.getText())) {
            ToastUtil.show("请输入服务器地址");
            return;
        }

        if (TextUtils.isEmpty(binding.portEt.getText())) {
            ToastUtil.show("请输入端口");
            return;
        }

        if (TextUtils.isEmpty(binding.accountEt.getText())) {
            ToastUtil.show("请输入帐号");
            return;
        }

        if (TextUtils.isEmpty(binding.pwdEt.getText())) {
            ToastUtil.show("请输入密码");
            return;
        }

        if (!binding.pwdEt.getText().toString().equals(Account.PWD)) {
            ToastUtil.show("密码错误");
            return;
        }

        Account account = new Account();
        account.setServerAddress(binding.ipEt.getText().toString());
        account.setPort(binding.portEt.getText().toString());

        SharedHelper sp = new SharedHelper(this);
        sp.saveAccount(account);

        login();
    }

    public void login() {
        showHub("登录中");

        String name = binding.accountEt.getText().toString();
        String pwd = MD5Util.md5(binding.pwdEt.getText().toString());

        Observable<BaseEntity3<Account>> observable = RetrofitFactory.getRetrofitService().login(name, pwd);
        observable.compose(compose(this.<BaseEntity3<Account>> bindToLifecycle()))
                .subscribe(new BaseObserver3<Account>(this, dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(Account model) {
                        hideHub();

                        Account account = new Account();
                        account.setServerAddress(binding.ipEt.getText().toString());
                        account.setPort(binding.portEt.getText().toString());
                        account.setUserName(binding.accountEt.getText().toString());
                        account.setPwd(binding.pwdEt.getText().toString());
                        account.setToken(model.getToken());

                        SharedHelper sp = new SharedHelper(LoginActivity.this);
                        sp.saveAccount(account);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                    }

                    @Override
                    protected void loginSuccess() {

                    }
                });
    }
}
