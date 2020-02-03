package com.tsingsee.easyrtc.activity;

import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.databinding.ActivityCreateRoomBinding;

import java.util.Calendar;

public class CreateRoomActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private ActivityCreateRoomBinding binding;

    private int birthdayYear;
    private int birthdayMonth;
    private int birthdayDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_room);
        binding.setOnClick(this);

        setSupportActionBar(binding.infoToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.infoToolbar.setOnMenuItemClickListener(this);
        // 左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        binding.infoToolbar.setNavigationIcon(R.drawable.back);

        Calendar c = Calendar.getInstance();
        birthdayYear = c.get(Calendar.YEAR);
        birthdayMonth = c.get(Calendar.MONTH);
        birthdayDay = c.get(Calendar.DATE);
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
        if (v.getId() == R.id.create_room_date_ll) {
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    birthdayYear = year;
                    birthdayMonth = monthOfYear;
                    birthdayDay = dayOfMonth;

                    String month = String.format("%02d", birthdayMonth+1);
                    String day = String.format("%02d", birthdayDay);
                    binding.createRoomDateEt.setText(year + "-" + month + "-" + day);
                }
            }, birthdayYear, birthdayMonth, birthdayDay).show();
        }
    }
}
