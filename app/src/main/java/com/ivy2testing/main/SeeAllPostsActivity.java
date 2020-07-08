package com.ivy2testing.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.ivy2testing.R;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.FeedAdapter;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.Constant;


/** @author Zahra Ghavasieh
 * Overview: an activity with a single recyclerView and tab bar,
 *           used to show a list of posts (FireBase "query" passed by intent)
 */
public class SeeAllPostsActivity extends AppCompatActivity implements FeedAdapter.FeedClickListener {

    // Constants
    private final static String TAG = "SeeAllPostsActivityTag";
    private RecyclerView recycler_view;
    private String appbar_title;                // Optional appbar title
    private User this_user;                     // Currently Logged in user
    private String uni_domain;                  // Uni domain used for query
    private String author_id;
    private FeedAdapter adapter;
    private TextView reached_bottom_text;



/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeall);
        recycler_view = findViewById(R.id.seeall_recycler);
        reached_bottom_text = findViewById(R.id.seeall_reached_bottom_text);

        // Initialization
        if (getIntentExtras()) {        // Get recycler a "query" via intent (returns its success)
            setTitle(appbar_title);     // set up toolBar as an actionBar
            setRecycler();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home && !isTaskRoot()){ // Handling up button for when another activity called it (it will simply go back to main otherwise)
            goBackToParent();
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }


/* Initialization Methods
***************************************************************************************************/

    // Get a list of user ids to display
    @SuppressWarnings("unchecked")
    private boolean getIntentExtras(){
        if (getIntent() != null){
            appbar_title = getIntent().getStringExtra("title");
            this_user = getIntent().getParcelableExtra("this_user");        // Currently logged in user
            uni_domain = getIntent().getStringExtra("uni_domain");          // Uni_domain of posts to display
            author_id = getIntent().getStringExtra("author_id");

            if (author_id == null || uni_domain == null || this_user == null) {
                finish();
            }
            else return true;
        }
        return false;
    }

    private void setRecycler(){
        adapter = new FeedAdapter(this, Constant.FEED_ADAPTER_SEEALL, uni_domain, author_id, this, null, reached_bottom_text);
        recycler_view.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recycler_view.setAdapter(adapter);
    }



/* OnClick Methods
***************************************************************************************************/

    // OnClick: Handles clicks on a post item
    public void onFeedClick(int position, int clicked_id) {
        Post clickedPost = adapter.getPost_array_list().get(position);     //<- this is the clicked event/post

        switch(clicked_id){
            case R.id.item_feed_full_text_button:
                viewPost(clickedPost);
                break;

            case R.id.item_feed_posted_by_text:
                viewUserProfile(clickedPost.getAuthor_id(), clickedPost.getUni_domain(), clickedPost.getAuthor_is_organization());
                break;

            case R.id.item_feed_pinned_text:
                //TODO
                break;
        }
    }

    // Transition to a post/event
    private void viewPost(Post post) {
        Log.d(TAG, "Launching ViewPostOrEventActivity...");
        Intent intent = new Intent(this, ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    // Transition to a user profile
    private void viewUserProfile(String user_id, String user_uni, boolean is_organization){
        if (user_id == null || user_uni == null){
            Log.e(TAG, "User not properly defined! Cannot view author's profile");
            return;
        } // author field wasn't defined


        if (this_user != null && user_id.equals(this_user.getId())) {
            Log.d(TAG, "Viewer is author. Might want to change behaviour.");
        } // Do nothing if viewer == author

        Intent intent;
        if (is_organization){
            Log.d(TAG, "Starting OrganizationProfile Activity for organization " + user_id);
            intent = new Intent(this, OrganizationProfileActivity.class);
            intent.putExtra("org_to_display_id", user_id);
            intent.putExtra("org_to_display_uni", user_uni);
        }
        else {
            Log.d("HomeFragment", "Starting StudentProfile Activity for student " + user_id);
            intent = new Intent(this, StudentProfileActivity.class);
            intent.putExtra("student_to_display_id", user_id);
            intent.putExtra("student_to_display_uni", user_uni);
        }
        intent.putExtra("this_user", this_user);
        startActivity(intent);
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