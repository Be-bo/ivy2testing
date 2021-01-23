package com.ivy2testing.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class for a 1on1 chatRoom object
 * Features: Firestore Compatible, parcelable
 */
public class Chatroom implements Parcelable {

    protected String id;
    protected List<String> members = new ArrayList<>();

    // Not stored in Firebase
    @Exclude protected Long last_message_timestamp;


    // Needed for Firebase
    public Chatroom(){}


    public Chatroom(String user1, String user2){
        id = UUID.randomUUID().toString();
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
        members = in.createStringArrayList();
        int indicator = in.readInt();
        last_message_timestamp = in.readLong();
        if (indicator == 0) last_message_timestamp = null;
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
        dest.writeStringList(members);
        dest.writeInt((last_message_timestamp == null) ? 0 : 1);
        dest.writeLong((last_message_timestamp == null) ? 0 : last_message_timestamp);
    }
}
