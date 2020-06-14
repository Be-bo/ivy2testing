package com.ivy2testing.main;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;

import java.util.Map;

public class UserViewModel extends ViewModel {

    // MARK: Variables

    private String TAG = "ThisUserViewModelTag";

    private MutableLiveData<Organization> thisOrganization = new MutableLiveData<>();
    private MutableLiveData<Student> thisStudent = new MutableLiveData<>();
    private FirebaseFirestore db_ref = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerRegistration;
    private boolean initialAcquisition = true;
    private boolean isOrganization = false;




    // MARK: Initial Acquisition of User's Profile (with student vs organization distinction)

    public void startListening(String thisUserId, String thisUniDomain){
        if(initialAcquisition){ //had problems with the listener not being up to date sometimes during the initial launch of the MainActivity so the first time is a one time query
            db_ref.collection("universities").document(thisUniDomain).collection("users").document(thisUserId).get().addOnCompleteListener(task -> {
                if(task.isSuccessful() && task.getResult() != null && task.getResult().getData() != null){
                    Map<String, Object> user = task.getResult().getData();
                    if(user.get("is_organization") instanceof Boolean){
                        if((Boolean)user.get("is_organization")){
                            isOrganization = true;
                            thisOrganization.setValue(task.getResult().toObject(Organization.class));
                        }else{
                            thisStudent.setValue(task.getResult().toObject(Student.class));
                        }
                    }
                    initialAcquisition = false;
                    setUpListener(thisUserId, thisUniDomain); //once we've acquired the profile initially start listening
                }
            });
        }else{
            setUpListener(thisUserId, thisUniDomain); //if not the initial acquisition simply start listening
        }
    }






    // MARK: Active Listening to User's Profile in the DB

    private void setUpListener(String thisUserId, String thisUniDomain){ //listen to this user's profile updates in Firestore
        listenerRegistration = db_ref.collection("universities").document(thisUniDomain).collection("users").document(thisUserId).addSnapshotListener((documentSnapshot, e) -> {
            if(e != null){
                Log.d(TAG, "Listening for this user's profile failed: ", e);
            }

            if(documentSnapshot != null && documentSnapshot.exists()){
                if(isOrganization) thisOrganization.setValue(documentSnapshot.toObject(Organization.class));
                else thisStudent.setValue(documentSnapshot.toObject(Student.class));
            } else {
                Log.d(TAG, "This user's profile: null.");
            }
        });
    }

    public boolean isOrganization() { //since we have two options, the getter operation will have to grab this first to decide which one to use
        return isOrganization;
    }

    public MutableLiveData<Organization> getThisOrganization() {
        return thisOrganization;
    }

    public MutableLiveData<Student> getThisStudent() {
        return thisStudent;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        listenerRegistration.remove();
    }
}