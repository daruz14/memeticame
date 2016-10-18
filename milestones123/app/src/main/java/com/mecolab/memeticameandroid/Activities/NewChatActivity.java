package com.mecolab.memeticameandroid.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mecolab.memeticameandroid.Fragments.ContactsFragment;
import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewChatActivity extends AppCompatActivity implements
        ContactsFragment.OnContactSelectedListener {

    @Bind(R.id.NewChatActivity_CreateGroup)
    Button mCreateGroupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        ButterKnife.bind(this);
        mCreateGroupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewChatActivity.this, NewGroupActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onContactSelected(User contact) {
        Conversation conversation =
                Conversation.getTwoConversation(this, User.getLoggedUser(this), contact);

        if (conversation == null) {
                            Conversation.createNewTwoConversation(this, User.getLoggedUser(this), contact,
                                    new Listeners.OnGetNewTwoConversationListener() {
                                        @Override
                                        public void onConversationReceived(final Conversation conversation) {

                                            if (conversation==null) {
                                                Toast.makeText(getApplicationContext(), "connection failed",
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                            conversation.save(NewChatActivity.this);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getApplicationContext(),
                                            ConversationActivity.class);
                                    intent.putExtra(Conversation.SERVER_ID, conversation.mServerId);
                                    startActivity(intent);
                                }
                            });

                        }
                    });
        } else {
            Intent intent = new Intent(getApplicationContext(),
                    ConversationActivity.class);
            intent.putExtra(Conversation.SERVER_ID, conversation.mServerId);
            startActivity(intent);
        }

    }
}
