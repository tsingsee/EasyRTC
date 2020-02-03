package com.tsingsee.easyrtc.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.activity.Record.PlayRecordActivity;
import com.tsingsee.easyrtc.model.Record;

import java.util.List;

/**
 * 录像列表
 * */
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;

    private List<Record> mDataList;

    public RecordAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void notifyDataSetChanged(List<Record> dataList) {
        this.mDataList = dataList;
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_record, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Record record = mDataList.get(position);
        holder.setData(record);

        holder.playTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayRecordActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("record", record);
                intent.putExtras(bundle);//发送数据
                mContext.startActivity(intent);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTV;
        TextView durationTV;
        TextView playTV;

        public ViewHolder(View itemView) {
            super(itemView);
            timeTV = (TextView) itemView.findViewById(R.id.tape_time_tv);
            durationTV = (TextView) itemView.findViewById(R.id.tape_duration_tv);
            playTV = (TextView) itemView.findViewById(R.id.tape_play_tv);
        }

        public void setData(Record record) {
            this.timeTV.setText(record.getStartAtDesc());
            this.durationTV.setText(record.getDurationDesc());
        }
    }

}
