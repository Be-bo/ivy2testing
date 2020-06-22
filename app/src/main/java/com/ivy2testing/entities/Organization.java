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
        if (memberId != null && !memberId.isEmpty()) member_ids.add(memberId);
    }

    public void deleteMemberFromList(String memberId){
        member_ids.remove(memberId);
    }

    public void addRequestFromList(String requestId) {
        if (requestId != null && !requestId.isEmpty()) request_ids.add(requestId);
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
