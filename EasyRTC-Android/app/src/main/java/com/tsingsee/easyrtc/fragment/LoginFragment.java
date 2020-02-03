package com.tsingsee.easyrtc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.tsingsee.easyrtc.R;
import com.tsingsee.easyrtc.model.RoomModel;
import com.tsingsee.rtc.Options;
import com.tsingsee.rtc.Room;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginFragment extends Fragment {
    @BindView(R.id.userNameEditText)
    EditText userName;

    @BindView(R.id.userEmailEditText)
    EditText email;

    @BindView(R.id.roomNumberEditText)
    EditText roomNumber;

    @BindView(R.id.userIdEditText)
    EditText userId;

    @BindView(R.id.userPasswordEditText)
    EditText userPassword;
    @BindView(R.id.serverEditText)
    EditText serverEditText;

    Room room;
    private Options options;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);

        room = RoomModel.getInstance().getRoom();

        options = new Options(getContext());
        userName.setText(options.displayName);
        email.setText(options.userEmail);
        userId.setText(options.username);
        userPassword.setText(options.password);
        roomNumber.setText(options.roomNumber);
        serverEditText.setText(options.serverAddress);

        return view;
    }

    @OnClick(R.id.call)
    public void onCallClick() {
        options.displayName = userName.getEditableText().toString();
        options.userEmail = email.getEditableText().toString();
        options.roomNumber = roomNumber.getEditableText().toString();
        options.username = userId.getEditableText().toString();
        options.password = userPassword.getEditableText().toString();
        options.serverAddress = serverEditText.getEditableText().toString();

        options.displayName = options.username;
        options.userEmail = options.username + "@easydarwin.org";
        if (TextUtils.isEmpty(options.username)) {
            Toast.makeText(getContext(), "账号不可为空",Toast.LENGTH_SHORT).show();
            return;
        }
        room.setOptions(options);
        room.join();
        options.save();
    }
}
