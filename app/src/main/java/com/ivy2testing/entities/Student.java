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
public class Student extends User{

    // Child Fields
    private String degree;
    private long birth_millis = 0;
    

/* Constructors
***************************************************************************************************/

    // Requirement for FireStore
    public Student(){
        super(false);
    }

    // Use for registering new student
    public Student(String id, String degree, String email){
        super(id,email,false,false);
        this.degree = degree;
    }


/* Getters
***************************************************************************************************/

    public String getDegree() {
        return degree;
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


/* Setters
***************************************************************************************************/

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public void setBirth_millis(long bd){
        this.birth_millis = bd;
    }


/* Parcelable Methods
***************************************************************************************************/

    // Must have same order as writeToParcel since it's reading in bytes
    public Student(Parcel in) {
        super(in);
        degree = in.readString();
        birth_millis = in.readLong();
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
        super.writeToParcel(dest, flags);
        dest.writeString(degree);
        dest.writeLong(birth_millis);
    }
}
