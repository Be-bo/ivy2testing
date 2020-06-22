package com.ivy2testing.entities;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.Exclude;


/** @author Zahra Ghavasieh
 * Overview: Class to store a Firebase comment document
 * Features: firebase compatible, Parcelable (can pass as intent Extra)
 */
public class Comment implements Parcelable {

    // Fields
    private String id;
    private String uni_domain;
    private String author_id;
    private String author_name;
    private String text = "";


/* Constructors
***************************************************************************************************/

    // Requirement for FireStore
    public Comment(){}

    // Use for creating new comment in java code
    public Comment(String id, String uni_domain, String author_id, String author_name, String text){
        this.id = id;
        this.uni_domain = uni_domain;
        this.author_id = author_id;
        this.author_name = author_name;
        this.text = text;
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

    public String getAuthor_id() {
        return author_id;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public String getText() {
        return text;
    }


/* Setters
***************************************************************************************************/

    public void setText(String text) {
        this.text = text;
    }


/* Parcelable Override Methods
***************************************************************************************************/

    protected Comment(Parcel in) {
        id = in.readString();
        uni_domain = in.readString();
        author_id = in.readString();
        author_name = in.readString();
        text = in.readString();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(uni_domain);
        dest.writeString(author_id);
        dest.writeString(author_name);
        dest.writeString(text);
    }

}
