package com.tsingsee.easyrtc.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.activity.SettingActivity;
import com.tsingsee.easyrtc.adapter.VideotapeAdapter;
import com.tsingsee.easyrtc.databinding.FragmentRecordBinding;
import com.tsingsee.easyrtc.http.BaseEntity3;
import com.tsingsee.easyrtc.http.BaseObserver3;
import com.tsingsee.easyrtc.http.RetrofitFactory;
import com.tsingsee.easyrtc.model.RoomRecord;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class RecordFragment extends BaseFragment implements View.OnClickListener {
    private FragmentRecordBinding binding;
    private List<RoomRecord> devices;

    public RecordFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_record, container, false);
        binding.setOnClick(this);

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (devices == null) {
                    return;
                }

                List<RoomRecord> res = new ArrayList<>();
                String s = binding.searchEt.getText().toString();
                if (TextUtils.isEmpty(s)) {
                    res = devices;
                } else {
                    for (RoomRecord item : devices) {
                        if (item.getName().contains(s)) {
                            res.add(item);
                        }
                    }
                }

                VideotapeAdapter adapter = new VideotapeAdapter(getContext());
                binding.recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged(res);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        showHub("查询中");
        queryDevices();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:{
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
            }
                break;
            default:

                break;
        }
    }

    public void queryDevices() {
        Observable<BaseEntity3<List<RoomRecord>>> observable = RetrofitFactory.getRetrofitService().queryDevices();
        observable.compose(compose(this.<BaseEntity3<List<RoomRecord>>> bindToLifecycle()))
                .subscribe(new BaseObserver3<List<RoomRecord>>(getContext(), dialog, null, false) {
                    @Override
                    protected void onHandleSuccess(List<RoomRecord> model) {
                        hideHub();

                        binding.activityEmptyView.setVisibility(View.GONE);
                        if (model == null || model.size() == 0) {
                            binding.activityEmptyView.setVisibility(View.VISIBLE);
                            return;
                        }

                        devices = model;

                        LinearLayoutManager manager = new LinearLayoutManager(getContext());
                        binding.recyclerView.setLayoutManager(manager);

                        VideotapeAdapter adapter = new VideotapeAdapter(getContext());
                        binding.recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged(model);
                    }

                    @Override
                    protected void loginSuccess() {
                        queryDevices();
                    }
                });
    }
}
