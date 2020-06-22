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
import com.ivy2testing.main.MainActivity;
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
    private String viewer_id;                   // Current user
    private String this_uni_domain;             // Uni Domain
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
        getIntentExtras();  // Get recycler a "query" via intent
        setUpToolBar();     // set up toolBar as an actionBar
        setRecycler();
        setListeners();
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
    private void getIntentExtras(){
        if (getIntent() != null){

            appbar_title = getIntent().getStringExtra("title");             // Optional activity title
            viewer_id = getIntent().getStringExtra("viewer_id");            // id of currently logged in user
            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            query_map = (HashMap<String, Object>) getIntent().getSerializableExtra("query_map");    // Used to make a query to firestore

            if (query_map == null || this_uni_domain == null) {
                Log.e(TAG, "Must Provide a query map and uni domain.");
                finish();
            }
        }
    }

    // Initialize recycler values and load first batch of items
    private void setRecycler(){
        recycler_view = findViewById(R.id.seeAll_recycler);

        // Set LayoutManager and Adapter
        adapter = new FeedAdapter(posts, this::onSelectListener);
        layout_man = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(layout_man);
        recycler_view.setAdapter(adapter);

        // Load Users with pagination
        constructQuery();
    }
    // Scroll listener
    private void setListeners(){

        // Scroll Listener used for pagination (TODO not fully tested yet)
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

    // OnClick: select post (May need to change later)
    private void onSelectListener(int position) {
        if (posts.get(position) instanceof Event)
            Log.d(TAG, "Starting ViewPost Activity for event #" + position);
        else Log.d(TAG, "Starting ViewPost Activity for post #" + position);

        Intent intent = new Intent(this, ViewPostOrEventActivity.class);
        intent.putExtra("post", posts.get(position));
        intent.putExtra("viewer_id", viewer_id);
        startActivityForResult(intent, Constant.VIEW_POST_REQUEST_CODE);
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
        String address = "universities/" + this_uni_domain + "/posts";
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