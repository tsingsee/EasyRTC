package com.tsingsee.easyrtc.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.tsingsee.easyrtc.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class StartActivity extends BaseActivity {

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "1.0";
        }

        TextView txtVersion = (TextView) findViewById(R.id.txt_version);
        txtVersion.setText(String.format("客户端版本：%s", versionName));

        Observable.interval(1, TimeUnit.SECONDS)
                .compose(this.<Long>bindToLifecycle())
//                .compose(this.<BaseEntity<User>>bindToLifecycle())
//                .compose(compose(this.<BaseEntity<User>> bindUntilEvent(ActivityEvent.DESTROY)))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        next();
                        disposable.dispose();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void next() {
//        SharedHelper helper = new SharedHelper(getApplicationContext());
//        Account account = helper.readAccount();
//
//        if ("".equals(account.getToken()) || "".equals(account.getUserName())) {
            Intent it = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(it);
//        } else {
//            Intent it = new Intent(StartActivity.this, RtcActivity.class);
//            startActivity(it);
//        }

        finish();
    }
}
