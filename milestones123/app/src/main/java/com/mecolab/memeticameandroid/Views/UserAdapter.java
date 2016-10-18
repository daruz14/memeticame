package com.mecolab.memeticameandroid.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.R;

import java.util.ArrayList;
import java.util.List;


public class UserAdapter extends ArrayAdapter<User> {
    private LayoutInflater mInflater;
    private ArrayList<User> mUsers;

    public UserAdapter(Context context, int resource, ArrayList<User> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUsers = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.user_list_item, parent, false);
        }
        User user = mUsers.get(position);

        TextView nameView = (TextView) view.findViewById(R.id.UserListItem_UserName);
        TextView phoneView = (TextView) view.findViewById(R.id.UserListItem_UserPhone);

        nameView.setText(user.mName);
        phoneView.setText(user.mPhoneNumber);

        return view;
    }
}
