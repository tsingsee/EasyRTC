package com.tsingsee.easyrtc.tool;

import android.content.Context;
import android.content.SharedPreferences;

import com.tsingsee.easyrtc.model.Account;
import com.tsingsee.easyrtc.model.UserInfo;

public class SharedHelper {
    private Context mContext;

    public SharedHelper(Context mContext) {
        this.mContext = mContext;
    }

    public void saveAccount(Account account) {
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("ip", account.getServerAddress());
        editor.putString("port", account.getPort());
        editor.putString("userName", account.getUserName());
        editor.putString("pwd", account.getPwd());
        editor.putString("token", account.getToken());

        editor.commit();
    }

    public Account readAccount() {
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);

        Account account = new Account();
        account.setServerAddress(sp.getString("ip", ""));
        account.setPort(sp.getString("port", ""));
        account.setUserName(sp.getString("userName", ""));
        account.setPwd(sp.getString("pwd", ""));
        account.setToken(sp.getString("token", ""));

        return account;
    }

    public String getURL() {
        Account account = readAccount();
//
//        String url;
//        if (!account.getIp().startsWith("http")) {
//            url = "http://" + account.getIp() + ":" + account.getPort();
//        } else {
//            url = account.getIp() + ":" + account.getPort();
//        }
//
//        return url;

//        return "https://demo.easyrtc.cn/api/v1/";
        return "http://" + account.getServerAddress() + ":" + account.getPort() + "/v1/";
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

    public void saveUserInfo(UserInfo user) {
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("user_id", user.getId());
        editor.putString("user_name", user.getUserName());

        editor.commit();
    }

    public UserInfo readUserInfo() {
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);

        UserInfo user = new UserInfo();
        user.setId(sp.getString("user_id", ""));
        user.setUserName(sp.getString("user_name", ""));

        return user;
    }
}
