package com.mecolab.memeticameandroid.Views;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mecolab.memeticameandroid.Models.Message;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.R;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MessageAdapter extends ArrayAdapter<Message> {
    private LayoutInflater mInflater;
    private ArrayList<Message> mMessages;

    static HashMap<Integer, Integer> sLayouts;
    static HashMap<Message.MessageType, Integer> sTypesInt;
    static {
        sLayouts = new HashMap<>();
        sTypesInt = new HashMap<>();
        sTypesInt.put(Message.MessageType.TEXT, 0);
        sLayouts.put(0, R.layout.message_text_list_item);
        sTypesInt.put(Message.MessageType.IMAGE, 1);
        sLayouts.put(1, R.layout.message_image_list_item);
        sTypesInt.put(Message.MessageType.AUDIO, 2);
        sLayouts.put(2, R.layout.message_audio_list_item);
        sTypesInt.put(Message.MessageType.VIDEO, 3);
        sLayouts.put(3, R.layout.message_video_list_item);
        sTypesInt.put(Message.MessageType.OTHER, 4);
        sLayouts.put(4, R.layout.message_other_list_item);
        sTypesInt.put(Message.MessageType.NOT_SET, 5);
        sLayouts.put(5, R.layout.message_text_list_item);
    }

    public MessageAdapter(Context context, int resource, ArrayList<Message> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMessages = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final Message message = mMessages.get(position);

        if (view == null) {
            view = mInflater.inflate(getLayout(message.mType), parent, false);
        }
        if (message.mType == Message.MessageType.TEXT) {
            EmojiconTextView contentView =
                    (EmojiconTextView) view.findViewById(R.id.MessageListItem_Content);
            contentView.setText(message.mContent);
        }
        else if (message.mType == Message.MessageType.IMAGE) {

            ImageView contentView = (ImageView) view.findViewById(R.id.MessageListItem_Content);
            Picasso.with(parent.getContext()).load(message.mContent).into(contentView);
        }
        else if (message.mType == Message.MessageType.VIDEO) {
            ImageView contentView = (ImageView) view.findViewById(R.id.MessageListItem_Content);
            contentView.setImageURI(Uri.parse(message.mContent));
        }
        else if (message.mType == Message.MessageType.OTHER) {
            ImageView contentView = (ImageView) view.findViewById(R.id.MessageListItem_ClipImage);
            TextView fileName = (TextView) view.findViewById(R.id.MessageListItem_FileName);
            fileName.setText(String.valueOf(message.mMimeType.split("/")[1].toUpperCase()));
            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.parse(message.mContent), message.mMimeType);
                    try {
                        getContext().startActivity(i);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Can't open this type of file", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else if (message.mType == Message.MessageType.NOT_SET) {
            TextView contentView = (TextView) view.findViewById(R.id.MessageListItem_Content);
            contentView.setText(message.mContent);
        }

        TextView authorView = (TextView) view.findViewById(R.id.MessageListItem_Author);
        authorView.setText(User.getUserOrCreate(getContext(), message.mSender).mName);

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return Message.MessageType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        Message.MessageType type = mMessages.get(position).mType;
        if (type == null) type = Message.MessageType.TEXT;
        return sTypesInt.get(type);
    }

    private int getLayout(Message.MessageType type) {
        if (type == null) type = Message.MessageType.TEXT;
        return sLayouts.get(sTypesInt.get(type));
    }
}
