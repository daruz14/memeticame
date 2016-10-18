package com.mecolab.memeticameandroid.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.mecolab.memeticameandroid.MemeticameApplication;
import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.Message;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.R;
import com.mecolab.memeticameandroid.Views.MessageAdapter;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConversationFragment extends Fragment{
    @Bind(R.id.ConversationFragment_MessagesView)
    ListView mMessagesView;
    @Bind(R.id.ConversationFragment_NewMessageView)
    EmojiconEditText mNewMessageView;
    @Bind(R.id.ConversationFragment_SendButton)
    ImageButton mSendButton;
    @Bind(R.id.emoticon_button)
    ImageButton mEmoticonButton;
    @Bind(R.id.emojicons)
    FrameLayout mEmoticonLayout;

    public static final String SHARED_TEXT = "shared_text";
    public static final String SHARED_IMAGE_URI = "shared_uri";
    public static final String SHARED_TYPE = "shared_type";

    private ArrayList<Message> mMessages;
    private MessageAdapter mMessagesAdapter;
    private User mLoggedUser;
    private Conversation mConversation;
    MessageReceiver messageReceiver;
    private boolean mSent = false;

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        ButterKnife.bind(this, view);
        mLoggedUser = User.getLoggedUser(getActivity());
        setConversation();
        setViews();
        if (getActivity() == null) return view;
        getActivity().setTitle(mConversation.mTitle);
        setListeners();
        messageReceiver = new MessageReceiver();
        getActivity().registerReceiver(messageReceiver,
                new IntentFilter(MemeticameApplication.MESSAGE_RECEIVED_ACTION));
        checkSharedMedia();
        return view;
    }

    private void checkSharedMedia() {
        Bundle extras = getActivity().getIntent().getExtras();
        String type = extras.getString(SHARED_TYPE);
        if (type != null && !mSent) {
            if (type.equals("text/plain")) {
                String text = extras.getString(SHARED_TEXT);
                mNewMessageView.setText(text);
                sendMessage();
            }
            else if (type.startsWith("image/")) {
                Uri uri = Uri.parse(extras.getString(SHARED_IMAGE_URI));
                sendFileMessage(uri, "image/jpeg");
            }
            mSent = true;
        }
    }

    @Override
    public void onPause() {
        try {
            getActivity().unregisterReceiver(messageReceiver);
        } catch (IllegalArgumentException e) {
            super.onPause();
        }
        super.onPause();
    }


    public void onEmojiconBackspaceClicked(View view) {
        EmojiconsFragment.backspace(mNewMessageView);

    }

    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mNewMessageView, emojicon);

    }

    private void setEmojiconFragment(boolean useSystemDefault) {
        if (mEmoticonLayout.getVisibility() == FrameLayout.GONE) {
            mEmoticonLayout.setVisibility(FrameLayout.VISIBLE);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                    .commit();
        } else if (mEmoticonLayout.getVisibility() == FrameLayout.VISIBLE) {
            mEmoticonLayout.setVisibility(FrameLayout.GONE);
        }

    }

    public class MessageReceiver extends BroadcastReceiver {
        public MessageReceiver() { }
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MemeticameApplication.MESSAGE_RECEIVED_ACTION)) {
                addGcmMessage(intent);
            }
        }
    }

    private void addGcmMessage(Intent intent) {
        final Message message = Message.getMessage(getActivity(),
                intent.getIntExtra("SERVER_ID", -1));
        if (message.mConversationId == mConversation.mServerId) {
            mMessages.add(message);
            mMessagesAdapter.notifyDataSetChanged();
            mMessagesView.setSelection(mMessagesAdapter.getCount() - 1);

        }
    }

    private void setViews() {
        mMessages = Message.getMessages(getActivity(), mConversation,
                new Listeners.OnGetMessagesListener() {
            @Override
            public void onMessagesReceived(final ArrayList<Message> messages) {
                if (getActivity() == null) return;
                Collections.sort(messages, new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        return o1.mServerId - o2.mServerId;
                    }
                });
                for (Message message : messages) {
                    if (message.save(getActivity())) {
                        mMessages.add(message);
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMessagesAdapter.notifyDataSetChanged();
                        mMessagesView.setSelection(mMessagesAdapter.getCount() - 1);
                    }
                });
            }
        });
        mMessagesAdapter =
                new MessageAdapter(getActivity(), R.layout.message_text_list_item, mMessages);
        mMessagesView.setAdapter(mMessagesAdapter);
        mMessagesView.setSelection(mMessagesAdapter.getCount() - 1);
    }

    private void setConversation() {
        int conversationServerId = getActivity().getIntent().getIntExtra(Conversation.SERVER_ID, 0);
        mConversation = Conversation.getConversation(getActivity(), conversationServerId);
    }

    private void setListeners() {
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mEmoticonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEmojiconFragment(false);
            }
        });

    }

    private void sendMessage() {
        if (mConversation == null) {
            Toast.makeText(getActivity(),
                    "Wait a moment, the conversation is being created.",
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Message.Builder builder = new Message.Builder();
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        String stringDate = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        Message message = builder.setContent(mNewMessageView.getText().toString())
                .setSender(mLoggedUser.mPhoneNumber)
                .setMimeType("plain/text")
                .setConversationId(mConversation.mServerId)
                .setDate(stringDate)
                .build();
        if (message.mContent.equals(""))
            return;
        if (message.mType != Message.MessageType.TEXT) {
            mMessages.add(message);
            mMessagesAdapter.notifyDataSetChanged();
        }
        message.sendMessage(getActivity(), new Listeners.SendMessageListener() {
            @Override
            public void onMessageSent(final Message msg) {
                if (msg.save(getActivity())) {
                    if (msg.mType == Message.MessageType.TEXT && getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMessages.add(msg);
                                mMessagesAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
        mNewMessageView.setText("");
    }

    public void sendFileMessage(Uri uri, String mimeType) {
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        String stringDate = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        Message.Builder builder = new Message.Builder();
        Message message = builder
                .setContent(uri.toString())
                .setConversationId(mConversation.mServerId)
                .setDate(stringDate)
                .setMimeType(mimeType)
                .setSender(User.getLoggedUser(getActivity()).mPhoneNumber)
                .build();
        if (message.mType != Message.MessageType.TEXT) {
            mMessages.add(message);
            mMessagesAdapter.notifyDataSetChanged();
        }
        message.sendMessage(getActivity(), new Listeners.SendMessageListener() {
            @Override
            public void onMessageSent(Message msg) {
                if (msg.mType == Message.MessageType.TEXT && getActivity() != null
                        && msg.save(getActivity())) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMessagesAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    public boolean isGroupConversation(){
        return mConversation.mIsGroup;
    }

   /*Ojo: si devuelve true quiere decir que se puede agregar, no qu se agrego*/
    public boolean addParticipant(String number, final Listeners.OnUserAddedListener listener) {
        User user = User.getUser(getActivity(), number);
        return mConversation.addParticipant(getActivity(), user, listener);
    }
}
