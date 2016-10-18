package com.mecolab.memeticameandroid.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.mecolab.memeticameandroid.Fragments.ContactsFragment;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.R;

public class ContactsActivity extends AppCompatActivity implements
        ContactsFragment.OnContactSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
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
        Intent data = new Intent();
        data.putExtra(User.PHONE_NUMBER, contact.mPhoneNumber);
        setResult(RESULT_OK, data);
        finish();
    }

}
