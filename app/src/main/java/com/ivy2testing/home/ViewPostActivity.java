package com.ivy2testing.home;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Post;

import de.hdodenhof.circleimageview.CircleImageView;

/** @author Zahra Ghavasieh
 * Overview: View a post (not an activity) WIP
 * Feature: If viewer is the author, they will have the option to edit this post [not implemented yet!]
 */
public class ViewPostActivity extends AppCompatActivity {

    //Constants
    private static final String TAG = "ViewPostActivity";

    // Views
    private ImageView mPostVisual;
    private ImageView mAuthorImg;
    private TextView mAuthorName;
    private TextView mPostDescription;
    private ConstraintLayout mPin;
    private TextView mPinnedEvent;
    private FloatingActionButton mFloatingEditButton;
    // TODO contractable RecyclerView for comments

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Post post;          // Nullable!!!
    private String viewerId;    // Nullable also!!!
    private boolean updated = false;


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        // Initialization
        declareViews();
        getIntentExtras();
        if (post != null) setFields();

        // Set Listeners

    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        if (isTaskRoot()) return super.getSupportParentActivityIntent(); //Return to home page if came here from a notification
        else return backToParent(); //Return to previous activity if navigated from there
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        if (isTaskRoot()) return super.getParentActivityIntent(); //Return to home page if came here from a notification
        else return backToParent(); //Return to previous activity if navigated from there
    }


/* Initialization Methods
***************************************************************************************************/

    // get post object and id of current user
    private void getIntentExtras() {
        if (getIntent() != null) {
            post = getIntent().getParcelableExtra("post");
            viewerId = getIntent().getStringExtra("this_user_id");
        }

        if (post == null) Log.e(TAG, "Student Parcel was null! Showing test view!");
        else if (viewerId != null) {
            post.addViewIdToList(viewerId);
        }
    }

    private void declareViews(){
        mPostVisual = findViewById(R.id.viewPost_visual);
        mAuthorImg = findViewById(R.id.viewPost_userImage);
        mAuthorName = findViewById(R.id.viewPost_userName);
        mPostDescription = findViewById(R.id.viewPost_description);
        mPin = findViewById(R.id.viewPost_pin);
        mPinnedEvent = findViewById(R.id.viewPost_pinned);
        mFloatingEditButton = findViewById(R.id.viewPost_floatingEditButton);

        // Action bar
        setActionBar((Toolbar) findViewById(R.id.viewPost_toolBar));
        ActionBar actionBar = getActionBar();
        if (actionBar != null){
            actionBar.setTitle(null);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else Log.e(TAG, "no actionbar");
    }

    // Populate fields with Post info
    private void setFields() {
        loadImages();           // Load author profile image and post visual
        mAuthorName.setText(post.getAuthor_name());
        mPostDescription.setText(post.getText());       // Post description
        mPinnedEvent.setText(post.getPinned_id());      // Pinned Event (name or id??)
        loadComments();         // Load comments from database TODO only load when expanding comments

        // Can edit if viewer is the author of the post
        if (viewerId != null && viewerId.equals(post.getAuthor_id()))
            mFloatingEditButton.setVisibility(View.VISIBLE);
        else mFloatingEditButton.setVisibility(View.GONE);
    }


/* OnClick Methods
***************************************************************************************************/

// Clicked on username or user pic -> go to author profile TODO
    public void viewAuthorProfile(View view){
        if (viewerId != null && viewerId.equals(post.getAuthor_id())) return; // Do nothing if viewer is author

        showToastError("ViewAuthorProfile WIP");
    }

   // onClick for Comments? TODO

    // onClick for edit Post (if viewer == author) TODO
    // collapse comments and unable its listener to expand (hide?)
    public void editPost(View view){
        showToastError("EditPost WIP");
    }


/* Transition Methods
***************************************************************************************************/


    private Intent backToParent(){
        Intent intent = new Intent();
        intent.putExtra("updated", updated);
        if (updated) intent.putExtra("post", post);
        setResult(RESULT_OK, intent);
        return intent;
    }


/* Firebase Related Methods
***************************************************************************************************/

    // TODO loads author profile image as well as post visual
    private void loadImages(){

        //load author profile image

        // load post visual
    }

    // TODO Loads Comments from Firebase
    private void loadComments(){
        // implement pagination! (up to 6 comments?)
    }


/* Util Methods
***************************************************************************************************/

    private void showToastError(String message){
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}