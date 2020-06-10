package com.ivy2testing.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: Class to store a Firebase student user document
 * Features: firebase compatible, Parcelable (can pass as intent Extra)
 */
public class Student implements Parcelable {

    // Fields
    private String id;
    private String email;
    private String name;
    private String degree;
    private String uni_domain;
    private long registration_millis = 0;
    private long birth_millis = 0;
    private String messaging_token;
    private String profile_picture;
    private String preview_picture;
    private final boolean is_organization = false;
    private final boolean is_club = false;
    private boolean is_banned = false;
    private String registration_platform;
    private List<String> post_ids = new ArrayList<>();



/* Constructors
***************************************************************************************************/

    // Requirement for FireStore
    public Student(){}

    // Use for registering new student
    public Student(String id, String degree, String email){
        this.id = id;
        this.degree = degree;
        this.email = email;

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

    public String getDegree() {
        return degree;
    }


    public String getEmail() {
        return email;
    }

    public Long getBirth_millis(){
        // Set a default value for birthday
        if (birth_millis == 0){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2000);
            cal.set(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            birth_millis = cal.getTimeInMillis();
        }
        return birth_millis;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    public String getMessaging_token() {
        return messaging_token;
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

    public List<String> getPost_ids() {
        if (post_ids == null) return new ArrayList<>();
        else return new ArrayList<>(post_ids);          // Return copy
    }

    public boolean getIs_club() {
        return is_club;
    }

    public String getPreview_picture() {
        return preview_picture;
    }

    /* Setters
***************************************************************************************************/

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public void setEmail(String email) {
        if (email.contains("@") && email.contains("."))
            this.email = email;
    }

    public void setUni_domain(String domain) {
        this.uni_domain = domain;
    }

    public void setBirth_millis(long bd){
        this.birth_millis = bd;
    }

    public void setProfile_picture(String profile_picture) {
        if (profile_picture == null) this.profile_picture = null;
        else if (profile_picture.isEmpty()) this.profile_picture = null;
        else this.profile_picture = profile_picture;
    }

    public void addPostToList(String postId){
        if (postId != null && !postId.isEmpty()) post_ids.add(postId);
    }

    public void deletePostFromList(String postId){
        post_ids.remove(postId);
    }

    public void setPreview_picture(String preview_picture) {
        this.preview_picture = preview_picture;
    }

    /* Parcelable Override Methods
***************************************************************************************************/

    // Must have same order as writeToParcel since it's reading in bytes
    public Student(Parcel in) {
        id = in.readString();
        email = in.readString();
        name = in.readString();
        degree = in.readString();
        uni_domain = in.readString();
        registration_millis = in.readLong();
        birth_millis = in.readLong();
        messaging_token = in.readString();
        profile_picture = in.readString();
        preview_picture = in.readString();
        is_banned = in.readByte() != 0;
        registration_platform = in.readString();
        post_ids = in.createStringArrayList();
    }

    public static final Creator<Student> CREATOR = new Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
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
        dest.writeString(degree);
        dest.writeString(uni_domain);
        dest.writeLong(registration_millis);
        dest.writeLong(birth_millis);
        dest.writeString(messaging_token);
        dest.writeString(profile_picture);
        dest.writeString(preview_picture);
        dest.writeByte((byte) (is_banned ? 1 : 0));
        dest.writeString(registration_platform);
        dest.writeStringList(post_ids);
    }
}
