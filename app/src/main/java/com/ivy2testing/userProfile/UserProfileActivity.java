package com.ivy2testing.userProfile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.MainActivity;

/** @author Zahra Ghavasieh
 * Overview: 3rd party User Profile view Activity.
 *          Uses fragments for Student Profile vs Organization Profile
 */
public class UserProfileActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "UserProfileActivity";

    // User Address
    private String this_uni_domain;
    private String this_user_id;    // User whose profile we're looking at
    private String viewer_id;       // Current user

    // Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


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
            goBackToParent();
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
    private void setFragment(User user) {
        Fragment selected_fragment;

        if (user.getIs_organization()) selected_fragment = new OrganizationProfileFragment();
        else {
            selected_fragment = new StudentProfileFragment(this_user_id.equals(viewer_id), viewer_id);
            ((StudentProfileFragment)selected_fragment).setStudent((Student) user);
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
            viewer_id = getIntent().getStringExtra("viewer_id");

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
    public void getUserInfo() {
        String address = "universities/" + this_uni_domain + "/users/" + this_user_id;
        if (address.contains("null")) {
            Log.e(TAG, "User Address has null values.");
            return;
        }

        db.document(address).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                DocumentSnapshot doc = task.getResult();
                User usr;
                if ((boolean) doc.get("is_organization"))
                    usr = task.getResult().toObject(Organization.class);
                else usr = task.getResult().toObject(Student.class);

                if (usr != null){
                    usr.setId(this_user_id);
                    setFragment(usr);
                }
                else {
                    Log.e(TAG, "usr was null!");
                    goBackToParent();
                }

            }
            else {
                Log.e(TAG, "There was a problem when loading user at: " + address);
                goBackToParent();
            }
        });

    }
}