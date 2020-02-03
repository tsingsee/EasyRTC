package com.tsingsee.easyrtc.tool;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.tsingsee.easyrtc.RTCApplication;

public class DownLoadUtil {

    private static CompleteReceiver completeReceiver;
    private static DownloadManager downloadManager;

    private static long reference;

    // https://blog.csdn.net/hnzcdy/article/details/53096798
    public void initDownload(Context context) {
        // 下载任务
        String serviceString = Context.DOWNLOAD_SERVICE;
        // 直接使用系统的下载管理器。是不是非常方便
        downloadManager = (DownloadManager) context.getSystemService(serviceString);

        //注册下载的广播监听
        completeReceiver = new CompleteReceiver();
        /** 注册下载监听的广播 **/
        completeReceiver = new CompleteReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        /** register download success broadcast **/
        context.registerReceiver(completeReceiver, filter);
    }

    public void download(String url, String time) {
        Log.d("DownLoadUtil", url);
        String name = time + ".mp4";

        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("下载录像");           // 通知栏的标题
        request.setDescription(name);    // 显示通知栏的说明
        request.setVisibleInDownloadsUi(true);

        // TODO
        // TODO
        // TODO
        // TODO
//        SharedHelper helper = new SharedHelper(RTCApplication.getContext());
//        Account account = helper.readAccount();
//        if (!account.getToken().equals("")) {
//            request.addRequestHeader("Cookie", "token=" + account.getToken());
//        }

        // 下载到那个文件夹下，以及命名
        request.setDestinationInExternalFilesDir(RTCApplication.getContext(), Environment.DIRECTORY_DOWNLOADS, name);
        // 下载的唯一标识，可以用这个标识来控制这个下载的任务enqueue（）开始执行这个任务
        reference = downloadManager.enqueue(request);
    }

    // 下载的状态广播接收
    class CompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 下载完成之后监听
            String action = intent.getAction();
            // 下载完成的监听
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                Log.d("downloadComplete", "下载完成");
            }

            // 点击通知栏，取消下载任务
            if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                downloadManager.remove((Long) reference);
            }
        }
    }
}
