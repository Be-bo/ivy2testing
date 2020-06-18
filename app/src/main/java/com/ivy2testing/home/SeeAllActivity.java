package com.ivy2testing.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.main.MainActivity;

/** @author Zahra Ghavasieh
 * Overview: an activity with a single recyclerView and tab bar,
 *           used to show a list of some sort (specify type of list in intent!)
 */
public class SeeAllActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "SeeAllActivity";

    // Views
    private RecyclerView recycler_view;
    //private

    // Firebase
    private FirebaseFirestore firebase_db = FirebaseFirestore.getInstance();
    private StorageReference firebase_storage = FirebaseStorage.getInstance().getReference();


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialization
        setUpToolBar();     // set up toolBar as an actionBar
        declareViews();
        //getIntentExtras();  // Get user address in database via intent
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
        setSupportActionBar(findViewById(R.id.seeAll_toolBar));
        ActionBar action_bar = getSupportActionBar();
        if (action_bar != null){
            action_bar.setTitle(null);
            action_bar.setDisplayHomeAsUpEnabled(true);
        }
        else Log.e(TAG, "No actionBar");
    }

    private void declareViews(){

    }


/* Transition Methods
***************************************************************************************************/

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

}
