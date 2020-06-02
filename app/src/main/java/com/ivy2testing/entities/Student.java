package com.ivy2testing.entities;

import com.google.firebase.firestore.Exclude;
import java.util.Calendar;

/** @author Zahra Ghavasieh
 * Overview: Class to store a Firebase student user document
 */
public class Student {

    // Constant
    private static final int STOCK_COUNT = 35; //actual num, inclusive

    // Fields
    private String id;
    private String name;
    private String degree;
    private String email;
    private String uni_domain;
    private long registration_millis;
    private long birthday = 0;
    private String profile_picture;


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

    public Long getBirthday(){
        // Set a default value for birthday
        if (birthday == 0){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR,2000);
            cal.set(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            birthday = cal.getTimeInMillis();
        }
        return birthday;
    }

    public String getProfile_picture() {
        return profile_picture;
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

    public void setBirthday(long bd){
        this.birthday = bd;
    }

    public void setProfile_picture(String profile_picture) {
        this.profile_picture = profile_picture;
    }
}
