package com.ivy2testing.userProfile;

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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.SeeAllPostsActivity;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.R;
import com.ivy2testing.entities.Student;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.adapters.SquareImageAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: Student Profile view fragment
 * Notes: Used for viewing both student's own profile as well as viewing other students' profiles
 */
public class StudentProfileFragment extends Fragment implements SquareImageAdapter.OnPostListener {

    // Constants
    private final static String TAG = "StudentProfileFragmentTag";

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
    private String viewer_id;
    private Student student;
    private SquareImageAdapter adapter;
    private Uri profile_img_uri;
    private boolean is_set_up = false;
    public boolean isIs_set_up() {
        return is_set_up;
    }



    // Constructors
    public StudentProfileFragment(boolean my_profile) {
        this.my_profile = my_profile;
    }

    public StudentProfileFragment(boolean my_profile, String viewer_id) {
        this.my_profile = my_profile;
        this.viewer_id = viewer_id;
    }

    public void setStudent(Student student){
        this.student = student;
    }


/* Override Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_studentprofile, container, false);
        Log.d(TAG,"onCreateView!");
        declareViews(root_view);
        return root_view;
    }

    public void setUp(){
        is_set_up = true;
        // Initialization Methods
        if (student == null) getUserProfile();
        else setUp();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null) adapter.cleanUp();
    }

    /* Initialization Methods
***************************************************************************************************/

    // Get User Data - always stays update and doesn't require passing anything because ViewModel is connected to the Activity that manages the fragment
    private void getUserProfile(){
        if (getActivity() != null) {

            // Parent Final fields
            UserViewModel user_view_model = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            User usr = user_view_model.getThis_user().getValue();
            if (usr instanceof Student) {
                student = (Student) usr; //grab the initial data

                // Only start doing processes that depend on user profile
                setUpElements();
            }

            // listen to realtime user profile changes afterwards
            user_view_model.getThis_user().observe(getActivity(), (User updatedProfile) -> {
                if (updatedProfile instanceof Student){
                    student = (Student) updatedProfile;   // Update student
                    getStudentPic();            // Do Other setups
                }
            });
        }
    }

    // General setup after acquiring student object
    private void setUpElements(){
        Log.d(TAG, "Showing student: " + student.getId() + ", name: " + student.getName());
        setupViews();               // populate UI
        setUpRecycler();            // set up posts recycler view
        setListeners(root_view);    // set up listeners
        getStudentPic();            // Do Other setups
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

    // Create adapter for recycler (adapter pulls posts from database)
    private void setUpRecycler(){
        startLoading();

        // set LayoutManager and Adapter
        adapter = new SquareImageAdapter(student.getId(), student.getUni_domain(), 9, getContext(), this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), 3, GridLayoutManager.VERTICAL, false));
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

    // Set up onClick Listeners
    private void setListeners(View v) {
        if (my_profile) v.findViewById(R.id.studentProfile_edit).setOnClickListener(v12 -> editProfile());
        else v.findViewById(R.id.studentProfile_edit).setVisibility(View.GONE);
        v.findViewById(R.id.studentProfile_seeAll).setOnClickListener(v1 -> seeAllPosts());
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

    // See all posts
    private void seeAllPosts(){
        Intent intent = new Intent(getContext(), SeeAllPostsActivity.class);
        if (my_profile) intent.putExtra("viewer_id", student.getId());
        else intent.putExtra("viewer_id", viewer_id);
        intent.putExtra("this_uni_domain", student.getUni_domain());

        // Make "Query"
        HashMap<String, Object> query_map = new HashMap<String, Object>() {{ put("author_id", student.getId()); }};
        intent.putExtra("query_map", query_map);

        startActivityForResult(intent, Constant.SEEALL_POSTS_REQUEST_CODE);
    }

    // A post in recycler was selected
    @Override
    public void onPostClick(int position) {
        Intent intent = new Intent(getContext(), ViewPostOrEventActivity.class);
        intent.putExtra("viewer_id", student.getId());
        intent.putExtra("post", adapter.getItem(position));
        startActivity(intent);
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

        base_storage_ref.child(ImageUtils.getProfilePath(student.getId())).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) profile_img_uri = task.getResult();
                    else Log.w(TAG, task.getException());

                    // Reload views
                    setupViews();
                });
    }
}
