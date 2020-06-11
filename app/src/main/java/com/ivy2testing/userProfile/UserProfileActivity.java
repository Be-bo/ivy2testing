package com.ivy2testing.userProfile;

import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.util.FragCommunicator;
import com.ivy2testing.entities.Student;

import java.util.Map;
import java.util.Objects;

/** @author Zahra Ghavasieh
 * Overview: 3rd party User Profile view Activity.
 *          Takes in a user "address" in Firestore as intent extras.
 *          And uses fragments
 * Notes: Needs update if abstract class User is created, TODO Up button configurations
 */
public class UserProfileActivity extends AppCompatActivity implements FragCommunicator {

    // Constants
    private final static String TAG = "UserProfileActivity";

    //Firebase
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();

    // User Address
    private String this_uni_domain;
    private String this_user_id;

    // One will be null
    private Student student;
    private Organization organization;
    private Uri profile_img;


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialization
        getIntentExtras();  // Get user address in database via intent
        setUpToolBar();     // set up toolBar as an actionBar
        getUserInfo();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof StudentProfileFragment) {
            StudentProfileFragment profileFragment = (StudentProfileFragment) fragment;
            profileFragment.setCommunicator(this);
        }
    }


/* Initialization Methods
***************************************************************************************************/

    // Set toolbar as actionBar
    private void setUpToolBar(){
        setActionBar(findViewById(R.id.userProfile_toolBar));
        ActionBar action_bar = getActionBar();
        if (action_bar != null){
            action_bar.setTitle(null);
            action_bar.setDisplayHomeAsUpEnabled(true);
        }
        else Log.e(TAG, "No actionBar");
    }

    // Set up either StudentProfile or OrganizationProfile Fragment in FrameLayout
    private void setFragment() {
        Fragment selected_fragment = null;

        if (student != null) {
            selected_fragment = new StudentProfileFragment(student, profile_img, false);
        }
        else if (organization != null) {
            selected_fragment = new OrganizationProfileFragment();
        }

        if (selected_fragment != null)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.userProfile_frameLayout, selected_fragment).commit();
    }


/* Transition Methods
***************************************************************************************************/

    // Get student address in firebase
    private void getIntentExtras(){
        if (getIntent() != null){

            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            this_user_id = getIntent().getStringExtra("this_user_id");

            if (this_uni_domain == null || this_user_id == null){
                Log.e(TAG,"User Address is null!");
                finish();
            }
        }
    }


/* Interface (FragCommunicator) Methods
***************************************************************************************************/

    @Override
    public Object message(Object obj) {
        if (obj instanceof Student) {
            student = (Student) obj;   // Update student user
            Log.d(TAG, "Student " + student.getName() + " got updated!");
        }
        else if (obj instanceof Organization){
            organization = (Organization) obj;   // Update organization user
            Log.d(TAG, "Organization " + organization.getName() + " got updated!");
        }

        return null;
    }

    @Override
    public void mapMessage(Map<Object, Object> map) {
        if (map.get("this_user_id") != null && map.get("this_uni_domain") != null) {
            this_user_id = Objects.requireNonNull(map.get("this_user_id")).toString();
            this_uni_domain = Objects.requireNonNull(map.get("this_uni_domain")).toString();
            getUserInfo();
        }
    }


/* Firebase Methods
***************************************************************************************************/

    // Get all student info and return student class
    public void getUserInfo(){
        String address = "universities/" + this_uni_domain + "/users/" + this_user_id;
        if (address.contains("null")){
            Log.e(TAG, "User Address has null values.");
            return;
        }

        db_reference.document(address).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if (doc == null){
                    Log.e(TAG, "Document doesn't exist");
                    return;
                }

                // Return if this field doesn't exist
                if (doc.get("is_organization") == null){
                    Log.e(TAG, "getUserInfo: 'is_organization' field doesn't exist");
                    return;
                }

                // Student or Organization?
                if ((boolean) doc.get("is_organization")){
                    organization = doc.toObject(Organization.class);
                    if (organization == null) Log.e(TAG, "Organization object obtained from database is null!");
                    else organization.setId(doc.getId());
                }
                else {
                    student = doc.toObject(Student.class);
                    if (student == null) Log.e(TAG, "Student object obtained from database is null!");
                    else student.setId(doc.getId());
                }
                getUserPic(); // Attempt to load profile image Uri from firebase Storage
            }
            else Log.e(TAG,"getUserInfo: unsuccessful!");
        });
    }

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getUserPic() {

        // Make sure student has a profile image already
        if (student != null && student.getProfile_picture() != null){
            db_storage.child(student.getProfile_picture()).getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            profile_img = task.getResult();
                        }
                        else {
                            Log.w(TAG, "getUserPic for Student Unsuccessful.");
                            student.setProfile_picture(""); // image doesn't exist
                        }
                        setFragment(); // Show profile!
                    });
        }
        else if (organization != null && organization.getProfile_picture() != null){
            db_storage.child(organization.getProfile_picture()).getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            profile_img = task.getResult();
                        }
                        else {
                            Log.w(TAG, "getUserPic for Organization Unsuccessful.");
                            organization.setProfile_picture(""); // image doesn't exist
                        }
                        setFragment(); // Show Profile!
                    });
        }
        else setFragment(); // Show Profile with no pic!
    }
}