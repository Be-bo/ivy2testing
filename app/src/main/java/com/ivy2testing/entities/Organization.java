package com.ivy2testing.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
* Overview: Class to store a Firebase organization user document
 * Features: firebase compatible, Parcelable (can pass as intent Extra)
*/
public class Organization implements Parcelable {

    // Fields
    private String id;
    private String email;
    private String name;
    private String uni_domain;
    private long registration_millis = 0;
    private String messaging_token;
    private String profile_picture;
    private final boolean is_organization = true;
    private boolean is_club;
    private boolean is_banned = false;
    private String registration_platform;
    private List<String> post_ids = new ArrayList<>();


/* Constructors
***************************************************************************************************/

    // Requirement for FireStore
    public Organization(){}

    // Use for registering new organization
    public Organization(String id, String email, boolean is_club){
        this.id = id;
        this.email = email;
        this.is_club = is_club;

        // Get Domain
        String[] splitEmail = email.split("@");
        if (splitEmail.length > 1)
            this.uni_domain = email.split("@")[1];

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

    public String getProfile_picture() {
        return profile_picture;
    }

    public String getMessaging_token() {
        return messaging_token;
    }

    public boolean isIs_organization() {
        return is_organization;
    }

    public boolean isIs_banned() {
        return is_banned;
    }

    public String getRegistration_platform() {
        return registration_platform;
    }

    public List<String> getPost_ids() {
        if (post_ids == null) post_ids = new ArrayList<>();
        return new ArrayList<>(post_ids);          // Return copy
    }

    public boolean isIs_club() {
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

    public void setProfile_picture(String profile_picture) {
        if (profile_picture == null) this.profile_picture = null;
        else if (profile_picture.isEmpty()) this.profile_picture = null;
        else this.profile_picture = profile_picture;
    }

    public void setMessaging_token(String messaging_token) {
        this.messaging_token = messaging_token;
    }

    public void setIs_banned(boolean is_banned) {
        this.is_banned = is_banned;
    }

    public void setRegistration_platform(String registration_platform) {
        this.registration_platform = registration_platform;
    }

    public void addPostToList(String postId){
        post_ids.add(postId);
    }

    public void deletePostfromList(String postId){
        post_ids.remove(postId);
    }


/* Parcelable Override Methods
***************************************************************************************************/

    // Must have same order as writeToParcel since it's reading in bytes
    protected Organization(Parcel in) {
        id = in.readString();
        email = in.readString();
        name = in.readString();
        uni_domain = in.readString();
        registration_millis = in.readLong();
        messaging_token = in.readString();
        profile_picture = in.readString();
        is_club = in.readByte() != 0;
        is_banned = in.readByte() != 0;
        registration_platform = in.readString();
        post_ids = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(name);
        dest.writeString(uni_domain);
        dest.writeLong(registration_millis);
        dest.writeString(messaging_token);
        dest.writeString(profile_picture);
        dest.writeByte((byte) (is_club ? 1 : 0));
        dest.writeByte((byte) (is_banned ? 1 : 0));
        dest.writeString(registration_platform);
        dest.writeStringList(post_ids);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Organization> CREATOR = new Creator<Organization>() {
        @Override
        public Organization createFromParcel(Parcel in) {
            return new Organization(in);
        }

        @Override
        public Organization[] newArray(int size) {
            return new Organization[size];
        }
    };
}
