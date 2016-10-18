package com.mecolab.memeticameandroid.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.R;

import java.util.ArrayList;

public class ConversationAdapter extends ArrayAdapter<Conversation> {
    private LayoutInflater mInflater;
    private ArrayList<Conversation> mConversations;

    public ConversationAdapter(Context context, int resource, ArrayList<Conversation> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mConversations = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.conversation_list_item , parent, false);
        }
        Conversation conversation = mConversations.get(position);
        if (conversation.mHasNewMessage) {
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            view.setBackgroundColor(Color.WHITE);
        }
        TextView conversationTitleView =
                (TextView) view.findViewById(R.id.ConversationListItem_Title);
        conversationTitleView.setText(conversation.mTitle);
        return view;
    }

}
