package com.ivy2testing.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.userProfile.UserProfileActivity;
import com.ivy2testing.util.Constant;
import com.squareup.picasso.Picasso;

import java.util.Objects;

/** @author Zahra Ghavasieh
 * Overview: Holder to View a post or an event. Uses ViewPostFragment and ViewEventFragment for specific details
 * Feature: If viewer is the author, they will have the option to edit this post [not implemented yet!]
 * WIP: comments [expandable recyclerView], edit post layout and functionality
 */
public class ViewPostOrEventActivity extends AppCompatActivity {

    //Constants
    private static final String TAG = "ViewPostActivity";

    // Views
    private ImageView mPostVisual;
    private ImageView mAuthorImg;
    private TextView mAuthorName;
    private FloatingActionButton mFloatingEditButton;
    // TODO expandable RecyclerView for comments

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
        setContentView(R.layout.activity_viewpostorevent);

        // Initialization
        declareViews();
        getIntentExtras();
        if (post != null){
            setFields();
            setFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handling up button for when another activity called it (it will simply go back to main otherwise)
        if (item.getItemId() == android.R.id.home && !isTaskRoot()){
            goBackToParent(); // Tells parent if post was updated
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // came back from viewing author user profile
        if (requestCode == Constant.USER_PROFILE_REQUEST_CODE) {
            Log.d(TAG, "Coming back from UserProfile!");
            if (resultCode == Activity.RESULT_OK && data != null) {

                // Update author name if changed
                String new_name = data.getStringExtra("user_name");
                if (new_name != null){
                    post.setAuthor_name(new_name);
                    mAuthorName.setText(new_name);
                }

                // Update author image if changed
                Uri profile_img = data.getParcelableExtra("profile_img");
                if (profile_img != null) Picasso.get().load(profile_img).into(mAuthorImg);
            }
        } else
            Log.w(TAG, "Don't know how to handle the request code, \"" + requestCode + "\" yet!");
    }


/* Initialization Methods
***************************************************************************************************/

    private void declareViews(){
        mPostVisual = findViewById(R.id.viewPost_visual);
        mAuthorImg = findViewById(R.id.viewPost_userImage);
        mAuthorName = findViewById(R.id.viewPost_userName);
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
        loadPostVisual();           // Load post visual from storage
        loadAuthorProfileImage();   // Load author profile image from storage
        loadComments();             // Load comments from database TODO only load when expanding comments

        mAuthorName.setText(post.getAuthor_name());

        // Can edit if viewer is the author of the post
        if (viewerId != null && viewerId.equals(post.getAuthor_id()))
            mFloatingEditButton.setVisibility(View.VISIBLE);
        else mFloatingEditButton.setVisibility(View.GONE);
    }

    // Set up either ViewPost or ViewEvent Fragment in FrameLayout
    private void setFragment() {
        Fragment selected_fragment;

        if (post.getIs_event()) selected_fragment = new ViewEventFragment((Event) post, viewerId);
        else selected_fragment = new ViewPostFragment(post, viewerId);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.viewPost_contents, selected_fragment).commit();
    }


/* Transition Methods
***************************************************************************************************/

    // get post object and id of current user
    private void getIntentExtras() {
        if (getIntent() != null) {
            post = getIntent().getParcelableExtra("post");
            viewerId = getIntent().getStringExtra("viewer_id");
        }

        if (post == null) Log.e(TAG, "Student Parcel was null! Showing test view!");
        else if (viewerId != null) {
            post.addViewIdToList(viewerId);
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

        intent.putExtra("updated", updated);        // Tell main to reload post pic or nah
        if (updated) intent.putExtra("post", post); // Don't pass on post if not necessary
        setResult(RESULT_OK, intent);
        finish();
    }


/* OnClick Methods
***************************************************************************************************/

    // Clicked on username or user pic -> go to author profile
    public void viewAuthorProfile(View view){
        if (post == null){
            Log.e(TAG, "Post object is null! Cannot view author's profile");
            return;
        } // author field wasn't define

        /* TODO Uncomment for final version!!!
        if (viewerId != null && viewerId.equals(post.getAuthor_id())) {
            Log.d(TAG, "Viewer is author. Doesn't make sense to view your own profile this way.");
            return;
        } // Do nothing if viewer == author
        */

        Log.d(TAG, "Starting UserProfile Activity for user " + post.getAuthor_id());
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("this_uni_domain", post.getUni_domain());
        intent.putExtra("this_user_id", post.getAuthor_id());
        intent.putExtra("viewer_id", viewerId);
        startActivityForResult(intent, Constant.USER_PROFILE_REQUEST_CODE);
    }

   // onClick for Comments? TODO

    // onClick for edit Post (if viewer == author) TODO
    // collapse comments and unable its listener to expand (hide?)
    // Load a completely different fragment/activity??
    public void editPost(View view){
        showToastError("EditPost WIP");
    }


/* Firebase Related Methods
***************************************************************************************************/

    // loads author profile image
    private void loadAuthorProfileImage(){
        if (post == null){
            Log.e(TAG, "Post is null!");
            return;
        }

        // Get author address
        String address = "universities/" + post.getUni_domain() + "/users/" + post.getAuthor_id();
        if (address.contains("null")){
            Log.e(TAG, "User Address has null values.");
            return;
        }

        // Get Profile pic from database
        db.document(address).get().addOnCompleteListener(task -> {
           if (task.isSuccessful()){
               DocumentSnapshot doc = task.getResult(); // We only need image field so casting to an object not necessary
               if (doc != null && doc.get("profile_picture") != null){

                   // Get profile image from storage and load into image view
                   String imageAddress = Objects.requireNonNull(doc.get("profile_picture")).toString();
                   base_storage_ref.child(imageAddress).getDownloadUrl().addOnCompleteListener(task1 -> {
                       if (task1.isSuccessful()) Picasso.get().load(task1.getResult()).into(mAuthorImg);
                       else Log.e(TAG, "Could not get User Profile Image from storage.");
                   });
               }
               else Log.e(TAG, "User " + post.getAuthor_id() + " does not exist or doesn't have a profile picture.");
           }
        });
    }

    // Loads post visual (only image for now)
    private void loadPostVisual() {
        if (post == null || post.getVisual() == null) {
            Log.e(TAG, "Either Post or its visual field is null!");
            return;
        }

        // Get Visual from storage and load into image view
        base_storage_ref.child(post.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) Picasso.get().load(task.getResult()).into(mPostVisual);
            else Log.e(TAG, "Could not get post Visual from storage.");
        });
    }


    // TODO Loads Comments from Firebase
    private void loadComments(){
        // implement pagination! (up to 10 comments?)
    }


/* Util Methods
***************************************************************************************************/

    private void showToastError(String message){
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}