package com.ivy2testing.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
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
    private User this_user;         // Current user
    private String uni_domain;      // Uni Domain of users to view
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
        getIntentExtras();  // Get array for recycler
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
            this_user = getIntent().getParcelableExtra("this_user");        // currently logged in user
            uni_domain = getIntent().getStringExtra("uni_domain");
            user_ids = getIntent().getStringArrayListExtra("user_ids");     // List of user ids to be displayed

            if (user_ids == null || uni_domain == null) {
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

    // OnClick for user name/image: view profile
    @Override
    public void onUserClick(int position) {
        User user = users.get(position);
        Intent intent;

        if (user.getIs_organization()) {
            Log.d(TAG, "Starting OrganizationProfile Activity for organization " + user.getId());
            intent = new Intent(this, OrganizationProfileActivity.class);
            // TODO @Robert, pass user as a parcel instead?
            intent.putExtra("org_to_display_id", user.getId());
            intent.putExtra("org_to_display_uni", user.getUni_domain());
        } else {
            Log.d(TAG, "Starting StudentProfile Activity for student " + user.getId());
            intent = new Intent(this, StudentProfileActivity.class);
            intent.putExtra("student_to_display", user);
        }
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }

    // OnClick for options button: open a popup menu
    @Override
    public void onOptionsClick(int position, View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // Hide certain items if not logged in
        if (this_user == null){
            Menu menu = popup.getMenu();
            menu.findItem(R.id.userOptions_addFriend).setEnabled(false);
            menu.findItem(R.id.userOptions_chat).setEnabled(false);
        }

        // TODO: Set onClick
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.userOptions_viewProfile:
                    onUserClick(position);
                    return true;

                case R.id.userOptions_addFriend:
                    Toast.makeText(SeeAllUsersActivity.this,"Add friend: " + user_ids.get(position),Toast.LENGTH_SHORT).show();
                    return true;

                case R.id.userOptions_chat:
                    Toast.makeText(SeeAllUsersActivity.this,"Chat",Toast.LENGTH_SHORT).show();
                    return true;

                default:
                    return false;
            }
        });
        popup.inflate(R.menu.user_options);
        popup.show();
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
        String address = "universities/" + uni_domain + "/users/" + user_id;
        if (address.contains("null")){
            Log.e(TAG, "User Address has null values. ID:" + user_id);
            return;
        }

        firebase_db.document(address).get().addOnCompleteListener( task->{
            if (task.isSuccessful() && task.getResult() != null) {
                User user;
                DocumentSnapshot doc = task.getResult();
                if ((boolean) doc.get("is_organization"))
                    user = task.getResult().toObject(Organization.class);
                else user = task.getResult().toObject(Student.class);

                if (user != null) {
                    user.setId(user_id);
                    users.add(user);
                    adapter.notifyItemInserted(users.size() - 1);
                }
                else Log.e(TAG, "User was null!");
            }
            else Log.e(TAG, "loadUserFromDB: unsuccessful or does not exist.");
        });

    }
}
