package com.mecolab.memeticameandroid.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mecolab.memeticameandroid.Fragments.ContactsFragment;
import com.mecolab.memeticameandroid.Fragments.ConversationsFragment;
import com.mecolab.memeticameandroid.GCM.RegistrationIntentService;
import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.R;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        ContactsFragment.OnContactSelectedListener,
        ConversationsFragment.OnConversationSelectedListener {

    public static final int ADD_CONTACT_REQUEST = 0;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        User loggedUser = User.getLoggedUser(this);
        if (loggedUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        final ActionBar actionBar = getSupportActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        if (position < actionBar.getTabCount()) {
                            actionBar.setSelectedNavigationItem(position);
                        }
                    }
                });

        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        actionBar.addTab(
                actionBar.newTab()
                        .setText(R.string.MainActivity_TabConversations)
                        .setTabListener(tabListener));

        actionBar.addTab(
                actionBar.newTab()
                        .setText(R.string.MainActivity_TabContacts)
                        .setTabListener(tabListener));

        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        else if (id == R.id.action_new_two_conversation) {
            Intent intent = new Intent(this, NewChatActivity.class);
            startActivity(intent);
            return true;
        }

        else if (id == R.id.action_refresh_contacts) {
            if (mViewPager != null) {
                Fragment contactsFragment = mSectionsPagerAdapter.
                        getRegisteredFragment(mViewPager.getCurrentItem());
                if (contactsFragment != null && contactsFragment.getClass().equals(ContactsFragment.class)) {
                    ((ContactsFragment)contactsFragment).refreshContacts();
                }
            }
            return true;
        }

        else if (id == R.id.add_contact) {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            intent.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
            startActivityForResult(intent, ADD_CONTACT_REQUEST);
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
                            if(conversation == null) {
                                Toast.makeText(getApplicationContext(), "connection failed",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            conversation.save(MainActivity.this);
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

    @Override
    public void onConversationSelected(Conversation conversation) {
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra(Conversation.SERVER_ID, conversation.mServerId);
        startActivity(intent);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return ConversationsFragment.newInstance(position + 1);
                case 1:
                    return ContactsFragment.newInstance(position + 1);
                default:
                    return null;
            }
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.MainActivity_TabConversations).toUpperCase(l);
                case 1:
                    return getString(R.string.MainActivity_TabContacts).toUpperCase(l);
            }
            return null;
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}
