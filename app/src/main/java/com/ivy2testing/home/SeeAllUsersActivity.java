package com.ivy2testing.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.util.adapters.UserAdapter;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: an activity with a single recyclerView and tab bar,
 *           used to show a list of users (list of user ids passed by intent)
 */
public class SeeAllUsersActivity extends AppCompatActivity implements UserAdapter.OnUserItemClickListener {

    // Constants
    private final static String TAG = "SeeAllUsersActivity";

    // Views
    private RecyclerView recycler_view;

    // Firebase
    private FirebaseFirestore firebase_db = FirebaseFirestore.getInstance();

    // Variables passed by intent
    private String appbar_title;    // Optional title
    private String viewer_id;       // Current user
    private String this_uni_domain; // Uni Domain
    private List<String> user_ids;  // List of user_ids

    // Recycler variables
    private UserAdapter adapter;
    private List<User> users = new ArrayList<>();
    private LinearLayoutManager layout_man;

    // Variables used for pagination
    private final static int MIN_USERS_LOADED = 15;
    private int last_user_pos = 0;                  // Position of last user loaded


    /* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeall);

        // Initialization
        getIntentExtras();  // Get recycler "mode" and a query via intent
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
    private void getIntentExtras(){
        if (getIntent() != null){

            appbar_title = getIntent().getStringExtra("title");             // Optional activity title
            viewer_id = getIntent().getStringExtra("viewer_id");            // id of currently logged in user
            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            user_ids = getIntent().getStringArrayListExtra("user_ids");     // List of user ids to be displayed

            //query_map = (Map<String, Object>) getIntent().getSerializableExtra("query_map");
            //intent.putExtra("query_map", (Serializable) this_user_profile); //TODO: use somewhere else maybe?

            if (user_ids == null || this_uni_domain == null) {
                Log.e(TAG, "Must Provide a list of user ids and uni domain.");
                finish();
            }
        }
    }

    // Initialize recycler values and load first batch of items
    private void setRecycler(){
        recycler_view = findViewById(R.id.seeAll_recycler);

        // Set LayoutManager and Adapter
        adapter = new UserAdapter(users);
        layout_man = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(layout_man);
        recycler_view.setAdapter(adapter);

        // Load Users with pagination
        if (!user_ids.isEmpty()) loadUsers();
    }

    // OnClick and scroll listeners
    private void setListeners(){

        // Check onNameClick and onOptionClick methods
        adapter.setOnUserItemClickListener(this);

        // Scroll Listener used for pagination (TODO not fully tested yet)
        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Check if we've scrolled to the bottom and if there are more users to be loaded
                if (last_user_pos < user_ids.size() - 1){
                    int firstVisibleItem = layout_man.findFirstVisibleItemPosition();
                    int visibleItemCount = layout_man.getChildCount();
                    int totalItemCount = layout_man.getItemCount();

                    if (firstVisibleItem + visibleItemCount == totalItemCount){
                        loadUsers();
                    }
                }
            }
        });
    }


/* OnClick Methods
***************************************************************************************************/

    @Override
    public void onNameClick(int position) {
        //TODO go to this user's profile
        Toast.makeText(this,"Name: " + users.get(position).getName(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOptionsClick(int position) {
        // TODO Show a spinner of options
        Toast.makeText(this,"Options for: " + users.get(position).getName(),Toast.LENGTH_SHORT).show();
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

    // Use Pagination to load Users (Recycler will load preview pictures)
    private void loadUsers(){
        // load a certain number of users at a time
        int i;
        for (i = last_user_pos; i < last_user_pos + MIN_USERS_LOADED; i++ ){
            if (i >= user_ids.size()) break;
            loadUser(user_ids.get(i));
        }
        last_user_pos = i;      // Update last position
    }

    // Load a user given its id (assuming they all share same uni domain)
    private void loadUser(String user_id){
        String address = "universities/" + this_uni_domain + "/users/" + user_id;
        if (address.contains("null")){
            Log.e(TAG, "User Address has null values. ID:" + user_id);
            return;
        }

        firebase_db.document(address).get().addOnCompleteListener( task->{
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    user.setId(user_id);
                    users.add(user);
                    adapter.notifyItemInserted(users.size()-1);
                } else Log.e(TAG, "User was null!");
            }
            else {
                Log.e(TAG, "loadUserFromDB: unsuccessful or does not exist.");
            }
        });

    }
}
