package com.mecolab.memeticameandroid.Persistence;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.Networking.NetworkingManager;

import java.util.ArrayList;

/**
 * Created by crojas on 29-10-15.
 */
public class ContactManager {

    public ContactManager(){
    }

    public static void getContacts(Context context, ContactsProviderListener listener){

        new GetContactsTask(context, listener).execute();
    }

    public interface ContactsProviderListener{
        void OnContactsReady(ArrayList<User> users);
    }

    private static class GetContactsTask extends AsyncTask<String, Void, ArrayList<User>>{

        private Context context;
        private ContactsProviderListener listener;
        private ContentResolver mResolver;
        private ArrayList<User> mPhoneContacts;

        public GetContactsTask(Context context, ContactsProviderListener listener){
            super();
            this.context = context;
            this.listener = listener;
            this.mResolver = context.getContentResolver();
        }

        @Override
        protected ArrayList<User> doInBackground(String... params) {
            mPhoneContacts = getPhoneContacts(context);
            NetworkingManager.getInstance(context).getUsers(new Listeners.ServerGetContactsListener() {
                @Override
                public void onContactsReceived(ArrayList<User> users) {
                    ArrayList<User> final_contacts = new ArrayList<>();
                    for(User u : users){
                        for(User contact : mPhoneContacts){
                            if(u.equals(contact)){
                                if (!User.getLoggedUser(context).equals(u)){
                                    final_contacts.add(new User(u.mId, u.mServerId, u.mPhoneNumber, contact.mName));
                                }
                                break;
                            }
                        }
                    }
                    listener.OnContactsReady(final_contacts);
                }
            });
            return null;
        }

        public ArrayList<User> getPhoneContacts(Context context) {
            if (mResolver == null) mResolver = context.getContentResolver();
            ArrayList<User> contacts = new ArrayList<>();
            Cursor cursor = mResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if(cursor.moveToFirst()) {
                do {
                    User contact = getContact(cursor);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                } while (cursor.moveToNext()) ;
            }
            cursor.close();
            return contacts;
        }

        private User getContact(Cursor cursor) {
            User contact = null;
            String id = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts._ID));
            if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor c = mResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null);
                if (c.moveToNext()) {
                    String contactNumber = c.getString(c.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String name = c.getString(c.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    contact = new User(0, 0, contactNumber, name);
                }
                c.close();
            }
            return contact;
        }
    }
}
