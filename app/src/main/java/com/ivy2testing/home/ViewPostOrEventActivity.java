package com.ivy2testing.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Comment;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.userProfile.UserProfileActivity;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.adapters.CommentAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


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
    private ImageButton mExpandComments;
    private RecyclerView mCommentsRecycler;

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Post post;          // Nullable!!!
    private String viewerId;    // Nullable also!!!

    // Comments Recycler Variables
    private List<Comment> comments = new ArrayList<>();
    private CommentAdapter adapter = new CommentAdapter(comments);
    private LinearLayoutManager layout_man;
    private final static int MIN_COMMENTS_LOADED = 15;
    private DocumentSnapshot last_doc;                  // Snapshot of last comment loaded
    private boolean comment_list_updated;



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
        mExpandComments = findViewById(R.id.viewPost_commentButton);
        mCommentsRecycler = findViewById(R.id.viewPost_commentRV);

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

    // Set up comments recycler with manager and adapter
    private void setCommentRecycler(){
        adapter.setOnSelectionListener(this::onCommentAuthorClicked);
        layout_man = new LinearLayoutManager(this);
        mCommentsRecycler.setLayoutManager(layout_man);
        mCommentsRecycler.setAdapter(adapter);
        loadComments();

        // Scroll Listener used for pagination (TODO not fully tested yet)
        mCommentsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (comment_list_updated) {
                    if (layout_man.findLastCompletelyVisibleItemPosition() > (comments.size() - 3 )){
                        comment_list_updated = false;
                        loadComments();
                    }
                }
            }
        });
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

        viewUserProfile(post.getAuthor_id(), post.getUni_domain());
    }

   // onClick for Comments
    private void onCommentAuthorClicked(int position) {
        viewUserProfile(comments.get(position).getAuthor_id(), comments.get(position).getUni_domain());
    }

    // View a user's profile
    private void viewUserProfile(String userId, String uniDomain){
        Log.d(TAG, "Starting UserProfile Activity for user " + userId);
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("this_uni_domain", uniDomain);
        intent.putExtra("this_user_id", userId);
        intent.putExtra("viewer_id", viewerId);
        startActivityForResult(intent, Constant.USER_PROFILE_REQUEST_CODE);
    }

    // onClick for edit Post (if viewer == author) TODO
    // collapse comments and unable its listener to expand (hide?)
    // Load a completely different fragment/activity??
    public void editPost(View view){
        String message = "EditPost WIP";
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // TODO new comment?

    // Load comments only when clicked on
    public void expandComments(View view){
        if (findViewById(R.id.viewPost_commentLayout).getVisibility() == View.GONE){
            TransitionManager.beginDelayedTransition(findViewById(R.id.viewPost_linearRootLayout), new AutoTransition());
            findViewById(R.id.viewPost_commentLayout).setVisibility(View.VISIBLE);
            mExpandComments.setImageResource(R.drawable.ic_arrow_up);

            // Set up recycler with recycler manager and adapter if not done yet
            if (layout_man == null) setCommentRecycler();
        }
        else {
            TransitionManager.beginDelayedTransition(findViewById(R.id.viewPost_linearRootLayout), new AutoTransition());
            findViewById(R.id.viewPost_commentLayout).setVisibility(View.GONE);
            mExpandComments.setImageResource(R.drawable.ic_arrow_down);
        }
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
        String address = ImageUtils.getPreviewPath(post.getAuthor_id());
        if (address.contains("null")){
            Log.e(TAG, "Address contained null! UserId: " + post.getAuthor_id());
            return;
        }

        base_storage_ref.child(address).getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                Picasso.get().load(task.getResult()).into(mAuthorImg);
            else Log.e(TAG, "Could not get User Profile Image from storage.");
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


    // Loads Comments from Firebase
    private void loadComments(){
        String address = "universities/" + post.getUni_domain() + "/posts/" + post.getId() +"/comments";
        if (address.contains("null")){
            Log.e(TAG, "Address has null values.");
            return;
        }

        // Build Query
        Query query = db.collection(address).limit(MIN_COMMENTS_LOADED);
        if (last_doc != null) query = query.startAfter(last_doc);

        // Pull Comments
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                QuerySnapshot query_doc = task.getResult();
                int i = 0;
                for (QueryDocumentSnapshot doc : query_doc){

                    // Get post object
                    Comment comment = doc.toObject(Comment.class);
                    post.setId(doc.getId());

                    // Add to list and tell recycler adapter
                    comments.add(comment);
                    adapter.notifyItemInserted(comments.size()-1);

                    i++;
                }
                if (!query_doc.isEmpty())
                    last_doc = query_doc.getDocuments().get(query_doc.size()-1);    // Save last doc retrieved
                Log.d(TAG, i + " comments were uploaded from database!");
            }
            else Log.e(TAG, "loadComments: unsuccessful or do not exist.", task.getException());

            // Put a no Comments available sign
            if (comments.size() > 0){
                mCommentsRecycler.setVisibility(View.VISIBLE);
                findViewById(R.id.viewPost_commentErrorMsg).setVisibility(View.GONE);
            }
            else {
                mCommentsRecycler.setVisibility(View.GONE);
                findViewById(R.id.viewPost_commentErrorMsg).setVisibility(View.VISIBLE);
            }
        });
    }
}