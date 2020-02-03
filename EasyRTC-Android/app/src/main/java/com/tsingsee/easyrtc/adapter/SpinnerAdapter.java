package com.tsingsee.easyrtc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tsingsee.easyrtc.R;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter {
    private Context mContext;
    private List<String> mDataList;

    public SpinnerAdapter(Context context, List<String> dataList) {
        super(context, android.R.layout.simple_spinner_item, dataList);

        mContext = context;
        mDataList = dataList;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        //修改Spinner展开后的字体颜色
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent,false);
        }

        //此处text1是Spinner默认的用来显示文字的TextView
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
        tv.setText(mDataList.get(position));
        tv.setTextSize(14f);
        tv.setTextColor(mContext.getResources().getColor(R.color.color_gray2));

        return convertView;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 修改Spinner选择后结果的字体颜色
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        //此处text1是Spinner默认的用来显示文字的TextView
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
        tv.setText(mDataList.get(position));
        tv.setTextSize(14f);
        tv.setTextColor(mContext.getResources().getColor(R.color.color_gray2));

        return convertView;
    }
}
