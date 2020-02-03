package com.tsingsee.easyrtc.tool;

import android.content.Context;
import android.content.SharedPreferences;

import com.tsingsee.easyrtc.model.Account;

public class SharedHelper {
    private Context mContext;

    public SharedHelper(Context mContext) {
        this.mContext = mContext;
    }

    public void saveAccount(Account account) {
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("userName", account.getUserName());
        editor.putString("pwd", account.getPwd());

        editor.commit();
    }

    public Account readAccount() {
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);

        Account account = new Account();
        account.setUserName(sp.getString("userName", ""));
        account.setPwd(sp.getString("pwd", ""));

        return account;
    }

    public String getURL() {
//        Account account = readAccount();
//
//        String url;
//        if (!account.getIp().startsWith("http")) {
//            url = "http://" + account.getIp() + ":" + account.getPort();
//        } else {
//            url = account.getIp() + ":" + account.getPort();
//        }
//
//        return url;
        return "https://demo.easyrtc.cn/api/v1/";
    }

//    // 删除密码和token
//    public void removeAccount() {
//        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//
//        editor.putString("pwd", "");
//        editor.putString("token", "");
//
//        editor.commit();
//    }
}
