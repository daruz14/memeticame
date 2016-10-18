package com.mecolab.memeticameandroid.Models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.Networking.NetworkingManager;
import com.mecolab.memeticameandroid.Persistence.ContactManager;
import com.mecolab.memeticameandroid.Persistence.DatabaseManager;
import com.mecolab.memeticameandroid.R;

import java.util.ArrayList;



public class User implements Parcelable{

    public static final String PHONE_NUMBER = "phone_number";

    public final int mId;
    public final int mServerId;
    public final String mPhoneNumber;
    public final String mName;

    public User(int id, int serverId, String phoneNumber, String name) {
        mId = id;
        mServerId = serverId;
        mPhoneNumber = phoneNumber;
        mName = name;
    }

    protected User(Parcel in) {
        mId = in.readInt();
        mServerId = in.readInt();
        mPhoneNumber = in.readString();
        mName = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public static User getUser(Context context, String phoneNumber) {
        return DatabaseManager.getInstance(context).getContact(phoneNumber);
    }

    public static User getUserOrCreate(Context context, String phoneNumber, String name){
        User user = DatabaseManager.getInstance(context).getContact(phoneNumber);
        if (user != null)
            return user;
        else return new User(0,0,phoneNumber,name);
    }

    public static User getUserOrCreate(Context context, String phoneNumber){
        User user = DatabaseManager.getInstance(context).getContact(phoneNumber);
        if (user != null)
            return user;
        else return new User(0,0,phoneNumber, phoneNumber);
    }

    //intenta guardar en la base de datos, revisando que no exista uno con el mismo numero
    public void save(Context context){
        DatabaseManager.getInstance(context).insertContact(this);
    }

    public static ArrayList<User> getContacts (Context context,
                                               ContactManager.ContactsProviderListener listener) {

        ContactManager.getContacts(context, listener);
        return DatabaseManager.getInstance(context).getContacts();
    }

    public static User getLoggedUser(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.SharedPreferences_Preferences), Context.MODE_PRIVATE);
        String myPhone = sharedPref.getString(
                context.getString(R.string.SharedPreferences_PhoneNumber), "0");
        String myToken = sharedPref.getString(
                context.getString(R.string.SharedPreferences_Token), "0");
        if (myPhone.equals("0") || myToken.equals("0"))
            return null;
        return DatabaseManager.getInstance(context).getContact(myPhone);
    }

    public static void setLoggedUser(Context context, String name, String phone, String password,
                                     Listeners.OnAuthListener listener) {

        NetworkingManager.getInstance(context).createUser(phone, name, password, listener);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof User)) return false;
        User otherUser = (User)other;
        return otherUser.mPhoneNumber.replaceAll("[^0-9.]", "").equals(mPhoneNumber.replaceAll("[^0-9.]", ""));
    }

    @Override
    public int hashCode(){
        return mPhoneNumber.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(mServerId);
        dest.writeString(mPhoneNumber);
        dest.writeString(mName);
    }
}
