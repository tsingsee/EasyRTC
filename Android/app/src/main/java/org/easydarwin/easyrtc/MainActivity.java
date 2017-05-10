package org.easydarwin.easyrtc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import org.easydarwin.easyrtc.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    PlayFragment play;
    PushFragment pusher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        preferences.getString("id", getString(R.string.default_value));

//        txtIp.setText(preferences.getString("ip", getString(R.string.default_value)));
//        txtPort.setText(preferences.getString("port", getString(R.string.default_port)));
//        txtId.setText(preferences.getString("id", String.valueOf(10000 * Math.random())));
        String peer_id = preferences.getString("peer_id", null);
        if (TextUtils.isEmpty(peer_id)){
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
    }

    public void onSettings(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
}
