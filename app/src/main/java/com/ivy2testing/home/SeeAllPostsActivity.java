package com.ivy2testing.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;


/** @author Zahra Ghavasieh
 * Overview: an activity with a single recyclerView and tab bar,
 *           used to show a list of posts (FireBase "query" passed by intent)
 */
public class SeeAllPostsActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "SeeAllPostsActivity";

    // Views
    private RecyclerView recycler_view;

    // Firebase
    private FirebaseFirestore firebase_db = FirebaseFirestore.getInstance();

    // Variables passed by intent
    private String appbar_title;                // Optional appbar title
    private User this_user;                     // Currently Logged in user
    private String uni_domain;                  // Uni domain used for query
    private HashMap<String, Object> query_map;  // Used to construct a firebase query (K = field name, V = field Value)

    // Recycler variables
    private FeedAdapter adapter;
    private ArrayList<Post> posts = new ArrayList<>();
    private LinearLayoutManager layout_man;

    // Variables used for pagination
    private final static int MIN_POSTS_LOADED = 15;
    private DocumentSnapshot last_doc;              // Snapshot of last post loaded
    private boolean array_list_updated;


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeall);

        // Initialization
        if (getIntentExtras()) {  // Get recycler a "query" via intent (returns its success)
            setUpToolBar();     // set up toolBar as an actionBar
            setRecycler();
            setListeners();
        }
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
        setSupportActionBar(findViewById(R.id.seeAll_toolbar));
        ActionBar action_bar = getSupportActionBar();
        if (action_bar != null){
            action_bar.setTitle(null);
            action_bar.setDisplayHomeAsUpEnabled(true);
            if (appbar_title != null) ((TextView) findViewById(R.id.seeAll_toolbarTitle)).setText(appbar_title);
        }
        else Log.e(TAG, "No actionBar");
    }

    // Get a list of user ids to display
    @SuppressWarnings("unchecked")
    private boolean getIntentExtras(){
        if (getIntent() != null){

            appbar_title = getIntent().getStringExtra("title");             // Optional activity title
            this_user = getIntent().getParcelableExtra("this_user");        // Currently logged in user
            uni_domain = getIntent().getStringExtra("uni_domain");          // Uni_domain of posts to display
            query_map = (HashMap<String, Object>) getIntent().getSerializableExtra("query_map");    // Used to make a query to firestore

            if (query_map == null || uni_domain == null) {
                Log.e(TAG, "Must Provide a query map and uni domain.");
                finish();
            }
            else return true;
        }
        return false;
    }

    // Initialize recycler values and load first batch of items
    private void setRecycler(){
        recycler_view = findViewById(R.id.seeAll_recycler);

        // Set LayoutManager and Adapter
        
        adapter = new FeedAdapter(posts, this::onFeedClick);
        layout_man = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(layout_man);
        recycler_view.setAdapter(adapter);

        // Load Users with pagination
        constructQuery();
    }

    private void onSelectListener(int i, int i1) {
    }

    // Scroll listener
    private void setListeners(){

        // Scroll Listener used for pagination
        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
              @Override
              public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                  super.onScrollStateChanged(recyclerView, newState);
                  if(array_list_updated) {

                      if (layout_man.findLastCompletelyVisibleItemPosition() > (posts.size() - 3 )){
                          array_list_updated = false;
                          constructQuery();
                      }
                  }
              }
        });
    }


/* OnClick Methods
***************************************************************************************************/

    // OnClick: Handles clicks on a post item
    public void onFeedClick(int position, int clicked_id) {
        Post clickedPost = posts.get(position);     //<- this is the clicked event/post

        switch(clicked_id){
            case R.id.object_full_button:
            case R.id.object_full_text:
                viewPost(clickedPost);
                break;

            case R.id.object_posted_by_author:
                viewUserProfile(clickedPost.getAuthor_id(), clickedPost.getUni_domain(), clickedPost.getAuthor_is_organization());
                break;

            case R.id.object_pinned_event:
                loadPostFromDB(clickedPost.getPinned_id(), clickedPost.getUni_domain());
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

    // Load a post from database
    private void loadPostFromDB(String post_id, String post_uni_domain) {
        String address = "universities/" + post_uni_domain + "/posts/" + post_id;
        if (address.contains("null")){
            Log.e(TAG, "Address has null values.");
            return;
        }

        firebase_db.document(address).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Post post;
                if ((boolean)task.getResult().get("is_event")) post = task.getResult().toObject(Event.class);
                else post = task.getResult().toObject(Post.class);
                if (post != null){
                    post.setId(post_id);
                    viewPost(post);
                }
                else Log.e(TAG, "Post retrieved was null.");
            }
            else Log.e(TAG, "Something went wrong when retrieving Post.", task.getException());
        });
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


/* Firebase Methods
***************************************************************************************************/

    // Load the next batch of posts
    private void constructQuery(){
        String address = "universities/" + uni_domain + "/posts";
        if (address.contains("null")){
            Log.e(TAG, "Address has null values.");
            return;
        }

        // Build Query given query_map (K = field name, V = field value)
        CollectionReference ref = firebase_db.collection(address);
        Query query = null;

        for (String k : query_map.keySet()){
            if (query == null) query = ref.whereEqualTo(k,query_map.get(k));
            else query = query.whereEqualTo(k,query_map.get(k));

            Log.d(TAG, k + " : " + query_map.get(k));
        }

        if (query == null){
            Log.e(TAG, "Query was not constructed properly!");
            return;
        }

        // Pagination: First batch different from rest
        query = query.limit(MIN_POSTS_LOADED).orderBy("creation_millis", Query.Direction.DESCENDING);
        if (last_doc != null) query = query.startAfter(last_doc);

        loadPosts(query);
    }

    // Get Posts from database
    private void loadPosts(Query query) {
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                QuerySnapshot query_doc = task.getResult();
                int i = 0;
                for (QueryDocumentSnapshot doc : query_doc){

                    // Get post object
                    Post post;
                    if ((boolean)doc.get("is_event")) post = doc.toObject(Event.class);
                    else post = doc.toObject(Post.class);
                    post.setId(doc.getId());

                    // Add to list and tell recycler adapter
                    posts.add(post);
                    adapter.notifyItemInserted(posts.size()-1);

                    i++;
                }
                if (!query_doc.isEmpty())
                    last_doc = query_doc.getDocuments().get(query_doc.size()-1);    // Save last doc retrieved
                Log.d(TAG, i + " posts were uploaded from database!");
            }
            else Log.e(TAG, "loadPostsFromDB: unsuccessful or do not exist.", task.getException());
        });
    }
}