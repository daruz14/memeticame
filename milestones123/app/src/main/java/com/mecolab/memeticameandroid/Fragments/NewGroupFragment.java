package com.mecolab.memeticameandroid.Fragments;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.mecolab.memeticameandroid.Activities.ContactsActivity;
import com.mecolab.memeticameandroid.Activities.ConversationActivity;
import com.mecolab.memeticameandroid.Activities.NewGroupActivity;
import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.R;
import com.mecolab.memeticameandroid.Views.UserAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class NewGroupFragment extends Fragment {
    @Bind(R.id.NewGroupFragment_AddParticipantButton)
    Button mAddParticipantButton;
    @Bind(R.id.NewGroupFragment_GroupParticipants)
    ListView mParticipantsView;
    @Bind(R.id.NewGroupFragment_GroupTitleView)
    EditText mTitleView;

    private UserAdapter mParticipantsAdapter;
    private ArrayList<User> mParticipants;
    public NewGroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_group, container, false);
        ButterKnife.bind(this, view);

        mParticipants = new ArrayList<>();
        mParticipantsAdapter =
                new UserAdapter(getActivity(), R.layout.user_list_item, mParticipants);
        mParticipantsView.setAdapter(mParticipantsAdapter);

        mAddParticipantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactsActivity.class);
                getActivity().startActivityForResult(intent, NewGroupActivity.PARTICIPANT_REQUEST_CODE);
            }
        });
        return view;
    }

    public void createGroup() {
        mParticipants.add(0, User.getLoggedUser(getActivity()));
        Conversation.createNewGroupConversation(getActivity(), mTitleView.getText().toString(),
                mParticipants, new Listeners.OnGetNewGroupConversationListener() {
                    @Override
                    public void onConversationReceived(Conversation conversation) {
                        conversation.save(getActivity());
                        Intent intent = new Intent(getActivity(), ConversationActivity.class);
                        intent.putExtra(Conversation.SERVER_ID, conversation.mServerId);
                        startActivity(intent);
                    }
                });

    }

    public void addParticipant(String number) {
        User user = User.getUser(getActivity(), number);
        mParticipants.add(user);
        mParticipantsAdapter.notifyDataSetChanged();

    }
}
