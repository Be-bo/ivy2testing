package com.ivy2testing.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Post;
import com.ivy2testing.userProfile.UserProfileActivity;
import com.ivy2testing.util.Constant;

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

    @Override
    public boolean onNavigateUp() {
        Log.d(TAG, "NAV UP WAS CALLED!!!!");
        return super.onNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // came back from viewing author user profile
        if (requestCode == Constant.USER_PROFILE_REQUEST_CODE) {
            Log.d(TAG, "Coming back from ViewPost!");
            if (resultCode == Activity.RESULT_OK && data != null) {
                String new_name = data.getStringExtra("author_name"); // Update author name if changed
                if (new_name != null){
                    post.setAuthor_name(new_name);
                    mAuthorName.setText(new_name);
                }
            }
        } else
            Log.w(TAG, "Don't know how to handle the request code, \"" + requestCode + "\" yet!");
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
        mPinnedEvent = findViewById(R.id.viewPost_pinned);
        mFloatingEditButton = findViewById(R.id.viewPost_floatingEditButton);

        // Action bar
        setSupportActionBar(findViewById(R.id.viewPost_toolBar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setTitle(null);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else Log.e(TAG, "no actionbar");
    }

    // Populate fields with Post info
    private void setFields() {
        loadImages();           // Load author profile image and post visual
        loadComments();         // Load comments from database TODO only load when expanding comments

        mAuthorName.setText(post.getAuthor_name());
        mPostDescription.setText(post.getText());       // Post description

        // Pinned event
        if (post.getPinned_id() != null) mPinnedEvent.setText(post.getPinned_id());      // Pinned Event (name or id??)
        else findViewById(R.id.viewPost_pin).setVisibility(View.GONE);


        // Can edit if viewer is the author of the post
        if (viewerId != null && viewerId.equals(post.getAuthor_id()))
            mFloatingEditButton.setVisibility(View.VISIBLE);
        else mFloatingEditButton.setVisibility(View.GONE);
    }


/* OnClick Methods
***************************************************************************************************/

// Clicked on username or user pic -> go to author profile TODO remove commented out after testing
    public void viewAuthorProfile(View view){
        //if (viewerId != null && viewerId.equals(post.getAuthor_id())) return; // Do nothing if viewer is author

        Log.d(TAG, "Starting UserProfile Activity for user " + post.getAuthor_id());
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("this_uni_domain", post.getUni_domain());
        intent.putExtra("this_user_id", post.getAuthor_id());
        startActivityForResult(intent, Constant.USER_PROFILE_REQUEST_CODE);
    }

   // onClick for Comments? TODO

    // onClick for edit Post (if viewer == author) TODO
    // collapse comments and unable its listener to expand (hide?)
    public void editPost(View view){
        showToastError("EditPost WIP");
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