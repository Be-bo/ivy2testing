package com.ivy2testing.userProfile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.entities.Post;
import com.ivy2testing.home.ViewPostActivity;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.R;
import com.ivy2testing.entities.Student;
import com.ivy2testing.util.Constant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: Student Profile view fragment
 * Notes: Used for viewing both student's own profile as well as viewing other students' profiles
 */
public class StudentProfileFragment extends Fragment {

    // Constants
    private final static String TAG = "StudentProfileFragment";

    // Parent Final fields
    private UserViewModel user_view_model;
    private View root_view;

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
    private boolean my_profile;      // Don't show edit button if this is not myProfile
    private Student student;
    private ImageAdapter adapter;
    private Uri profile_img_uri;
    private List<Post> posts = new ArrayList<>(6);        // Load first 6 posts only
    private List<Uri> post_img_uris = new ArrayList<>(6); // non synchronous adds!



    // Constructor
    public StudentProfileFragment(boolean my_profile) {
        this.my_profile = my_profile;
    }


/* Override Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_studentprofile, container, false);

        // Initialization Methods
        declareViews(root_view);
        getUserProfile();
        return root_view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.VIEW_POST_REQUEST_CODE) {
            Log.d(TAG, "Coming back from ViewPost!");
            if (resultCode == Activity.RESULT_OK && data != null) {
                boolean updated = data.getBooleanExtra("updated", false);
                if (updated) loadPostImg(data.getParcelableExtra("post"));
            }
        } else
            Log.w(TAG, "Don't know how to handle the request code, \"" + requestCode + "\" yet!");
    }


/* Initialization Methods
***************************************************************************************************/

    // Get User Data - always stays update and doesn't require passing anything because ViewModel is connected to the Activity that manages the fragment
    private void getUserProfile(){
        if (getActivity() != null) {

            user_view_model = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            student = user_view_model.getThisStudent().getValue(); //grab the initial data

            // Only start doing processes that depend on user profile
            if(student != null){
                Log.d(TAG, "Showing student: " + student.getId() + ", name: " + student.getName());
                setupViews();           // populate UI
                setUpRecycler();        // set up posts recycler view
                setListeners(root_view); // set up listeners
            }

            // listen to realtime user profile changes afterwards
            user_view_model.getThisStudent().observe(getActivity(), (Student updatedProfile) -> {
                if (updatedProfile != null){
                    student = updatedProfile;   // Update student
                    getStudentPic();            // Do Other setups
                }
            });
        }
    }

    private void declareViews(View v){
        mProfileImg = v.findViewById(R.id.studentProfile_circleImg);
        mName = v.findViewById(R.id.studentProfile_name);
        mDegree = v.findViewById(R.id.studentProfile_degree);
        mRecyclerView = v.findViewById(R.id.studentProfile_posts);
        mLoadingLayout = v.findViewById(R.id.studentProfile_loading);
        mLoadingProgressBar = v.findViewById(R.id.studentProfile_progressBar);
        mPostError = v.findViewById(R.id.studentProfile_errorMsg);
        mSeeAll = v.findViewById(R.id.studentProfile_seeAll);
    }

    private void setupViews(){
        if (student == null) return;
        mName.setText(student.getName());
        mDegree.setText(student.getDegree());
        if (profile_img_uri != null) Picasso.get().load(profile_img_uri).into(mProfileImg);
    }

    // Create adapter for recycler (empty!)
    private void setUpRecycler(){
        loadPostsFromDB();

        // set LayoutManager and Adapter
        adapter = new ImageAdapter(post_img_uris);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(adapter);
    }

    // Set up onClick Listeners
    private void setListeners(View v){
        if (my_profile) v.findViewById(R.id.studentProfile_edit).setOnClickListener(v12 -> editProfile());
        else    v.findViewById(R.id.studentProfile_edit).setVisibility(View.GONE);
        v.findViewById(R.id.studentProfile_seeAll).setOnClickListener(v1 -> seeAllPosts());
        adapter.setOnSelectionListener(this::selectPost);
    }

/* OnClick Methods
***************************************************************************************************/

    // Edit profile
    private void editProfile(){
        Intent intent = new Intent(getActivity(), EditStudentProfileActivity.class);
        Log.d(TAG, "Starting EditProfile Activity for student: " + student.getId());
        intent.putExtra("student", student);

        // onActivityResult in MainActivity gets called!
        if (getActivity() != null)
            startActivityForResult(intent, Constant.EDIT_STUDENT_REQUEST_CODE);
        else
            Log.e(TAG, "getActivity() was null when calling EditProfile.");
    }

    // See all posts TODO
    private void seeAllPosts(){}

    // A post in recycler was selected
    private void selectPost(int position) {

        Intent intent = new Intent(getContext(), ViewPostActivity.class);
        Log.d(TAG, "Starting ViewPost Activity for post #" + position);
        intent.putExtra("post", posts.get(position));
        intent.putExtra("this_user_id", student.getId());
        startActivityForResult(intent, Constant.VIEW_POST_REQUEST_CODE);
    }


/* Transition Methods
***************************************************************************************************/

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


/* Firebase Methods
***************************************************************************************************/

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getStudentPic() {
        if (student == null) return;

        // Make sure student has a profile image already
        if (student.getProfile_picture() != null){
            base_storage_ref.child(student.getProfile_picture()).getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            profile_img_uri = task.getResult();
                        }
                        else {
                            Log.w(TAG, task.getException());
                            student.setProfile_picture(""); // image doesn't exist
                        }

                        // Reload views
                        setupViews();
                    });
        } else {
            // Reload views
            setupViews();
        }
    }

    // Load a maximum of 6 posts from FireStore give its id
    private void loadPostsFromDB(){
        startLoading();
        if (student.getId() == null || student.getUni_domain() == null){
            Log.e(TAG, "Post Address has null values. ID:" + student.getId());
            if (getContext() != null) postError(getString(R.string.error_noPosts));
            return;
        }

        if (student.getPost_ids().size() == 0){
            Log.e(TAG, "No posts for this user.");
            postError(getString(R.string.error_noPosts));
            return;
        }

        String address = "universities/" + student.getUni_domain() + "/posts";

        db.collection(address).whereEqualTo("author_id", student.getId()).limit(6)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot == null){
                            Log.e(TAG, "No posts for this user.");
                            postError(getString(R.string.error_noPosts));
                            return;
                        }
                        int i = 0;
                        for (QueryDocumentSnapshot doc : querySnapshot){
                            posts.add(i, doc.toObject(Post.class));
                            if (posts.get(i) == null) Log.e(TAG, "Post object obtained from database is null!");
                            else {
                                posts.get(i).setId(doc.getId());    // Set Post ID
                                loadPostImg(posts.get(i));          // Upload pic and update views
                            }
                            i++;
                        }
                        Log.d(TAG, "There were " + i + " posts!");
                        stopLoading();
                    }
                    else {
                        Log.e(TAG,"loadPostsFromDB: unsuccessful!");
                        postError(getString(R.string.error_getPost));
                    }
                });
    }

    // Load a post's visual from Firestore Storage given post object
    private void loadPostImg(final Post post){
        if (post == null){
            Log.e(TAG, "Post was null!");
            return;
        }

        int postIndex = posts.indexOf(post);
        if (postIndex < post_img_uris.size() && post_img_uris.get(postIndex) != null) {
            Log.d(TAG,"Post Image already loaded!");
            return;
        }

        // Insert Image Uri to arrayList in same position as its post object
        // Then it tells Recycler adapter that it should update the view
        if (post.getVisual() != null){
            base_storage_ref.child(post.getVisual()).getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Uri uri = task.getResult();
                            if (uri != null){
                                post_img_uris.add(uri);
                                adapter.notifyItemInserted(post_img_uris.size()-1);
                                Log.d(TAG, "Added to position "+posts.indexOf(post)+" img " + uri);
                            }
                        }
                        else Log.w(TAG, "this post's image isn't here! Visual: " + post.getVisual());
                    });
        } else Log.e(TAG, "Post had null visual! Post ID: "+ post.getId());
    }
}
