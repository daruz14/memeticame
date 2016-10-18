package com.mecolab.memeticameandroid.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.mecolab.memeticameandroid.Fragments.NewGroupFragment;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.R;

public class NewGroupActivity extends AppCompatActivity {
    public static final int PARTICIPANT_REQUEST_CODE = 1233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_group, menu);
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
        else if (id == R.id.action_create_group) {
            // TODO: 27-10-2015 Pass relevant data
            NewGroupFragment newGroupFragment = (NewGroupFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.NewGroupActivity_NewGroupFragment);
            newGroupFragment.createGroup();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NewGroupActivity.PARTICIPANT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                NewGroupFragment newGroupFragment = (NewGroupFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.NewGroupActivity_NewGroupFragment);
                newGroupFragment.addParticipant(data.getStringExtra(User.PHONE_NUMBER));
            }
        }
    }
}
