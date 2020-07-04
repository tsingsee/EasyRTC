package com.tsingsee.easyrtc.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.activity.Record.RecordTimeAxisActivity;
import com.tsingsee.easyrtc.model.RoomRecord;

import java.util.List;

public class VideotapeAdapter extends RecyclerView.Adapter<VideotapeAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;

    private List<RoomRecord> mDataList;

    public VideotapeAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void notifyDataSetChanged(List<RoomRecord> dataList) {
        this.mDataList = dataList;
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_videotape, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RoomRecord item = mDataList.get(position);
        holder.setData(item.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, RecordTimeAxisActivity.class);
                intent.putExtra("id", item);
                mContext.startActivity(intent);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView room_no_tv;

        public ViewHolder(View itemView) {
            super(itemView);
            room_no_tv = (TextView) itemView.findViewById(R.id.room_no_tv);
        }

        public void setData(String data) {
//            this.room_no_tv.setText("会议室号：" + data);
            this.room_no_tv.setText(data);
        }
    }
}
