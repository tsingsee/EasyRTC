package com.tsingsee.easyrtc.adapter;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

public class BaseBindingViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding binding;

    public ViewDataBinding getBinding() {
        return binding;
    }

    public void setBinding(ViewDataBinding binding) {
        this.binding = binding;
    }

    public BaseBindingViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
