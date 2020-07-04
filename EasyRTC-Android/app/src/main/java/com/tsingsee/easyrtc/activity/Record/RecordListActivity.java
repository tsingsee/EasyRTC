package com.tsingsee.easyrtc.activity.Record;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.activity.BaseActivity;
import com.tsingsee.easyrtc.adapter.RecordAdapter;
import com.tsingsee.easyrtc.databinding.ActivityRecordListBinding;
import com.tsingsee.easyrtc.http.BaseEntity3;
import com.tsingsee.easyrtc.http.BaseObserver3;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.Record;
import com.tsingsee.easyrtc.model.RoomRecord;
import com.tsingsee.easyrtc.tool.DateUtil;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

/**
 * 录像列表
 * */
public class RecordListActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

    private static final int request_code = 8888;

    private ActivityRecordListBinding binding;
    private RecordAdapter adapter;

    private RoomRecord item;
    private Date selectDate;
    private List<Record> recordModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_record_list);

        setSupportActionBar(binding.mainToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.mainToolbar.setOnMenuItemClickListener(this);
        // 左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        binding.mainToolbar.setNavigationIcon(R.drawable.back);

        Intent intent = getIntent();
        item = (RoomRecord) intent.getSerializableExtra("id");
        selectDate = (Date) intent.getSerializableExtra("selectDate");

        showDateTv();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.recordRecyclerView.setLayoutManager(manager);

        // 日历选择日期
        binding.dateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecordListActivity.this, CalendarActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("id", item);
                bundle.putSerializable("selectDate", selectDate);
                intent.putExtras(bundle);//发送数据
                startActivityForResult(intent, request_code);
            }
        });

        // 添加侧滑菜单
        binding.recordRecyclerView.setSwipeMenuCreator(new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                SwipeMenuItem downloadItem = new SwipeMenuItem(RecordListActivity.this);
                downloadItem.setText("下载");
                downloadItem.setTextSize(15);
                downloadItem.setTextColor(getResources().getColor(R.color.white_color));
                downloadItem.setBackgroundColor(getResources().getColor(R.color.color_5b2e));
                downloadItem.setWidth(200);
                downloadItem.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

                SwipeMenuItem shareItem = new SwipeMenuItem(RecordListActivity.this);
                shareItem.setText("分享");
                shareItem.setTextSize(15);
                shareItem.setTextColor(getResources().getColor(R.color.white_color));
                shareItem.setBackgroundColor(getResources().getColor(R.color.color_fb9643));
                shareItem.setWidth(200);
                shareItem.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

                SwipeMenuItem deleteItem = new SwipeMenuItem(RecordListActivity.this);
                deleteItem.setText("删除");
                deleteItem.setTextSize(15);
                deleteItem.setTextColor(getResources().getColor(R.color.white_color));
                deleteItem.setBackgroundColor(getResources().getColor(R.color.color_fd6845));
                deleteItem.setWidth(200);
                deleteItem.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

                swipeRightMenu.addMenuItem(downloadItem);
                swipeRightMenu.addMenuItem(shareItem);
                swipeRightMenu.addMenuItem(deleteItem);
            }
        });
        binding.recordRecyclerView.setSwipeMenuItemClickListener(new SwipeMenuItemClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
                menuBridge.closeMenu();

                Record record = recordModel.get(menuBridge.getAdapterPosition());
                if (menuBridge.getPosition() == 0) {        // 下载
                    downloadVideo(record);
                } else if (menuBridge.getPosition() == 1) { // 分享
                    shareVideo(record);
                } else {                                    // 删除
                    deleteVideo(record);
                }
            }
        });

        binding.activityEmptyView.setVisibility(View.GONE);

        showHub("查询中");
        queryDaily();
    }

    private void showDateTv() {
        String period = DateUtil.getDateStr(selectDate, "yyyy-MM-dd");
        binding.dateTv.setText("  " + period);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == request_code && resultCode == RESULT_OK) {
            selectDate = (Date) intent.getSerializableExtra("selectDate");

            showDateTv();

            showHub("查询中");
            queryDaily();
        }
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    private void shareVideo(final Record record) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("text", record.getHls());
        cm.setPrimaryClip(myClip);

        new AlertDialog.Builder(RecordListActivity.this)
                .setTitle("提示")
                .setMessage("您已成功复制该时段的录像地址")
                .setNegativeButton("确认", null)
                .show();
    }

    private void downloadVideo(final Record record) {
//        new AlertDialog.Builder(this)
//                .setTitle("提示")
//                .setMessage("确定下载该时段的录像吗？")
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        SharedHelper helper = new SharedHelper(RTCApplication.getContext());
//                        String url = helper.getURL() + "/api/v1/record/download/" + id + "/" + record.getStartAt();
//
//                        DownLoadUtil downLoadUtil = new DownLoadUtil();
//                        downLoadUtil.initDownload(RecordListActivity.this);
//                        downLoadUtil.download(url, record.getStartAt());
//                    }
//                })
//                .setNegativeButton("取消", null)
//                .show();
    }

    private void deleteVideo(final Record record) {
//        new AlertDialog.Builder(this)
//                .setTitle("提示")
//                .setMessage("确定删除该时段的录像吗？")
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        removeRecord(record);
//                    }
//                })
//                .setNegativeButton("取消", null)
//                .show();
    }

    private void removeRecord(final Record record) {
        if (record == null) {
            return;
        }

        showHub("删除中");

        Observable<BaseEntity3<String>> observable = RetrofitFactory.getRetrofitService2().removeRecord(item.getId(), record.getStartAt());
        observable.compose(compose(this.<BaseEntity3<String>> bindToLifecycle()))
                .subscribe(new BaseObserver3<String>(this, dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(String res) {
                        hideHub();

                        // 删除完成后 刷新数据
                        showHub("查询中");
                        queryDaily();
                    }

                    @Override
                    protected void loginSuccess() {
                        removeRecord(record);
                    }
                });
    }

    private void queryDaily() {
        String period = DateUtil.getDateStr(selectDate, "yyyyMMdd");

        Observable<BaseEntity3<List<Record>>> observable = RetrofitFactory.getRetrofitService2().queryDaily(item.getId(), period);
        observable.compose(compose(this.<BaseEntity3<List<Record>>> bindToLifecycle()))
                .subscribe(new BaseObserver3<List<Record>>(this, dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(List<Record> records) {
                        recordModel = records;

                        binding.activityEmptyView.setVisibility(View.GONE);

                        adapter = new RecordAdapter(RecordListActivity.this);
                        binding.recordRecyclerView.setAdapter(adapter);

                        adapter.notifyDataSetChanged(records);

                        if (records.size() == 0) {
                            binding.activityEmptyView.setVisibility(View.VISIBLE);
                        }

                        hideHub();
                    }

                    @Override
                    protected void loginSuccess() {
                        queryDaily();
                    }
                });
    }
}
