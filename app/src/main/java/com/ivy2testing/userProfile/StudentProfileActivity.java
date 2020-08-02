package com.ivy2testing.userProfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.ivy2testing.main.SeeAllPostsActivity;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.adapters.SquarePostAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: 3rd party Student Profile view Activity.
 * Features: Student can be passed by intent or can be retrieved from database given its address (id and uni_domain)
 */
public class StudentProfileActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "StudentProfileActivityTag";

    // Views
    private ImageView mProfileImg;
    private TextView mName;
    private TextView mDegree;
    private RecyclerView mRecyclerView;
    private TextView mSeeAll;
    private TextView post_title;
    private TextView no_posts_text;
    private TextView private_text;
    private View contents;

    // Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private SquarePostAdapter adapter;
    private Uri profile_img_uri;

    // User Variables
    private Student student_to_display;  // Student whose profile we're looking at
    private User this_user;             // Currently logged in user (Nullable)


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        declareViews();
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

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null) adapter.stopListening();
    }

    /* Initialization Methods
***************************************************************************************************/

    // General setup after acquiring student object
    private void setUpElements(){
        setTitle("Profile");
        setUpViews();               // populate UI
        setUpRecycler();            // set up posts recycler view
        getStudentPic();            // Do Other setups
    }

    private void declareViews(){
        mProfileImg = findViewById(R.id.studentProfile_circleImg);
        mName = findViewById(R.id.studentProfile_name);
        mDegree = findViewById(R.id.studentProfile_degree);
        mRecyclerView = findViewById(R.id.studentProfile_posts);
        mSeeAll = findViewById(R.id.studentProfile_seeAll);
        post_title = findViewById(R.id.studentProfile_header);
        no_posts_text = findViewById(R.id.studentProfile_no_posts_text);
        private_text = findViewById(R.id.activity_student_profile_private_text);
        contents = findViewById(R.id.activity_student_profile_contents);
        findViewById(R.id.studentProfile_edit).setVisibility(View.GONE);
    }

    private void setUpViews(){
        if (student_to_display == null) return;
        mName.setText(student_to_display.getName());
        mDegree.setText(student_to_display.getDegree());
        mSeeAll.setOnClickListener(v -> seeAllPosts());
        if (profile_img_uri != null) Picasso.get().load(profile_img_uri).into(mProfileImg);
    }

    // Create adapter for recycler (adapter pulls posts from database)
    private void setUpRecycler(){
        // set LayoutManager and Adapter
        List<View> allViews = new ArrayList<>();
        allViews.add(mRecyclerView);
        allViews.add(mSeeAll);
        allViews.add(post_title);
        adapter = new SquarePostAdapter(student_to_display.getId(), student_to_display.getUni_domain(), Constant.PROFILE_POST_LIMIT_STUDENT, this, this::onPostClick, allViews, no_posts_text);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Constant.PROFILE_POST_GRID_ROW_COUNT, GridLayoutManager.VERTICAL, false){
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = getWidth() / Constant.PROFILE_POST_GRID_ROW_COUNT;
                return true;
            }
        });
        mRecyclerView.setAdapter(adapter);
    }


/* OnClick Methods
***************************************************************************************************/

    // See all posts
    private void seeAllPosts(){
        Intent intent = new Intent(this, SeeAllPostsActivity.class);
        intent.putExtra("title", student_to_display.getName()+"'s Posts");
        intent.putExtra("this_user", this_user);
        intent.putExtra("uni_domain", student_to_display.getUni_domain());
        intent.putExtra("author_id", student_to_display.getId());
        startActivity(intent);
    }

    // A post in recycler was selected
    public void onPostClick(int position) {
        Intent intent = new Intent(this, ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post_uni", adapter.getItem(position).getUni_domain());
        intent.putExtra("post_id", adapter.getItem(position).getId());
        startActivity(intent);
    }


/* Transition Methods
***************************************************************************************************/

    // Get user (or address in firebase)
    private void getIntentExtras(){
        if (getIntent() != null){

            this_user = getIntent().getParcelableExtra("this_user");
            student_to_display = getIntent().getParcelableExtra("student_to_display");

            if (student_to_display == null) {
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
        String address = "users/" + user_id;
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
                else student_to_display = task.getResult().toObject(Student.class);

                if (student_to_display != null){

                    if(student_to_display.isIs_private()) setPrivateDisplay();
                    else{
                        student_to_display.setId(user_id);
                        setUpElements();
                    }
                } else {
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

    private void setPrivateDisplay(){
        private_text.setVisibility(View.VISIBLE);
        contents.setVisibility(View.GONE);
    }

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getStudentPic() {
        if (student_to_display == null) return;

        base_storage_ref.child(ImageUtils.getUserImagePath(student_to_display.getId())).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) profile_img_uri = task.getResult();
                    else Log.w(TAG, task.getException());

                    // Reload views
                    setUpViews();
                });
    }
}