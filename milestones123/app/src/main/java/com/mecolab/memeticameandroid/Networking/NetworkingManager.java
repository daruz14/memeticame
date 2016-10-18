package com.mecolab.memeticameandroid.Networking;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mecolab.memeticameandroid.FileUtils.FileManager;
import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.Message;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetworkingManager {

    public static final String BASE_URL = "http://mcctrack4.ing.puc.cl/api/v2/";
    private static final int DEFAULT_TIMEOUT = 10000; //10 seconds
    private static NetworkingManager mInstance;
    private static Context mContext;
    private RequestQueue mRequestQueue;
    private RetryPolicy mRetryPolicy;
    private String mToken;

    private NetworkingManager(Context context){
        mContext = context;
        mRequestQueue = getRequestQueue();
        mRetryPolicy = new DefaultRetryPolicy(DEFAULT_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public static synchronized NetworkingManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetworkingManager(context);
        }
        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    private String getToken(){
        if(mToken != null)
            return mToken;
        else {
            SharedPreferences sharedPref = mContext.getSharedPreferences(
                    mContext.getString(R.string.SharedPreferences_Preferences), Context.MODE_PRIVATE);
            mToken = sharedPref.getString(
                    mContext.getString(R.string.SharedPreferences_Token), "0");
            return mToken;
        }
    }

    private <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(mRetryPolicy);
        getRequestQueue().add(req);
    }

    public void createUser(String phone, String name, String password, final Listeners.OnAuthListener listener) {

        String url = BASE_URL + "users";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone_number", phone);
            jsonObject.put("name", name);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            User user = new User(
                                    0,
                                    response.getInt("id"),
                                    response.getString("phone_number"),
                                    response.getString("name"));
                            String token = response.getString("api_key");

                            SharedPreferences sharedPref = mContext.getSharedPreferences(
                                    mContext.getString(R.string.SharedPreferences_Preferences),
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(mContext.getString(R.string.SharedPreferences_PhoneNumber),
                                    user.mPhoneNumber);
                            editor.putString(mContext.getString(R.string.SharedPreferences_Token),
                                    token);
                            editor.apply();

                            listener.onUserReceived(user);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onUserReceived(null);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }};

        addToRequestQueue(jsObjRequest);
    }

    public void getUsers(final Listeners.ServerGetContactsListener listener){
        String url = BASE_URL + "users";
        JSONObject jsonObject = new JSONObject();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            int len = response.length();
                            ArrayList<User> users = new ArrayList<>();
                            for(int i = 0; i < len; i++){
                                JSONObject json = response.getJSONObject(i);
                                User user = new User(
                                        0,
                                        json.getInt("id"),
                                        json.getString("phone_number"),
                                        json.getString("name"));
                                users.add(user);
                            }
                            listener.onContactsReceived(users);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token token=" + getToken());
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        addToRequestQueue(jsonArrayRequest);
    }

    public void getMessages(int conversationServerId, int lastMessageId,
                            final ArrayList<Message> savedMessages,
                            final Listeners.OnGetMessagesListener listener) {

        String url = BASE_URL +
                "conversations/get_messages?conversation_id="+conversationServerId +
                "&last_message_id=" + lastMessageId;
        JSONObject jsonObject = new JSONObject();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(final JSONArray resp) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<Message> messages = new ArrayList<>();
                                    int size = resp.length();
                                    for(int i = 0; i < size; i++) {
                                        JSONObject response = resp.getJSONObject(i);
                                        String mimeType;
                                        Message.Builder builder = new Message.Builder()
                                                .setServerId(response.getInt("id"));
                                        if (response.isNull("file")) {
                                            mimeType = "plain/text";
                                            builder.setContent(response.getString("content"));
                                        } else {
                                            JSONObject file = response.getJSONObject("file");
                                            mimeType = file.getString("mime_type");
                                            builder.setContent(downloadFile(file.getString("url"),
                                                    FileManager.BASE_PATH + "/" +
                                                            FileManager.generateFileName(mimeType)).toString());

                                        }

                                        Message msg = builder.setSender(response.getString("sender"))
                                                .setMimeType(mimeType)
                                                .setConversationId(response.getInt("conversation_id"))
                                                .build();
                                        boolean messageAlreadyExists = false;

                                        for(Message savedMessage : savedMessages){
                                            if(msg.mServerId == savedMessage.mServerId) {
                                                messageAlreadyExists = true;
                                                break;
                                            }
                                        }
                                        if(messageAlreadyExists){
                                            continue;
                                        }
                                        messages.add(msg);
                                    }
                                    listener.onMessagesReceived(messages);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token token="+getToken());
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        addToRequestQueue(jsonArrayRequest);
    }

    public void sendMessage(Message msg, final Listeners.SendMessageListener listener) {

        String url = BASE_URL + "conversations/send_message";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sender", msg.mSender);
            jsonObject.put("conversation_id", msg.mConversationId);
            if (!msg.mType.equals(Message.MessageType.TEXT)) {
                Uri uri = Uri.parse(msg.mContent);
                try {
                    JSONObject fileObject = new JSONObject();
                    fileObject.put("file_name", uri.getLastPathSegment());
                    fileObject.put("content", FileManager.loadBase64(mContext, uri));
                    fileObject.put("mime_type", msg.mMimeType);
                    jsonObject.put("file", fileObject);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                jsonObject.put("content", msg.mContent);
            }

            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                    Request.Method.POST,
                    url,
                    jsonObject,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(final JSONArray resp) {
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject response = resp.getJSONObject(0);
                                        String mimeType;
                                        Message.Builder builder = new Message.Builder()
                                                .setServerId(response.getInt("id"));
                                        if (response.isNull("file")) {
                                            mimeType = "plain/text";
                                            builder.setContent(response.getString("content"));
                                        }
                                        else{
                                            JSONObject file = response.getJSONObject("file");
                                            mimeType = file.getString("mime_type");
                                            builder.setContent(downloadFile(file.getString("url"),
                                                    FileManager.BASE_PATH + "/" +
                                                            FileManager.generateFileName(mimeType)).toString());

                                        }

                                        Message msg = builder.setSender(response.getString("sender"))
                                                .setMimeType(mimeType)
                                                .setConversationId(response.getInt("conversation_id"))
                                                .build();
                                        listener.onMessageSent(msg);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            thread.start();

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Token token=" + getToken());
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };
            addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deleteParticipant(int conversationId, String phoneNumber,
                                  final Listeners.OnUserAddedListener listener){
        String url = BASE_URL + "conversations/remove_user?phone_number="+phoneNumber+"&conversation_id="+conversationId;
        JSONObject jsonObject = new JSONObject();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.DELETE, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token token="+getToken());
                return headers;
            }};
        addToRequestQueue(jsObjRequest);
    }
    public void acceptInvitation(int conversationId, String phoneNumber,
                                 final Listeners.OnUserAddedListener listener){
        String url = BASE_URL + "conversations/accept_invitation";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone_number", phoneNumber);
            jsonObject.put("conversation_id", conversationId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token token="+getToken());
                headers.put("Accept", "application/json");
                return headers;
            }};

        addToRequestQueue(jsObjRequest);

    }

    public void getConversations(String userPhone, final ArrayList<Conversation> savedConversations,
                                 final Listeners.OnGetConversationsListener listener) {
        String url = BASE_URL + "users/get_conversations?phone_number="+userPhone;
        JSONObject jsonObject = new JSONObject();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<Conversation> conversations = new ArrayList<>();
                            int size = response.length();
                            for(int i = 0; i < size; i++){
                                JSONObject jsonObject = response.getJSONObject(i);
                                String title = jsonObject.getString("title");
                                if (!jsonObject.getBoolean("group")) {
                                    User me = User.getLoggedUser(mContext);
                                    User user1 = User.getUserOrCreate(mContext,
                                            ((JSONObject) jsonObject.getJSONArray("users")
                                                    .get(0)).getString("phone_number"));
                                    User user2 = User.getUserOrCreate(mContext,
                                            ((JSONObject) jsonObject.getJSONArray("users")
                                                    .get(1)).getString("phone_number"));
                                    if (!user1.equals(me)) {
                                        title = user1.mName;
                                    } else {
                                        title = user2.mName;
                                    }
                                }
                                int id = jsonObject.getInt("id");
                                boolean conversationAlreadyExists = false;
                                //boolean conversationNeedsUpdate = false;
                                JSONArray jsonParticipants = jsonObject.getJSONArray("users");
                                ArrayList<User> participants = new ArrayList<>();
                                int len = jsonParticipants.length();
                                for(int j = 0; j < len; j++){
                                    User u = User.getUserOrCreate(mContext,
                                            jsonParticipants.getJSONObject(j).getString("phone_number"),
                                            jsonParticipants.getJSONObject(j).getString("name"));
                                    participants.add(u);
                                }

                                for(Conversation savedConversation: savedConversations){
                                    if(savedConversation.mServerId == id ){
                                        if(savedConversation.mParticipants.size() == participants.size()){
                                            conversationAlreadyExists = true;
                                            break;
                                        }
                                        else{
                                            conversationAlreadyExists = true;
                                            break;
                                        }
                                    }
                                }

                                if(conversationAlreadyExists)
                                    continue;

                                Conversation conversation = new Conversation(
                                        0,
                                        id,
                                        title,
                                        jsonObject.getString("admin"),
                                        jsonObject.getString("created_at"),
                                        jsonObject.getBoolean("group"),
                                        participants);
                                conversations.add(conversation);
                            }
                            listener.onConversationsReceived(conversations);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token token="+getToken());
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        addToRequestQueue(jsonArrayRequest);
    }

    public void newTwoConversation(User user1, User user2, final Listeners.OnGetNewTwoConversationListener listener) {
        String url = BASE_URL + "conversations";
        JSONObject jsonObject = new JSONObject();
        ArrayList<String> usersPhoneNumbers = new ArrayList<>();
        usersPhoneNumbers.add(user1.mPhoneNumber);
        usersPhoneNumbers.add(user2.mPhoneNumber);
        try {
            jsonObject.put("admin", user1.mPhoneNumber);
            jsonObject.put("group", false);
            jsonObject.put("users", new JSONArray(usersPhoneNumbers));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonParticipants = response.getJSONArray("users");
                            ArrayList<User> participants = new ArrayList<>();
                            int len = jsonParticipants.length();
                            for(int i = 0; i < len; i++){
                                User user = User.getUserOrCreate(mContext,
                                        jsonParticipants.getJSONObject(i).getString("phone_number"),
                                        jsonParticipants.getJSONObject(i).getString("name"));
                                participants.add(user);
                            }
                            String title;
                            User me = User.getLoggedUser(mContext);
                            if(!me.equals(participants.get(0))){
                                title = participants.get(0).mName;
                            }
                            else{
                                title = participants.get(1).mName;
                            }
                            Conversation conversation = new Conversation(
                                    0,
                                    response.getInt("id"),
                                    title,
                                    response.getString("admin"),
                                    response.getString("created_at"),
                                    false,
                                    participants);

                            listener.onConversationReceived(conversation);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onConversationReceived(null);

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token token="+getToken());
                headers.put("Accept", "application/json");
                return headers;
            }};
        addToRequestQueue(jsObjRequest);
    }

    public void newGroupConversation(ArrayList<User> users, String title,
                                     final Listeners.OnGetNewGroupConversationListener listener) {
        String url = BASE_URL + "conversations";
        JSONObject jsonObject = new JSONObject();
        try {
            ArrayList<String> jsonUsers = new ArrayList<>();
            for(User u : users){
                jsonUsers.add(u.mPhoneNumber);
            }
            jsonObject.put("admin", users.get(0).mPhoneNumber);
            jsonObject.put("title", title);
            jsonObject.put("group", true);
            jsonObject.put("users", new JSONArray(jsonUsers));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonParticipants = response.getJSONArray("users");
                            ArrayList<User> participants = new ArrayList<>();
                            int len = jsonParticipants.length();
                            for(int i = 0; i < len; i++){
                                User user = User.getUserOrCreate(mContext,
                                        jsonParticipants.getJSONObject(i).getString("phone_number"),
                                        jsonParticipants.getJSONObject(i).getString("name"));
                                participants.add(user);
                            }
                            Conversation conversation = new Conversation(
                                    0,
                                    response.getInt("id"),
                                    response.getString("title"),
                                    response.getString("admin"),
                                    response.getString("created_at"),
                                    true,
                                    participants);
                            listener.onConversationReceived(conversation);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onConversationReceived(null);

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token token="+getToken());
                headers.put("Accept", "application/json");
                return headers;
            }};
        addToRequestQueue(jsObjRequest);
    }

    public void addParticipantToConversation(int conversationId, String phoneNumber,
                                             final Listeners.OnUserAddedListener listener){
        String url = BASE_URL + "conversations/add_user";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone_number", phoneNumber);
            jsonObject.put("conversation_id", conversationId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onUserAdded(true);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onUserAdded(false);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token token="+getToken());
                headers.put("Accept", "application/json");
                return headers;
            }};

        addToRequestQueue(jsObjRequest);
    }

    public void registerGCM(String token) {
        String url = BASE_URL + "users/gcm_register";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone_number", User.getLoggedUser(mContext).mPhoneNumber);
            jsonObject.put("registration_id", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token token="+getToken());
                headers.put("Accept", "application/json");
                return headers;
            }};
        addToRequestQueue(jsObjRequest);
    }

    public static Uri downloadFile(String urlToDownload, String path) {
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(path);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(new File(path));
    }
}