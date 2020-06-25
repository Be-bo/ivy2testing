package com.ivy2testing.userProfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.SeeAllPostsActivity;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.adapters.SquareImageAdapter;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/** @author Zahra Ghavasieh
 * Overview: 3rd party Student Profile view Activity.
 * Features: Student can be passed by intent or can be retrieved from database given its address (id and uni_domain)
 */
public class StudentProfileActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "StudentProfileActivity";

    // Views
    private ImageView mProfileImg;
    private TextView mName;
    private TextView mDegree;
    private RecyclerView mRecyclerView;
    private FrameLayout mLoadingLayout;
    private ProgressBar mLoadingProgressBar;
    private TextView mPostError;
    private TextView mSeeAll;

    // Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private SquareImageAdapter adapter;
    private Uri profile_img_uri;

    // User Variables
    private Student student_toDisplay;  // Student whose profile we're looking at
    private User this_user;             // Currently logged in user (Nullable)


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getIntentExtras();      // Get student (via intent or database) and set up elements
        setTitle(null);         // set up toolBar as an actionBar
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

    // General setup after acquiring student object
    private void setUpElements(){
        Log.d(TAG, "Showing student: " + student_toDisplay.getId() + ", name: " + student_toDisplay.getName());
        declareViews();
        setUpViews();               // populate UI
        setUpRecycler();            // set up posts recycler view
        getStudentPic();            // Do Other setups
    }

    private void declareViews(){
        mProfileImg = findViewById(R.id.studentProfile_circleImg);
        mName = findViewById(R.id.studentProfile_name);
        mDegree = findViewById(R.id.studentProfile_degree);
        mRecyclerView = findViewById(R.id.studentProfile_posts);
        mLoadingLayout = findViewById(R.id.studentProfile_loading);
        mLoadingProgressBar = findViewById(R.id.studentProfile_progressBar);
        mPostError = findViewById(R.id.studentProfile_errorMsg);
        mSeeAll = findViewById(R.id.studentProfile_seeAll);

        findViewById(R.id.studentProfile_edit).setVisibility(View.GONE);
        findViewById(R.id.studentProfile_seeAll).setOnClickListener(v -> seeAllPosts());
    }

    private void setUpViews(){
        if (student_toDisplay == null) return;
        mName.setText(student_toDisplay.getName());
        mDegree.setText(student_toDisplay.getDegree());
        if (profile_img_uri != null) Picasso.get().load(profile_img_uri).into(mProfileImg);
    }

    // Create adapter for recycler (adapter pulls posts from database)
    private void setUpRecycler(){
        startLoading();

        // set LayoutManager and Adapter
        adapter = new SquareImageAdapter(student_toDisplay.getId(), student_toDisplay.getUni_domain(), 9, this, this::onPostClick);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(adapter);

        // Set a Listener for when adapter gets posts
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.d(TAG, "Found " + adapter.getItemCount() + " posts!");
                if (adapter.getItemCount() > 0) stopLoading();
                else postError(getString(R.string.error_noPosts));
            }
        });
    }


/* OnClick Methods
***************************************************************************************************/

    // See all posts
    private void seeAllPosts(){
        Intent intent = new Intent(this, SeeAllPostsActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("uni_domain", student_toDisplay.getUni_domain());

        // Make "Query"
        HashMap<String, Object> query_map = new HashMap<String, Object>() {{ put("author_id", student_toDisplay.getId()); }};
        intent.putExtra("query_map", query_map);

        startActivityForResult(intent, Constant.SEEALL_POSTS_REQUEST_CODE);
    }

    // A post in recycler was selected
    public void onPostClick(int position) {
        Log.d(TAG, "Getting post: " + adapter.getItem(position).getId());
        Intent intent = new Intent(this, ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("uni_domain", student_toDisplay.getUni_domain());
        intent.putExtra("post", adapter.getItem(position));
        startActivity(intent);
    }


/* Transition Methods
***************************************************************************************************/

    // Get user (or address in firebase)
    private void getIntentExtras(){
        if (getIntent() != null){

            this_user = getIntent().getParcelableExtra("this_user");
            student_toDisplay = getIntent().getParcelableExtra("student_to_display");

            if (student_toDisplay == null) {
                String student_id = getIntent().getStringExtra("student_to_display_id");
                String student_uni = getIntent().getStringExtra("student_to_display_uni");

                if (student_id == null || student_uni == null) {
                    Log.e(TAG, "Student Address is null!");
                    finish();
                }
                else getUserInfo(student_id, student_uni);
            }
            else setUpElements();
        }
        else {
            Log.e(TAG, "No Intent");
            finish();
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

    // Loading Post images Animation
    private void startLoading(){
        mRecyclerView.setVisibility(View.GONE);
        mPostError.setVisibility(View.GONE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mLoadingLayout.setVisibility(View.VISIBLE);
    }

    // Stop loading Animation
    private void stopLoading(){
        mLoadingLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // Display blank screen with an error message instead of posts
    private void postError(String error_msg){
        mRecyclerView.setVisibility(View.GONE);
        mLoadingProgressBar.setVisibility(View.GONE);
        mSeeAll.setVisibility(View.GONE);
        mPostError.setVisibility(View.VISIBLE);
        mPostError.setText(error_msg);
        mLoadingLayout.setVisibility(View.VISIBLE);
    }

    // In case of mistake, go to Organization Profile Activity
    private void viewOrganization(String user_id, String user_uni) {
        Log.d(TAG, "Starting OrganizationProfile Activity for organization " + user_id);
        Intent intent = new Intent(this, OrganizationProfileActivity.class);
        intent.putExtra("org_to_display_id", user_id);
        intent.putExtra("org_to_display_uni", user_uni);
        intent.putExtra("this_user", this_user);
        startActivity(intent);
        finish();
    }


/* Firebase Methods
***************************************************************************************************/

    // Get all student info and return student class
    public void getUserInfo(String user_id, String user_uni) {
        String address = "universities/" + user_uni + "/users/" + user_id;
        if (address.contains("null")) {
            Log.e(TAG, "User Address has null values.");
            return;
        }

        db.document(address).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                DocumentSnapshot doc = task.getResult();
                if ((boolean) doc.get("is_organization")) {
                    Log.e(TAG, "This user is an organization!");
                    viewOrganization(user_id, user_uni);
                }
                else student_toDisplay = task.getResult().toObject(Student.class);

                if (student_toDisplay != null){
                    student_toDisplay.setId(user_id);
                    setUpElements();
                }
                else {
                    Log.e(TAG, "user was null!");
                    goBackToParent();
                }

            }
            else {
                Log.e(TAG, "There was a problem when loading user at: " + address);
                goBackToParent();
            }
        });
    }

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getStudentPic() {
        if (student_toDisplay == null) return;

        base_storage_ref.child(ImageUtils.getProfilePath(student_toDisplay.getId())).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) profile_img_uri = task.getResult();
                    else Log.w(TAG, task.getException());

                    // Reload views
                    setUpViews();
                });
    }
}