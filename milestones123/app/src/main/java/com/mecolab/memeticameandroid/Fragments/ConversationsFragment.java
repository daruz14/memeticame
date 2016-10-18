package com.mecolab.memeticameandroid.Fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mecolab.memeticameandroid.Activities.ConversationActivity;
import com.mecolab.memeticameandroid.MemeticameApplication;
import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.Message;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.R;
import com.mecolab.memeticameandroid.Views.ConversationAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConversationsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    @Bind(R.id.ConversationsFragment_ConversationsView)
    ListView mConversationsView;

    private ConversationAdapter mAdapter;
    private ArrayList<Conversation> mConversations;
    private User mLoggedUser;

    MessageReceiver messageReceiver;

    OnConversationSelectedListener mListener;

    public interface OnConversationSelectedListener {
        void onConversationSelected(Conversation conversation);
    }

    public static ConversationsFragment newInstance(int position) {
        ConversationsFragment fragment = new ConversationsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }

    public ConversationsFragment() {
        // Required empty public constructor
    }

    Listeners.OnGetConversationsListener mOnGetConversationsListener =
            new Listeners.OnGetConversationsListener() {
                @Override
                public void onConversationsReceived(ArrayList<Conversation> conversations) {
                    for (Conversation conversation : conversations) {
                        if (mConversations.size() == 0) {
                            mConversations.add(conversation);
                        } else {
                            mConversations.add(0, conversation);
                        }
                        conversation.save(getActivity());
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversations, container, false);
        ButterKnife.bind(this, view);
        mLoggedUser = User.getLoggedUser(getContext());
        setConversations();
        setViews();
        setListeners();
        return view;
    }

    @Override
    public void onResume() {
        messageReceiver = new MessageReceiver();
        getActivity().registerReceiver(messageReceiver,
                new IntentFilter(MemeticameApplication.MESSAGE_RECEIVED_ACTION));
        super.onResume();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(messageReceiver);
        super.onPause();
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MemeticameApplication.MESSAGE_RECEIVED_ACTION)) {
                final Message message = Message.getMessage(context,
                        intent.getIntExtra("SERVER_ID", -1));
                for (int i = 0; i < mConversations.size(); i++) {
                    if (message.mConversationId == mConversations.get(i).mServerId) {
                        mConversations.get(i).mHasNewMessage = true;
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private void setConversations() {
        mConversations = Conversation.getConversations(getContext(), mLoggedUser,
                mOnGetConversationsListener);
    }

    private void setViews() {
        mAdapter = new ConversationAdapter(getActivity(), R.layout.conversation_list_item,
                mConversations);
        mConversationsView.setAdapter(mAdapter);
    }

    private void setListeners() {
        mConversationsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mConversations.get(position).mHasNewMessage = false;
                mAdapter.notifyDataSetChanged();
                mListener.onConversationSelected(mConversations.get(position));
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnConversationSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
}
