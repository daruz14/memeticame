package com.mecolab.memeticameandroid;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Networking.NetworkingManager;

import java.util.ArrayList;

/**
 * Created by daruz14 on 17-10-16.
 */

public class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("Accept")) {
                int valor = intent.getIntExtra("id",0);
                String title = intent.getStringExtra("title");
                String admin = intent.getStringExtra("admin");
                String createdAt = intent.getStringExtra("created");
                ArrayList<User> participants = intent.getParcelableArrayListExtra("participants");
                Conversation conversation = new Conversation(0, valor, title, admin, createdAt,
                        true, participants);
                conversation.save(context);
                new AcceptInvitationTask(context,valor).execute();
                Toast.makeText(context, "Accept invitation :D", Toast.LENGTH_SHORT).show();
            }
            else  if (action.equals("Decline")) {
                int valor = intent.getIntExtra("id",0);
                new DeclineInvitationTask(context,valor).execute();
                Toast.makeText(context, "Decline Called", Toast.LENGTH_SHORT).show();
            }
        }

        private static class DeclineInvitationTask extends AsyncTask<String, Void, Void> {

            private Context context;
            private ContentResolver mResolver;
            private int id_conversation;
            private User mPhoneContacts;

            public DeclineInvitationTask(Context context, int conversation){
                super();
                this.context = context;
                this.mResolver = context.getContentResolver();
                this.id_conversation = conversation;
            }

            @Override
            protected Void doInBackground(String... params) {
                mPhoneContacts= User.getLoggedUser(context);
                NetworkingManager.getInstance(context).deleteParticipant(id_conversation,mPhoneContacts.mPhoneNumber,null);
                return null;
            }

            //ACÁ IMPLEMENTAR PARA QUE SE OBTENGA A SI MISMO Y SE ELIMINE DEL GRUPO :D
        }

        private static class AcceptInvitationTask extends AsyncTask<String, Void, Void>{

            private Context context;
            private int id_conversation;
            private User mPhoneContacts;

            public AcceptInvitationTask(Context context, int conversation){
                super();
                this.context = context;
                this.id_conversation = conversation;
            }

            @Override
            protected Void doInBackground(String... params) {
                mPhoneContacts= User.getLoggedUser(context);
                NetworkingManager.getInstance(context).acceptInvitation(id_conversation,mPhoneContacts.mPhoneNumber,null);
                return null;
            }

            //ACÁ IMPLEMENTAR PARA QUE SE OBTENGA A SI MISMO Y SE ELIMINE DEL GRUPO :D
        }
}
