package com.ivy2testing.home;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;

/** @author Zahra Ghavasieh
 * Overview: View a post (not an activity) WIP
 * Feature: If viewer is the author, they will have the option to edit this post [not implemented yet!]
 */
public class ViewPostActivity extends AppCompatActivity {

    //Constants
    private static final String TAG = "ViewPostActivity";

    // Views
    private ImageView mImg;
    private EditText mName;
    private Spinner mDegree;
    private DatePicker mBirthDay;
    private Button mSaveButton;
    private ProgressBar mProgressBar;

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    // TODO Post post;


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);


        // Action bar
        setActionBar((Toolbar) findViewById(R.id.viewPost_toolBar));
        ActionBar actionBar = getActionBar();
        if (actionBar != null){
            actionBar.setTitle("post.name");  //TODO post.name
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else Log.e(TAG, "no actionbar");
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        if (isTaskRoot()) return super.getSupportParentActivityIntent(); //Return to home page if came here from a notification
        else
            //finish(); //Return to previous activity if navigated from there
            return null;
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        if (isTaskRoot()) return super.getParentActivityIntent(); //Return to home page if came here from a notification
        else
            //finish(); //Return to previous activity if navigated from there
            return null;
    }

/* Initialization Methods
***************************************************************************************************/

    // get post values TODO
    private void getIntentExtras() {
        if (getIntent() != null)
            //post = getIntent().getParcelableExtra("post");
            showToastError("Get post from Intent");

        /* if (post == null) {
            Log.e(TAG, "Student Parcel was null!");
            finish();
        } */
    }



/* Util Methods
***************************************************************************************************/

    private void showToastError(String message){
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}