package com.ivy2testing.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for a 1on1 chatRoom object
 * Features: Firestore Compatible, parcelable
 */
public class Chatroom implements Parcelable {

    protected String id;
    protected List<String> members = new ArrayList<>();

    // Not stored in Firebase
    protected Long last_message_timestamp;


    // Needed for Firebase
    public Chatroom(){}


    public Chatroom(String user1, String user2){
        members.add(user1);
        members.add(user2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chatroom chatroom = (Chatroom) o;
        return id.equals(chatroom.id);
    }

    /* Getters and Setters
     ***************************************************************************************************/

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public Long getLast_message_timestamp() {
        return last_message_timestamp;
    }

    public void setLast_message_timestamp(Long last_message_timestamp) {
        this.last_message_timestamp = last_message_timestamp;
    }

    public List<String> getMembers() {
        if (members == null) members = new ArrayList<>();
        return new ArrayList<>(members);
    }

    public void addMember(String newMember){
        if (newMember != null) members.add(newMember);
        else Log.w("Chatroom", "newMember was null!");
    }

    public void removeMember(String member){
        members.remove(member);
    }

    /* Parcel related Methods
     ***************************************************************************************************/

    protected Chatroom(Parcel in) {
        id = in.readString();
        last_message_timestamp = in.readLong();
        members = in.createStringArrayList();
    }

    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel in) {
            return new Chatroom(in);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(last_message_timestamp);
        dest.writeStringList(members);
    }
}
