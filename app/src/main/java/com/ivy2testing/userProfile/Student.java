package com.ivy2testing.userProfile;

import com.google.firebase.firestore.Exclude;

/** @author Zahra Ghavasieh
 * Overview: Class to store a Firebase student user document
 */
public class Student {

    // Fields
    private String id;
    private String name;
    private String degree;
    private String email;
    private String uni_domain;
    private long registration_millis;


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

/* Setters
***************************************************************************************************/
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

/* Other Methods
***************************************************************************************************/
}
