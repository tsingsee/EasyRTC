package com.tsingsee.easyrtc.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.databinding.ActivityLoginBinding;
import com.tsingsee.easyrtc.model.Account;
import com.tsingsee.easyrtc.tool.SharedHelper;
import com.tsingsee.easyrtc.tool.ToastUtil;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setOnClick(this);

        SharedHelper sp = new SharedHelper(this);
        Account account = sp.readAccount();

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
        account.setUserName(binding.accountEt.getText().toString());
        account.setPwd(binding.pwdEt.getText().toString());

        SharedHelper sp = new SharedHelper(this);
        sp.saveAccount(account);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }
}
