package com.ivy2testing.userProfile;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.chat.ChatroomActivity;
import com.ivy2testing.entities.Chatroom;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.hometab.ViewPostOrEventActivity;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.Utils;
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
    private TextView post_title;
    private TextView no_posts_text;
    private TextView private_text;
    private View contents;
    private ProgressBar progress_bar;
    private MenuItem block_button;
    private boolean isBlocked = false;

    // Firestore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private ProfilePostAdapter adapter;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (student_to_display != null && this_user.getId().equals(student_to_display.getId()))
            return super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userprofile_options_button, menu);
        block_button = menu.findItem(R.id.userprofile_block);
        Utils.colorMenuItem(block_button, getColor(R.color.red));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handling up button for when another activity called it (it will simply go back to main otherwise)
        int id = item.getItemId();
        if (id == android.R.id.home && !isTaskRoot()){
            goBackToParent();
            return true;
        }
        else if (id == R.id.userprofile_block) blockAction();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        post_title = findViewById(R.id.studentProfile_header);
        no_posts_text = findViewById(R.id.studentProfile_no_posts_text);
        private_text = findViewById(R.id.activity_student_profile_private_text);
        contents = findViewById(R.id.activity_student_profile_contents);
        progress_bar = findViewById(R.id.studentProfile_progress_bar);

        // Change to message icons and add onClickListeners
        TextView tv_message = findViewById(R.id.studentProfile_action);
        tv_message.setText(R.string.message);
        tv_message.setOnClickListener(this::newChatroom);
        ImageView ic_message = findViewById(R.id.studentProfile_action_icon);
        ic_message.setImageResource(R.drawable.ic_chat_selected);
        ic_message.setOnClickListener(this::newChatroom);
    }

    private void setUpViews(){
        if (student_to_display == null) return;
        mName.setText(student_to_display.getName());
        mDegree.setText(student_to_display.getDegree());
        if (profile_img_uri != null) Picasso.get().load(profile_img_uri).into(mProfileImg);


        if (this_user.getId().equals(student_to_display.getId())){
            findViewById(R.id.studentProfile_action).setVisibility(View.GONE);
            findViewById(R.id.studentProfile_action_icon).setVisibility(View.GONE);
            if (block_button != null) block_button.setVisible(false);
        }
        else if (block_button != null) {
            isBlocked = this_user.getBlocked_users().contains(student_to_display.getId());
            if (isBlocked) block_button.setTitle(R.string.unblock);
            else block_button.setTitle(R.string.block);
        }
    }

    // Create adapter for recycler (adapter pulls posts from database)
    private void setUpRecycler(){
        // set LayoutManager and Adapter
        List<View> allViews = new ArrayList<>();
        allViews.add(mRecyclerView);
        allViews.add(post_title);
        adapter = new ProfilePostAdapter(student_to_display.getId(), student_to_display.getUni_domain(), Constant.PROFILE_POST_LIMIT_STUDENT, this, this::onPostClick, allViews, no_posts_text, mRecyclerView, progress_bar);
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

    //Block or unblock a user
    private void blockAction(){
        if (isBlocked){
            block_button.setTitle(R.string.block);
            unblockUser();
            isBlocked = false;
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.block_alert_title)
                    .setMessage(R.string.block_alert_message)
                    .setPositiveButton(R.string.block, (dialog, which) -> {
                        block_button.setTitle(R.string.unblock);
                        blockUser();
                        isBlocked = true;
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
        Log.e(TAG, "isBlocked?: " + isBlocked);
    }

    // A post in recycler was selected
    public void onPostClick(int position) {
        Intent intent = new Intent(this, ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post_uni", adapter.getItem(position).getUni_domain());
        intent.putExtra("post_id", adapter.getItem(position).getId());
        intent.putExtra("author_id", adapter.getItem(position).getAuthor_id());
        startActivity(intent);
    }

    // Open ChatroomActivity with new Chatroomm
    public void newChatroom(View v) {
        Intent intent = new Intent(this, ChatroomActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("partner", student_to_display);
        intent.putExtra("chatroom", new Chatroom(this_user.getId(), student_to_display.getId()));
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
                    else {
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

    // Add to blocked_users and blockers
    private void blockUser() {
        db.document(User.getPath(this_user.getId())).update("blocked_users", FieldValue.arrayUnion(student_to_display.getId()));
        db.document(User.getPath(student_to_display.getId())).update("blockers", FieldValue.arrayUnion(this_user.getId()));
        removeChatrooms();
    }

    // Remove from block_users and blockers
    private void unblockUser() {
        db.document(User.getPath(this_user.getId())).update("blocked_users", FieldValue.arrayRemove(student_to_display.getId()));
        db.document(User.getPath(student_to_display.getId())).update("blockers", FieldValue.arrayRemove(this_user.getId()));
    }

    // If blocking, remove existing chatrooms
    private void removeChatrooms() {
        if (!this_user.getMessaging_users().contains(student_to_display.getId())) return;

        // Remove from messaging Lists
        db.document(User.getPath(this_user.getId())).update("messaging_users", FieldValue.arrayRemove(student_to_display.getId()));
        db.document(User.getPath(student_to_display.getId())).update("messaging_users", FieldValue.arrayRemove(this_user.getId()));

        // Remove Chatrooms (only one ArrayContains is allowed)
        // Note: must remove messages manually
        db.collection("conversations")
                .whereArrayContains("members", this_user.getId())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Chatroom room = doc.toObject(Chatroom.class);
                            if (room != null && room.getMembers().contains(student_to_display.getId()))
                                db.document(Chatroom.getPath(doc.getId())).delete();
                        }
                    }
        });
    }
}