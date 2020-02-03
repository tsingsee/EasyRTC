package com.tsingsee.easyrtc.activity;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.tsingsee.easyrtc.BuildConfig;
import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.adapter.FragmentIndexAdapter;
import com.tsingsee.easyrtc.fragment.LiveFragment;
import com.tsingsee.easyrtc.fragment.RecordFragment;
import com.tsingsee.easyrtc.fragment.RoomFragment;
import com.tsingsee.easyrtc.tool.ToastUtil;
import com.tsingsee.easyrtc.view.MyViewPager;
import com.tsingsee.easyrtc.view.ProVideoView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private MyViewPager index_vp_fragment_list_top;
    private LinearLayout index_bottom_bar1;
    private LinearLayout index_bottom_bar2;
    private LinearLayout index_bottom_bar3;

    private List<Fragment> mFragments;

    private FragmentIndexAdapter mFragmentIndexAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();

        ProVideoView.setKey(BuildConfig.PLAYER_KEY);
    }

    private void initEvent() {
        index_bottom_bar1.setOnClickListener(new TabOnClickListener(0));
        index_bottom_bar2.setOnClickListener(new TabOnClickListener(1));
        index_bottom_bar3.setOnClickListener(new TabOnClickListener(2));
    }

    private void initIndexFragmentAdapter() {
        mFragmentIndexAdapter = new FragmentIndexAdapter(this.getSupportFragmentManager(), mFragments);
        index_vp_fragment_list_top.setAdapter(mFragmentIndexAdapter);
        index_vp_fragment_list_top.setCurrentItem(0);
        index_vp_fragment_list_top.setOffscreenPageLimit(3);
        index_vp_fragment_list_top.addOnPageChangeListener(new TabOnPageChangeListener());

        index_bottom_bar1.setSelected(true);
    }

    private void initData() {
        mFragments = new ArrayList<>();
        mFragments.add(new RoomFragment());
        mFragments.add(new LiveFragment());
        mFragments.add(new RecordFragment());
        initIndexFragmentAdapter();
    }

    private void initView() {
        index_vp_fragment_list_top = (MyViewPager) findViewById(R.id.index_vp_fragment_list_top);
        index_bottom_bar1 = (LinearLayout) findViewById(R.id.index_bottom_bar1);
        index_bottom_bar2 = (LinearLayout) findViewById(R.id.index_bottom_bar2);
        index_bottom_bar3 = (LinearLayout) findViewById(R.id.index_bottom_bar3);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    /**
     * Bottom_Bar的点击事件
     */
    public class TabOnClickListener implements View.OnClickListener {

        private int index = 0;

        public TabOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            //选择某一页
            index_vp_fragment_list_top.setCurrentItem(index, false);
        }

    }

    public class TabOnPageChangeListener implements ViewPager.OnPageChangeListener {

        //当滑动状态改变时调用
        public void onPageScrollStateChanged(int state) {
        }

        //当前页面被滑动时调用
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        //当新的页面被选中时调用
        public void onPageSelected(int position) {
            resetTextView();

            switch (position) {
                case 0:
                    index_bottom_bar1.setSelected(true);
                    break;
                case 1:
                    index_bottom_bar2.setSelected(true);
                    break;
                case 2:
                    index_bottom_bar3.setSelected(true);
                    break;
            }
        }
    }

    /**
     * 重置所有TextView的字体颜色
     */
    private void resetTextView() {
        index_bottom_bar1.setSelected(false);
        index_bottom_bar2.setSelected(false);
        index_bottom_bar3.setSelected(false);
    }

    private long lastPressBackKeyTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (RoomFragment.isConnecting) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long current = System.currentTimeMillis();
            long Interval = current - lastPressBackKeyTime;

            if (Interval > 2 * 1000) {
                lastPressBackKeyTime = System.currentTimeMillis();
                ToastUtil.show(getResources().getString(R.string.exit_hint));
            } else {
                // 退出
                lastPressBackKeyTime = 0;
                finish();
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
