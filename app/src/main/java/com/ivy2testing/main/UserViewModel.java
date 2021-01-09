package com.ivy2testing.main;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;

import java.util.Map;

public class UserViewModel extends ViewModel {

    // MARK: Variables

    private String TAG = "ThisUserViewModelTag";
    private MutableLiveData<User> this_user = new MutableLiveData<>();
    private FirebaseFirestore db_ref = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerRegistration;







    // MARK: Active Listening to User's Profile in the DB

    public void startListening(String thisUserId){ //listen to this user's profile updates in Firestore
        listenerRegistration = db_ref.collection("users").document(thisUserId).addSnapshotListener((documentSnapshot, e) -> {
            if(e != null){
                Log.d(TAG, "Listening for this user's profile failed: ", e);
            }
            if(documentSnapshot != null && documentSnapshot.exists() && documentSnapshot.getData() != null){
                Map<String, Object> mashHap = documentSnapshot.getData();
                User usr;
                if(mashHap.get("is_organization") instanceof Boolean && (Boolean) mashHap.get("is_organization"))
                    usr = documentSnapshot.toObject(Organization.class);
                else usr = documentSnapshot.toObject(Student.class);

                if(usr != null) usr.setId(thisUserId);
                this_user.setValue(usr);
            } else {
                Log.d(TAG, "This user's profile: null.");
            }
        });
    }







    // MARK: Other

    public MutableLiveData<User> getThis_user() {
        return this_user;
    }

    public void setThis_user(MutableLiveData<User> this_user) {
        this.this_user = this_user;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(listenerRegistration != null) listenerRegistration.remove();
    }
}