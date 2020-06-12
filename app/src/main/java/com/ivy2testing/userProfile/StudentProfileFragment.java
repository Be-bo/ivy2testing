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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.entities.Post;
import com.ivy2testing.home.ViewPostActivity;
import com.ivy2testing.util.FragCommunicator;
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

    // Parent activity
    private FragCommunicator mCommunicator; // For communications to activity

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
    private boolean myProfile;      // Don't show edit button if this is not myProfile
    private Student student;
    private ImageAdapter adapter;
    private Uri profileImgUri;
    private List<Post> posts = new ArrayList<>(6);        // Load first 6 posts only
    private List<Uri> postImgUris = new ArrayList<>(6);  // non synchronous adds!


    // Constructor
    public StudentProfileFragment(Student student, Uri profileImgUri, boolean myProfile) {
        this.student = student;
        this.profileImgUri = profileImgUri;
        this.myProfile = myProfile;
    }

    // Setter for communicator
    public void setCommunicator(FragCommunicator communicator) {
        mCommunicator = communicator;
    }


/* Override Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_studentprofile, container, false);

        // Initialization Methods
        declareViews(rootView);
        setupViews();
        setUpRecycler();
        setListeners(rootView);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Came back from edit student activity (Change to a switch statement if more request codes)
        if (requestCode == Constant.EDIT_STUDENT_REQUEST_CODE) {
            Log.d(TAG, "Coming back from EditStudent!");
            if (resultCode == Activity.RESULT_OK && data != null) {
                boolean updated = data.getBooleanExtra("updated", false);
                if (updated) reloadStudent();
            }
        }
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
        if (profileImgUri!= null) Picasso.get().load(profileImgUri).into(mProfileImg);
    }

    // Create adapter for recycler (empty!)
    private void setUpRecycler(){
        loadPostsFromDB();

        // set LayoutManager and Adapter
        adapter = new ImageAdapter(postImgUris);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(adapter);
    }

    // Set up onClick Listeners
    private void setListeners(View v){
        if (myProfile) v.findViewById(R.id.studentProfile_edit).setOnClickListener(v12 -> editProfile());
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

    // Reload student profile
    private void reloadStudent() {
        if (student == null) return;

        final String this_user_id = student.getId();
        String address = "universities/" + student.getUni_domain() + "/users/" + student.getId();
        if (address.contains("null")){
            Log.e(TAG, "Student Address has null values.");
            return;
        }

        db.document(address).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if (doc == null){
                    Log.e(TAG, "Document doesn't exist");
                    return;
                }
                student = doc.toObject(Student.class);
                if (student == null) Log.e(TAG, "Student object obtained from database is null!");
                else {
                    student.setId(this_user_id);    // Set student ID
                    mCommunicator.message(student); // Tell MainActivity to use new student
                    getStudentPic();                // Upload pic and update views
                }
            }
            else Log.e(TAG,"getUserInfo: unsuccessful!");
        });
    }

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getStudentPic() {
        if (student == null) return;

        // Make sure student has a profile image already
        if (student.getProfile_picture() != null){
            base_storage_ref.child(student.getProfile_picture()).getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            profileImgUri = task.getResult();
                        }
                        else {
                            Log.w(TAG, task.getException());
                            student.setProfile_picture(""); // image doesn't exist
                        }

                        // Reload views
                        setupViews();
                        loadPostsFromDB();
                    });
        } else {
            // Reload views
            setupViews();
            loadPostsFromDB();
        }
    }

    // Load a maximum of 6 posts from FireStore give its id
    private void loadPostsFromDB(){
        startLoading();
        if (student.getId() == null || student.getUni_domain() == null){
            Log.e(TAG, "Post Address has null values. ID:" + student.getId());
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
        if (postIndex < postImgUris.size() && postImgUris.get(postIndex) != null) {
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
                                postImgUris.add(uri);
                                adapter.notifyItemInserted(postImgUris.size()-1);
                                Log.d(TAG, "Added to position "+posts.indexOf(post)+" img " + uri);
                            }
                        }
                        else Log.w(TAG, "this post's image isn't here! Visual: " + post.getVisual());
                    });
        } else Log.e(TAG, "Post had null visual! Post ID: "+ post.getId());
    }
}
