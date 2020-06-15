package com.ivy2testing.userProfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.entities.Student;

/** @author Zahra Ghavasieh
 * Overview: 3rd party User Profile view Activity.
 *          Takes in a user "address" in Firestore as intent extras.
 *          And uses fragments
 */
public class UserProfileActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "UserProfileActivity";

    //Firebase
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();

    // User Address
    private String this_uni_domain;
    private String this_user_id;

    // user must be a student or an Organization!
    private User user;
    private boolean is_organization;


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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handling up button for when another activity called it (it will simply go back to main otherwise)
        if (item.getItemId() == android.R.id.home && !isTaskRoot()){
            goBackToParent(); // Tells parent if user was updated
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }


/* Initialization Methods
***************************************************************************************************/

    // Set toolbar as actionBar
    private void setUpToolBar(){
        setSupportActionBar(findViewById(R.id.userProfile_toolBar));
        ActionBar action_bar = getSupportActionBar();
        if (action_bar != null){
            action_bar.setTitle(null);
            action_bar.setDisplayHomeAsUpEnabled(true);
        }
        else Log.e(TAG, "No actionBar");
    }

    // Set up either StudentProfile or OrganizationProfile Fragment in FrameLayout
    private void setFragment() {
        Fragment selected_fragment;

        if (!is_organization) {
            selected_fragment = new StudentProfileFragment(false);
        }
        else {
            selected_fragment = new OrganizationProfileFragment();
        }

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

    // Handle Up Button
    private void goBackToParent(){
        Log.d(TAG, "Returning to parent");
        Intent intent;

        // Try to go back to activity that called startActivityForResult()
        if (getCallingActivity() != null)
            intent = new Intent(this, getCallingActivity().getClass());
        else intent = new Intent(this, MainActivity.class); // Go to main as default

        setResult(RESULT_OK, intent);
        finish();
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

        // Use UserViewModel to get user info and update realtime
        UserViewModel user_view_model = new ViewModelProvider(this).get(UserViewModel.class);
        user_view_model.startListening(this_user_id, this_uni_domain);
        if (user_view_model.isOrganization()) {
            user_view_model.getThisOrganization().observe(this, (Organization updatedUser) -> {
                if (updatedUser != null) {
                    is_organization = true;
                    user = updatedUser;
                    setFragment();
                }
            });
        } else {
            user_view_model.getThisStudent().observe(this, (Student updatedUser) -> {
                if (updatedUser != null) {
                    is_organization = false;
                    user = updatedUser;
                    setFragment();
                }
            });
        }
    }
}