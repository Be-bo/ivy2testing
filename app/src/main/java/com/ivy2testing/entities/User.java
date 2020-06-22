package com.ivy2testing.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: Abstract Parent Class of Organization and Student 
 * Features: Parcelable (can pass as intent Extra)
 */
public class User implements Parcelable {

    // Fields
    protected String id;
    protected String email;
    protected String name;
    protected String uni_domain;
    protected long registration_millis = 0;
    protected String messaging_token;
    protected boolean is_organization;
    protected boolean is_club;
    protected boolean is_banned = false;
    protected String registration_platform = "Android";

    /* Constructors
     ***************************************************************************************************/

    public User(){
    }
    
    // Use for FireStore constructors!
    public User(boolean is_organization){
        this.is_organization = is_organization;
    }

    // Use for registering new users
    public User(String id, String email, boolean is_organization, boolean is_club){
        this.id = id;
        this.email = email;
        this.is_organization = is_organization;
        this.is_club = is_club;

        // Get Domain
        String[] splitEmail = email.split("@");
        if (splitEmail.length > 1) this.uni_domain = email.split("@")[1];

        this.name = splitEmail[0];      // Set a default name
        this.registration_millis = System.currentTimeMillis();
    }


/* Getters
***************************************************************************************************/

    // Don't write ID in database! (redundant)
    @Exclude
    public String getId() {
        return id;
    }

    public String getUni_domain() {
        return uni_domain;
    }

    public long getRegistration_millis() {
        return registration_millis;
    }

    public String getName() {
        if (name == null) name = email.split("@")[0];
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMessaging_token() {
        return messaging_token;
    }

    public void setMessaging_token(String messaging_token) {
        this.messaging_token = messaging_token;
    }

    public boolean getIs_organization() {
        return is_organization;
    }

    public boolean getIs_banned() {
        return is_banned;
    }



    public String getRegistration_platform() {
        return registration_platform;
    }

    public boolean getIs_club() {
        return is_club;
    }


/* Setters
***************************************************************************************************/

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        if (email.contains("@") && email.contains("."))
            this.email = email;
    }

    public void setUni_domain(String domain) {
        this.uni_domain = domain;
    }


/* Parcelable Methods
 ***************************************************************************************************/

    protected User(Parcel in) {
        id = in.readString();
        email = in.readString();
        name = in.readString();
        uni_domain = in.readString();
        registration_millis = in.readLong();
        messaging_token = in.readString();
        is_organization = in.readByte() != 0;
        is_club = in.readByte() != 0;
        is_banned = in.readByte() != 0;
        registration_platform = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(name);
        dest.writeString(uni_domain);
        dest.writeLong(registration_millis);
        dest.writeString(messaging_token);
        dest.writeByte((byte) (is_organization ? 1 : 0));
        dest.writeByte((byte) (is_club ? 1 : 0));
        dest.writeByte((byte) (is_banned ? 1 : 0));
        dest.writeString(registration_platform);
    }
}
