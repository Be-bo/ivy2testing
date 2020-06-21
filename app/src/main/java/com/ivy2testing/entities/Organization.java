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
public class Organization extends User {

    // Fields
    private List<String> member_ids = new ArrayList<>();
    private List<String> request_ids = new ArrayList<>();


/* Constructors
***************************************************************************************************/

    // Requirement for FireStore
    public Organization(){
        super(true);
    }

    // Use for registering new organization
    public Organization(String id, String email, boolean is_club){
        super(id, email,true, is_club);
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


    public String getRegistration_platform() {
        return registration_platform;
    }

    public List<String> getPost_ids() {
        if (post_ids == null) post_ids = new ArrayList<>();
        return new ArrayList<>(post_ids);          // Return copy
    }

    public List<String> getMember_ids() {
        if (member_ids == null) return new ArrayList<>();
        else return new ArrayList<>(member_ids);          // Return copy
    }

    public List<String> getRequest_ids() {
        if (request_ids == null) return new ArrayList<>();
        else return new ArrayList<>(request_ids);          // Return copy
    }

/* Setters
***************************************************************************************************/

    public void addMemberToList(String memberId){
        if (memberId != null && !memberId.isEmpty()) post_ids.add(memberId);
    }

    public void deleteMemberFromList(String memberId){
        member_ids.remove(memberId);
    }

    public void addRequestFromList(String requestId) {
        if (requestId != null && !requestId.isEmpty()) post_ids.add(requestId);
    }

    public void deleteRequestFromList(String requestId) {
        request_ids.remove(requestId);
    }



/* Parcelable Override Methods
***************************************************************************************************/

    // Must have same order as writeToParcel since it's reading in bytes
    protected Organization(Parcel in) {
        super(in);
        member_ids = in.createStringArrayList();
        request_ids = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeStringList(member_ids);
        dest.writeStringList(request_ids);
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
