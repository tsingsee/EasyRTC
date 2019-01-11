package com.easydarwin.easyrtc;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easyrtc.venus.Room;
import com.easyrtc.venus.UserInfo;
import com.easyrtc.venus.UserInfoSink;
import com.easyrtc.easyrtc.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserInfoDialogFragment extends Fragment
        implements AdapterView.OnItemClickListener, UserInfoSink {
    Room room;

    @BindView(R.id.userList)
    RecyclerView userList;

    static class UserInfoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.nameText)
        TextView name;

        @BindView(R.id.emailText)
        TextView email;

        @BindView(R.id.energy)
        ProgressBar progressBar;

        public UserInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    RecyclerView.Adapter<UserInfoViewHolder> userAdapter;
    ArrayList<UserInfo> users = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userinfo, container, false);
        ButterKnife.bind(this, view);

        room = RoomModel.getInstance().getRoom();

        userAdapter = new RecyclerView.Adapter<UserInfoViewHolder>() {
            @NonNull
            @Override
            public UserInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                return new UserInfoViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.userinfo_item, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull UserInfoViewHolder holder, int position) {
                UserInfo info = users.get(position);
                holder.name.setText(info.getDisplayName());
                holder.email.setText(info.getUserEmail());

//                if (info.isMute())
//                    holder.itemView.setBackgroundResource(R.color.red);
//                else
//                    holder.itemView.setBackgroundResource(R.color.green);
                holder.progressBar.setProgress(info.getEnergy());
            }

            @Override
            public int getItemCount() {
                return users.size();
            }
        };
        userList.setLayoutManager(new GridLayoutManager(getContext(),3));
        userList.setAdapter(userAdapter);
//        userList.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        room.setUserInfoSink(this);
        updateView();
    }

    @Override
    public void onStop() {
        super.onStop();

        room.setUserInfoSink(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserInfo userInfo = room.getUserInfo().get(position);
        if (userInfo.isMute())
            room.unmute(userInfo);
        else
            room.mute(userInfo);
    }

    @Override
    public void onRoomUserJoin(UserInfo info) {
        updateView();
    }

    @Override
    public void onRoomUserLeave(UserInfo info) {
        updateView();
    }

    @Override
    public void onRoomUserModify(UserInfo info) {
        updateView();
    }

    private void updateView() {
//        userAdapter.clear();
//        userAdapter.addAll(room.getUserInfo());
        users.clear();
        users.addAll(room.getUserInfo());
        userAdapter.notifyDataSetChanged();
    }

}
