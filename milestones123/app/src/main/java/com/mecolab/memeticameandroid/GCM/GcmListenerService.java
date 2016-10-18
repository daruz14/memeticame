package com.mecolab.memeticameandroid.GCM;

import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.mecolab.memeticameandroid.Activities.MainActivity;
import com.mecolab.memeticameandroid.FileUtils.FileManager;
import com.mecolab.memeticameandroid.MemeticameApplication;
import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.Message;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Networking.NetworkingManager;
import com.mecolab.memeticameandroid.Persistence.ContactManager;
import com.mecolab.memeticameandroid.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * Created by Andres on 04-11-2015.
 */
public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    private static final String TAG = "GcmListenerService";
    private ArrayList<User> participants = new ArrayList<User>();
    private String title;
    private String admin;
    private String createdAt;
    private int conversationId;

    @Override
    public void onMessageReceived(String from, final Bundle data) {

        Log.d(TAG, "From: " + from);
        String debuger = data.getString("collapse_key");
        if (data.getString("collapse_key").equals("new_message")) {
            int conversationId = Integer.valueOf(data.getString("conversation_id"));
            String content = data.getString("message");
            String sender = data.getString("sender");
            int id = Integer.valueOf(data.getString("id"));
            Conversation conversation = Conversation.getConversation(this, conversationId);
            String conversationTitle = "New conversation";
            if (conversation != null) {
                Message.Builder builder = new Message.Builder();
                conversationTitle = conversation.mTitle;
                builder .setSender(sender)
                        .setConversationId(conversationId)
                        .setServerId(id);
                if (data.getString("mime_type").equals("plain/text")) {
                    builder.setContent(content).setMimeType("plain/text");

                }
                else {
                    builder.setMimeType(data.getString("mime_type"))
                            .setContent(NetworkingManager.downloadFile(data.getString("link"), FileManager.BASE_PATH +
                                    "/" + FileManager.generateFileName(data.getString("mime_type"))).toString());
                    content = "File received";
                }
                final Message message = builder.build();
                if (message.save(this)) {
                    Intent intent = new Intent();
                    Intent intent1 = new Intent(getApplicationContext(),MainActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    intent.putExtra("SERVER_ID", message.mServerId);
                    intent.setAction(MemeticameApplication.MESSAGE_RECEIVED_ACTION);
                    sendBroadcast(intent);

                    sendNotification(conversationTitle, content, sender, intent1);
                }
            }
        }
        else if (data.getString("collapse_key").equals("new_group_conversation")) {
            conversationId = Integer.valueOf(data.getString("conversation_id"));
            title = data.getString("title");
            admin = data.getString("admin");
            createdAt = data.getString("created_at");
            String participantsString = data.getString("participants");
            String[] participantsNumber = participantsString.split(",");
            for (String number : participantsNumber) {
                participants.add(User.getUserOrCreate(this, number.replaceAll("\\D+","")));
            }
            if (Conversation.getConversation(this, conversationId) == null) {

                /*Conversation conversation = new Conversation(0, conversationId, title, admin, createdAt,
                        true, participants);
                conversation.save(this);*/
                sendNotificationGroup();
            }
        }
        else if (data.getString("collapse_key").equals("new_two_conversation")) {
            int conversationId = Integer.valueOf(data.getString("conversation_id"));
            String createdAt = data.getString("created_at");
            String participantsString = data.getString("participants");
            String[] participantsNumber = participantsString.split(",");
            ArrayList<User> participants = new ArrayList<>();
            for (String number : participantsNumber) {
                participants.add(User.getUserOrCreate(this, number.replaceAll("\\D+","")));
            }
            User admin = participants.get(1);
            User other = participants.get(0);
            if (participants.get(0).mPhoneNumber.equals(User.getLoggedUser(this).mPhoneNumber)) {
                admin = participants.get(0);
                other = participants.get(1);
            }

            if (Conversation.getConversation(this, conversationId) == null) {
                Conversation conversation = new Conversation(0, conversationId, other.mPhoneNumber,
                        admin.mPhoneNumber, createdAt, true, participants);
                conversation.save(this);
            }
        }
    }

    private void sendNotification( String title, String content, String sender, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(User.getUserOrCreate(this, sender).mName +": " + content)
                        .setContentIntent(pendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
    private void sendNotificationGroup() {
        Intent accept = new Intent();
        accept.putExtra("id",conversationId);
        accept.putExtra("title",title);
        accept.putExtra("admin",admin);
        accept.putExtra("created",createdAt);
        accept.putParcelableArrayListExtra("participants",participants);
        accept.setAction("Accept");
        Intent decline = new Intent();
        decline.putExtra("id",conversationId);
        decline.setAction("Decline");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,12345,accept,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this,12345,decline,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("New Group")
                        .setContentText("Invitation :D")
                        .addAction(R.mipmap.ic_acceptsss,"Accept",pendingIntent)
                        .addAction(R.mipmap.ic_acceptsss, "Decline", pendingIntent1);

        mBuilder.setAutoCancel(true);
        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    /*public class InvitationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("Accept")) {
                int valor = intent.getIntExtra("id",0);
                String title = intent.getStringExtra("title");
                String admin = intent.getStringExtra("admin");
                String createdAt = intent.getStringExtra("created");
                ArrayList<User> participants = intent.getParcelableArrayListExtra("participants");*/
                //Toast.makeText(context, "Accept invitation :D", Toast.LENGTH_SHORT).show();
                //Conversation conversation = new Conversation(0, valor, title, admin, createdAt,
                //        true, participants);
                //conversation.save(context);
           /* }
            else  if (action.equals("Decline")) {
                int valor = intent.getIntExtra("id",0);
                Toast.makeText(context, "Decline Called", Toast.LENGTH_SHORT).show();
            }
        }

    }*/

}
