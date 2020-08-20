package com.ivy2testing.home;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.bubbletabs.CampusFragment;
import com.ivy2testing.bubbletabs.EventsFragment;
import com.ivy2testing.entities.Comment;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.notifications.NotificationHandler;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.OrganizationProfileFragment;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.userProfile.StudentProfileFragment;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.Utils;
import com.ivy2testing.util.adapters.CommentAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Zahra Ghavasieh
 * Overview: Holder to View a post or an event. Uses ViewPostFragment and ViewEventFragment for specific details
 * Feature: If viewer is the author, they will have the option to edit this post [not implemented yet!]
 * WIP: comments [expandable recyclerView], edit post layout and functionality
 */
public class ViewPostOrEventActivity extends AppCompatActivity {

    //Constants
    private static final String TAG = "ViewPostActivityTag";

    // Views
    private ImageView mPostVisual;
    private ImageView mAuthorImg;
    private TextView mAuthorName;
    private ImageButton mExpandComments;
    private RecyclerView mCommentsRecycler;
    private EditText mWriteComment;
    private ImageButton mPostComment;
    private TextView commentsTitle;
    private TextView cantSeeComments;
    private Menu options_menu;
    private ProgressBar progress_bar;
    private ScrollView scroll_view;

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Comments Recycler Variables
    private List<Comment> comments = new ArrayList<>();
    private CommentAdapter adapter = new CommentAdapter(comments);
    private LinearLayoutManager layout_man;
    private final static int MIN_COMMENTS_LOADED = 15;
    private DocumentSnapshot last_doc;                  // Snapshot of last comment loaded
    private boolean comment_list_updated;

    // Other Variables (Nullable)
    private Post post;
    private User this_user;     // Currently logged in user
















/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpostorevent);
        declareViews();
        pullPost();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_event_post_bar_menu, menu);
        options_menu = menu;
        options_menu.setGroupVisible(0, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handling up button for when another activity called it (it will simply go back to main otherwise)
        if (item.getItemId() == android.R.id.home && !isTaskRoot()) {
            goBackToParent(); // Tells parent if post was updated
            return true;
        } else if(item.getItemId() == R.id.view_event_post_bar_edit_post){
            editPost();
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case Constant.USER_PROFILE_REQUEST_CODE:
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
                break;
            case Constant.EDIT_POST_REQUEST_CODE:
                if(resultCode == RESULT_OK){ //not an ideal solution but good enough for now - if an event/post gets edited we refresh all fragments
                    pullPost();
                }
        }
    }















    /* Initialization Methods
     ***************************************************************************************************/

    private void declareViews() {
        mPostVisual = findViewById(R.id.viewPost_visual);
        mAuthorImg = findViewById(R.id.viewPost_userImage);
        mAuthorName = findViewById(R.id.viewPost_userName);
       // mExpandComments = findViewById(R.id.viewPost_commentButton);
        mCommentsRecycler = findViewById(R.id.viewPost_commentRV);
        mWriteComment = findViewById(R.id.writeComment_commentText);
        mPostComment = findViewById(R.id.writeComment_commentButton);
        commentsTitle = findViewById(R.id.viewPost_commentsTitle);
        cantSeeComments = findViewById(R.id.viewPost_cantSeeComments);
        progress_bar = findViewById(R.id.viewPost_progress_bar);
        scroll_view = findViewById(R.id.viewPost_scrollView);
        setTitle(null);
    }

    // get post object and id of current user
    private void pullPost() {
        if (getIntent() != null) {
            String postId = getIntent().getStringExtra("post_id"); //if it's a pinned event
            String postUni = getIntent().getStringExtra("post_uni");
            this_user = getIntent().getParcelableExtra("this_user");
            if (postId != null && postUni != null) {
                checkNotifications(postId);
                db.collection("universities").document(postUni).collection("posts").document(postId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().get("is_event") instanceof Boolean && (Boolean) task.getResult().get("is_event"))
                            post = task.getResult().toObject(Event.class);
                        else post = task.getResult().toObject(Post.class);
                        if (post != null) {
                            setFields();
                            setFragment();
                            if (this_user != null) post.addViewIdToList(this_user.getId());
                        }
                    } else {
                        Toast.makeText(this, "Failed to get post/event data. :-(", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to get post/event data. :-(", Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (this_user != null) {
            post.addViewIdToList(this_user.getId());
        }
    }

    // Populate fields with Post info
    private void setFields() {
        loadPostVisual();           // Load post visual from storage
        loadAuthorProfileImage();   // Load author profile image from storage

        mAuthorName.setText(post.getAuthor_name());

        // Can comment if logged in
        if (this_user != null) {
            if (this_user.getUni_domain().equals(post.getUni_domain())) { //from this uni -> can comment
               // mExpandComments.setVisibility(View.VISIBLE);
                commentsTitle.setVisibility(View.VISIBLE);
                cantSeeComments.setVisibility(View.GONE);
                findViewById(R.id.viewPost_commentsLayout).setVisibility(View.VISIBLE);
                setupWriteComment();
                setCommentRecycler();
            } else {
            //    mExpandComments.setVisibility(View.GONE);
                commentsTitle.setVisibility(View.GONE);
                cantSeeComments.setVisibility(View.GONE);
            }

            if(this_user.getId().equals(post.getAuthor_id())){ //if the user is the author of this post -> show the edit button and allow them to edit
                options_menu.setGroupVisible(0, true);
            }
        }else{
           // mExpandComments.setVisibility(View.GONE);
            commentsTitle.setVisibility(View.GONE);
            cantSeeComments.setVisibility(View.VISIBLE);
        }
    }

    // Set up either ViewPost or ViewEvent Fragment in FrameLayout
    private void setFragment() {
        Fragment selected_fragment;

        if (post.getIs_event()) {
            setTitle(((Event) post).getName());
            selected_fragment = new ViewEventFragment((Event) post, this_user);
        } else {
            setTitle("Post");
            selected_fragment = new ViewPostFragment(post, this_user);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.viewPost_contents, selected_fragment).commit();
    }

    // Set up comments recycler with manager and adapter
    private void setCommentRecycler() {
        adapter.setOnSelectionListener(this::onCommentAuthorClicked);
        layout_man = new LinearLayoutManager(this);
        mCommentsRecycler.setLayoutManager(layout_man);
        mCommentsRecycler.setAdapter(adapter);
        mCommentsRecycler.setNestedScrollingEnabled(true);
        loadComments();

        // Scroll Listener used for pagination
        mCommentsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (comment_list_updated) {
                    if (layout_man.findLastCompletelyVisibleItemPosition() > (comments.size() - 3)) {
                        comment_list_updated = false;
                        loadComments();
                    }
                }
            }
        });
    }

    // Set up write comment functionality for a logged in user
    private void setupWriteComment() {
        mWriteComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPostComment.setClickable(!mWriteComment.getText().toString().trim().isEmpty());
                if (mPostComment.isClickable())
                    mPostComment.setColorFilter(getColor(R.color.interaction));
                else mPostComment.setColorFilter(getColor(R.color.grey));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }















    /* Transition Methods
     ***************************************************************************************************/


    // Handle Up Button
    private void goBackToParent() {
        Log.d(TAG, "Returning to parent");
        Intent intent;

        // Try to go back to activity that called startActivityForResult()
        if (getCallingActivity() != null)
            intent = new Intent(this, getCallingActivity().getClass());
        else intent = new Intent(this, MainActivity.class); // Go to main as default

        setResult(RESULT_OK, intent);
        finish();
    }

    // Don't let user make another comment while comment is going to database
    private void startCommentLoading() {
        closeKeyboard();
        mPostComment.setClickable(false);
        mPostComment.setVisibility(View.INVISIBLE);
        findViewById(R.id.writeComment_loading).setVisibility(View.VISIBLE);
    }

    private void stopCommentLoading() {
        mPostComment.setVisibility(View.VISIBLE);
        findViewById(R.id.writeComment_loading).setVisibility(View.GONE);
    }

    // Successfully made comment
    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Show different views if there are no comments
    private void viewComments() {
        // Put a no Comments available sign
        if (comments.size() > 0) {
            mCommentsRecycler.setVisibility(View.VISIBLE);
            findViewById(R.id.viewPost_commentErrorMsg).setVisibility(View.GONE);
            // Scroll down a bit
            ScrollView scrollView = findViewById(R.id.viewPost_scrollView);
            scrollView.smoothScrollBy(0, getResources().getDisplayMetrics().heightPixels); //TODO scroll down a bit
        } else {
            mCommentsRecycler.setVisibility(View.GONE);
            findViewById(R.id.viewPost_commentErrorMsg).setVisibility(View.VISIBLE);
            // Scroll down a bit
            ScrollView scrollView = findViewById(R.id.viewPost_scrollView);
            scrollView.smoothScrollBy(0, getResources().getDisplayMetrics().heightPixels); //TODO scroll down a bit
        }
    }















    /* OnClick Methods
     ***************************************************************************************************/

    // Clicked on username or user pic -> go to author profile
    public void viewAuthorProfile(View view) {
        if (post == null) {
            Log.e(TAG, "Post object is null! Cannot view author's profile");
            return;
        } // author field wasn't define

        if (this_user != null && this_user.getId().equals(post.getAuthor_id())) {
            Log.d(TAG, "Viewer is author. Doesn't make sense to view your own profile this way.");
        } // Do nothing if viewer == author


        viewUserProfile(post.getAuthor_id(), post.getUni_domain(), post.getAuthor_is_organization());
    }

    // onClick for Comments
    private void onCommentAuthorClicked(int position) {
        Comment clicked_comment = comments.get(position);
        viewUserProfile(clicked_comment.getAuthor_id(),
                clicked_comment.getUni_domain(),
                clicked_comment.getAuthor_is_organization());
    }

    // View a user's profile
    private void viewUserProfile(String user_id, String uni_domain, boolean is_organization) {
        Intent intent;
        if (is_organization) {
            Log.d(TAG, "Starting OrganizationProfile Activity for organization " + user_id);
            intent = new Intent(this, OrganizationProfileActivity.class);
            intent.putExtra("org_to_display_id", user_id);
            intent.putExtra("org_to_display_uni", uni_domain);
        } else {
            Log.d(TAG, "Starting StudentProfile Activity for student " + user_id);
            intent = new Intent(this, StudentProfileActivity.class);
            intent.putExtra("student_to_display_id", user_id);
            intent.putExtra("student_to_display_uni", uni_domain);
        }
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }

    // Load comments only when clicked on
    public void expandComments(View view) {
        if (findViewById(R.id.viewPost_commentsLayout).getVisibility() == View.GONE) {
            TransitionManager.beginDelayedTransition(findViewById(R.id.viewPost_linearRootLayout), new AutoTransition());
            findViewById(R.id.viewPost_commentsLayout).setVisibility(View.VISIBLE);
           // mExpandComments.setImageResource(R.drawable.ic_arrow_up);

            // Set up recycler with recycler manager and adapter if not done yet
            if (layout_man == null) setCommentRecycler();
            else {
                // Scroll down a bit
                ScrollView scrollView = findViewById(R.id.viewPost_scrollView);
                scrollView.smoothScrollBy(0, getResources().getDisplayMetrics().heightPixels); //TODO scroll down a bit
            }
        } else {
            TransitionManager.beginDelayedTransition(findViewById(R.id.viewPost_linearRootLayout), new AutoTransition());
            findViewById(R.id.viewPost_commentsLayout).setVisibility(View.GONE);
          //  mExpandComments.setImageResource(R.drawable.ic_arrow_down);
        }
    }

    // OnClick for post a comment
    public void postComment(View view) {
        String commentText = mWriteComment.getText().toString().trim();
        if (commentText.isEmpty()) {
            Log.d(TAG, "Can't post empty comment!");
            return;
        }

        startCommentLoading();

        // Create Comment object
        String commentId = "" + System.currentTimeMillis();
        Comment newComment = new Comment(
                commentId,
                post.getUni_domain(),
                this_user.getId(),
                this_user.getName(),
                this_user.getIs_organization(),
                commentText);

        // Write to database
        String address = "universities/" + post.getUni_domain() + "/posts/" + post.getId() + "/comments/" + commentId;
        if (address.contains("null")) {
            Log.e(TAG, "Address has null values.");
            stopCommentLoading();
            return;
        }
        db.document(address).set(newComment).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                comments.add(0, newComment);
                adapter.notifyItemInserted(0);
                if (mCommentsRecycler.getVisibility() == View.GONE) viewComments();
                mWriteComment.setText(null);
            } else {
                Log.e(TAG, "Comment not saved in database.", task1.getException());
                Toast.makeText(this, "Could not post comment", Toast.LENGTH_LONG).show();
            }
            stopCommentLoading();
        });
    }

    private void editPost(){
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("editing_mode", true);
        intent.putExtra("is_event", post.getIs_event());
        if(post.getIs_event()) intent.putExtra("event", post);
        else intent.putExtra("post", post);
        startActivityForResult(intent, Constant.EDIT_POST_REQUEST_CODE);
    }











    /* Firebase Related Methods
     ***************************************************************************************************/

    // loads author profile image
    private void loadAuthorProfileImage() {
        if (post == null) {
            Log.e(TAG, "Post is null!");
            return;
        }

        // Get author address
        String address = ImageUtils.getUserImagePreviewPath(post.getAuthor_id());
        if (address.contains("null")) {
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
        if (post == null || post.getVisual() == null || post.getVisual().equals("") || post.getVisual().equals("nothing")) {
            mPostVisual.setVisibility(View.GONE);
            progress_bar.setVisibility(View.GONE);
            scroll_view.setVisibility(View.VISIBLE);
            Log.e(TAG, "Either Post or its visual field is null!");
            return;
        }else{
            mPostVisual.setVisibility(View.VISIBLE);
        }

        // Get Visual from storage and load into image view
        base_storage_ref.child(post.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) Picasso.get().load(task.getResult()).into(mPostVisual);
            else {
                mPostVisual.setVisibility(View.GONE);
                Log.e(TAG, "Could not get post Visual from storage.");
            }
            progress_bar.setVisibility(View.GONE);
            scroll_view.setVisibility(View.VISIBLE);
        });
    }


    // Loads Comments from Firebase
    private void loadComments() {
        String address = "universities/" + post.getUni_domain() + "/posts/" + post.getId() + "/comments";
        if (address.contains("null")) {
            Log.e(TAG, "Address has null values.");
            return;
        }

        // Build Query
        Query query = db.collection(address).limit(MIN_COMMENTS_LOADED);
        if (last_doc != null) query = query.startAfter(last_doc);

        // Pull Comments
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot query_doc = task.getResult();
                int i = 0;
                for (QueryDocumentSnapshot doc : query_doc) {

                    // Get post object
                    Comment comment = doc.toObject(Comment.class);
                   //  post.setId(doc.getId());// TODO heres the problem

                    // Add to list and tell recycler adapter
                    comments.add(comment);
                    adapter.notifyItemInserted(comments.size() - 1);

                    i++;
                }
                if (!query_doc.isEmpty())
                    last_doc = query_doc.getDocuments().get(query_doc.size() - 1);    // Save last doc retrieved
                Log.d(TAG, i + " comments were uploaded from database!");
            } else Log.e(TAG, "loadComments: unsuccessful or do not exist.", task.getException());

            viewComments();
        });
    }

    private void checkNotifications(String postID) {
        boolean found = false;
        int count = 0;
        ArrayList<Integer> found_list = new ArrayList<>();
        ArrayList<Integer> remove_list = new ArrayList<>();
        ArrayList<HashMap<String, Integer>> notif_list = Utils.getNotif_list();
        if (notif_list.size() != 0) {
            for (HashMap<String, Integer> map : notif_list) {
                if (map.containsKey(postID)) {
                    if (map.get(postID) != null) {
                        found_list.add(map.get(postID));
                        remove_list.add(count);
                        found = true;
                    }
                }
                count++;
            }
            if (found) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                for (int j = found_list.size() - 1; j >= 0; j--) {
                    notificationManager.cancel(found_list.get(j));
                    notif_list.remove((int) remove_list.get(j));
                }
                Utils.setNotif_list(notif_list);
            }
        }
    }
}