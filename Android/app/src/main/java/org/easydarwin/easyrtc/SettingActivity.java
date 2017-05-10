/*
	Copyright (c) 2013-2016 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/

package org.easydarwin.easyrtc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends AppCompatActivity {

    private static final boolean TEST_ = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        final EditText txtIp = (EditText) findViewById(R.id.edt_server_address);
        final EditText txtPort = (EditText) findViewById(R.id.edt_server_port);
        final EditText txtId = (EditText) findViewById(R.id.edt_stream_id);
        final EditText txtPeerId = (EditText) findViewById(R.id.edt_peer_id);

//        String ip = EasyApplication.getEasyApplication().getIp();
//        String port = EasyApplication.getEasyApplication().getPort();
//        String id = EasyApplication.getEasyApplication().getId();


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        txtIp.setText(preferences.getString("ip", getString(R.string.default_value)));
        txtPort.setText(preferences.getString("port", getString(R.string.default_port)));
        txtId.setText(preferences.getString("id", String.valueOf((int)(10000 * Math.random()))));
        txtPeerId.setText(preferences.getString("peer_id", ""));

        Button btnSave = (Button) findViewById(R.id.ok);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipValue = txtIp.getText().toString();
                String portValue = txtPort.getText().toString();
                String idValue = txtId.getText().toString();
                String peerId = txtPeerId.getText().toString();

                if (TextUtils.isEmpty(ipValue)) {
                    ipValue = getString(R.string.default_value);
                }
                if (TextUtils.isEmpty(portValue)) {
                    portValue = getString(R.string.default_port);
                }
                if (TextUtils.isEmpty(idValue)) {
                    idValue = String.valueOf(10000 * Math.random());
                }
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
                preferences.edit().putString("ip", ipValue).putString("port", portValue).putString("id", idValue).putString("peer_id", peerId).apply();
                finish();
            }
        });

    }
}
