package com.tsingsee.easyrtc.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.databinding.ActivityLiveDetailBinding;
import com.tsingsee.easyrtc.model.LiveSessionModel;
import com.tsingsee.easyrtc.tool.ToastUtil;
import com.tsingsee.rtc.Options;

import java.util.Hashtable;

public class LiveDetailActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private ActivityLiveDetailBinding binding;
    private LiveSessionModel session;

    private ClipboardManager cm;
    private ClipData mClipData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_detail);
        binding.setOnClick(this);

        setSupportActionBar(binding.infoToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.infoToolbar.setOnMenuItemClickListener(this);
        // 左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        binding.infoToolbar.setNavigationIcon(R.drawable.back);

        session = (LiveSessionModel) getIntent().getSerializableExtra("session");

        Options options = new Options(this);

        String addr = "https://" + options.serverAddress + "/record";
        String flv = addr + session.getHttpFlv().replace("/hls", "");
        String hls = addr + session.getHls().replace("/hls", "");

        binding.liveFlvTv.setText(flv);
        binding.liveHlsTv.setText(hls);
        binding.liveRtmpTv.setText(session.getRtmp());

        Bitmap bitmap = createQRCodeBitmap(session.getRtmp(),
                200,
                200,
                "UTF-8",
                "H",
                "1", Color.BLACK, Color.WHITE);
        binding.liveCodeIv.setImageBitmap(bitmap);
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
        if (v.getId() == R.id.live_hls_iv) {
            //获取剪贴板管理器
            cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            mClipData = ClipData.newPlainText("Label", binding.liveHlsTv.getText());
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            ToastUtil.show("复制成功");
        } else if (v.getId() == R.id.live_flv_iv) {
            cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            mClipData = ClipData.newPlainText("Label", binding.liveFlvTv.getText());
            cm.setPrimaryClip(mClipData);
            ToastUtil.show("复制成功");
        } else {
            cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            mClipData = ClipData.newPlainText("Label", binding.liveRtmpTv.getText());
            cm.setPrimaryClip(mClipData);
            ToastUtil.show("复制成功");
        }
    }

    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    public static Bitmap createQRCodeBitmap(String content, int width, int height,
                                            String character_set, String error_correction_level,
                                            String margin, int color_black, int color_white) {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }

        try {
            /** 1.设置二维码相关配置 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set);
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
