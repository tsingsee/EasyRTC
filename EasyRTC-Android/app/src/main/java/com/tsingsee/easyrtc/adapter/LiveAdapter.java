package com.tsingsee.easyrtc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.model.LiveSessionModel;

import java.util.List;

public class LiveAdapter extends RecyclerView.Adapter<LiveAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;

    private List<LiveSessionModel.Session> mDataList;

    public LiveAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void notifyDataSetChanged(List<LiveSessionModel.Session> dataList) {
        this.mDataList = dataList;
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_live, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LiveSessionModel.Session item = mDataList.get(position);
        holder.setData(item);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(mContext, RecordTimeAxisActivity.class);
//                intent.putExtra("id", item);
//                mContext.startActivity(intent);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView room_no_tv;
        TextView room_duration_tv;
        TextView room_time_tv;

        public ViewHolder(View itemView) {
            super(itemView);
            room_no_tv = (TextView) itemView.findViewById(R.id.room_no_tv);
            room_duration_tv = (TextView) itemView.findViewById(R.id.room_duration_tv);
            room_time_tv = (TextView) itemView.findViewById(R.id.room_time_tv);
        }

        public void setData(LiveSessionModel.Session data) {
            this.room_no_tv.setText("会议室号：" + data.getId());
            this.room_duration_tv.setText("会议时长：" + data.getTime());
            this.room_time_tv.setText("推送码率：" + data.getInBitrate() / 1000 + "KB");
        }
    }
}
