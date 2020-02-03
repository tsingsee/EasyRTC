package com.tsingsee.easyrtc.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.databinding.ItemRoomBinding;
import com.tsingsee.easyrtc.model.RoomBean;

import java.util.List;

public class RoomAdapter extends BaseBindRecyclerViewAdapter<RoomBean> {
    private Context mContext;

    private MyClickListener clickListener;

    public RoomAdapter(Context context, List<RoomBean> mList) {
        super(context, mList);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateMyViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_room, parent, false);

        return new BaseBindingViewHolder(binding);
    }

    @Override
    public void onBindMyViewHolder(RecyclerView.ViewHolder holder, int pos) {
        ItemRoomBinding binding = DataBindingUtil.getBinding(holder.itemView);

        final RoomBean item = mList.get(pos);
        binding.setItem(item);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(pos);
            }
        });

        binding.executePendingBindings();// 数据改变时立即刷新数据
    }

    public void setClickListener(MyClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface MyClickListener {
        void onItemClick(int pos);
    }
}
