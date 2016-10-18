package com.mecolab.memeticameandroid.Activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.mecolab.memeticameandroid.FileUtils.FileManager;
import com.mecolab.memeticameandroid.Fragments.ConversationFragment;
import com.mecolab.memeticameandroid.MemeticameApplication;
import com.mecolab.memeticameandroid.Models.Message;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.R;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import java.io.File;
import java.io.IOException;

public class ConversationActivity extends AppCompatActivity  implements
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener  {
    public static final int GENERIC_FILE_CODE = 0;
    public static final int PICK_IMAGE_CODE = 1;
    public static final int TEXT_MEME_CODE = 2;
    public static final int PICK_MEME_IMAGE_CODE = 3;
    public static final int PICK_MEME_AUDIO_CODE = 4;
    public static final int PICK_CONTACT_CODE = 5;
    public static final int PICK_PUBLIC_MEME = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
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
        else if (id == R.id.action_send_photo) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_CODE);
            return true;
        }
        else if (id == R.id.action_send_generic_file) {
            // This always works
            Intent i = new Intent(this, FilePickerActivity.class);
            // This works if you defined the intent filter
            // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

            // Set these depending on your use case. These are the defaults.
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

            // Configure initial directory by specifying a String.
            // You could specify a String like "/storage/emulated/0/", but that can
            // dangerous. Always use Android's API calls to get paths to the SD-card or
            // internal memory.
            i.putExtra(FilePickerActivity.EXTRA_START_PATH,
                    Environment.getExternalStorageDirectory().getPath());

            startActivityForResult(i, GENERIC_FILE_CODE);
            return true;
        }
        else if (id == R.id.action_invite_contact) {
            ConversationFragment conversationFragment =
                    (ConversationFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.ChatActivity_ConversationFragment);

            if(!conversationFragment.isGroupConversation())
            {
                Toast.makeText(this, "This isn't a group conversation.", Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivityForResult(intent, PICK_CONTACT_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i("canvas act onActRes", "requestCode:"+"requestCode");// 2

        if (requestCode == GENERIC_FILE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri));
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            ConversationFragment conversationFragment =
                    (ConversationFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.ChatActivity_ConversationFragment);
            conversationFragment.sendFileMessage(uri, type);
        }
        else if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String mimeType = getContentResolver().getType(uri);
            ConversationFragment conversationFragment =
                    (ConversationFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.ChatActivity_ConversationFragment);
            conversationFragment.sendFileMessage(uri, mimeType);
        }
        else if (requestCode == PICK_CONTACT_CODE && resultCode == RESULT_OK) {
            String number = data.getStringExtra(User.PHONE_NUMBER);

            ConversationFragment conversationFragment =
                    (ConversationFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.ChatActivity_ConversationFragment);

            boolean exists = conversationFragment.addParticipant(number, new Listeners.OnUserAddedListener() {
                @Override
                public void onUserAdded(boolean successful) {
                    if(successful){
                        Toast.makeText(ConversationActivity.this,
                                "Contact added successfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ConversationActivity.this,
                                "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            if (!exists) {
                Toast.makeText(ConversationActivity.this,
                        "Contact already in group", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onEmojiconBackspaceClicked(View view) {
        ConversationFragment conversationFragment =
                (ConversationFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.ChatActivity_ConversationFragment);
        conversationFragment.onEmojiconBackspaceClicked(view);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        ConversationFragment conversationFragment =
                (ConversationFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.ChatActivity_ConversationFragment);
        conversationFragment.onEmojiconClicked(emojicon);
    }
}
