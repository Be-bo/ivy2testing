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
 * Notes:Only used for viewing student's own profile
 */
public class StudentProfileFragment extends Fragment {

    // Constants
    private final static String TAG = "StudentProfileFragment";

    private View root_view;

    // Views
    private ImageView mProfileImg;
    private TextView mName;
    private TextView mDegree;
    private RecyclerView mRecyclerView;
    private TextView mSeeAll;
    private TextView post_title;

    // Firestore
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Student student;
    private SquareImageAdapter adapter;
    private Uri profile_img_uri;
    private boolean is_set_up = false;


    public boolean isIs_set_up() {
        return is_set_up;
    }


/* Override Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_studentprofile, container, false);
        declareViews(root_view);
        return root_view;
    }

    public void setUpProfile(){
        is_set_up = true;
        if (student == null) getUserProfile();
        else setUpElements();

        //TODO: remove
        Log.d(TAG, "setting up");
        Intent intent = new Intent(getContext(), OrganizationProfileActivity.class);
        intent.putExtra("this_user", student);
        intent.putExtra("org_to_display_id", "Z2xem5pMPsQzQqA27ZMpNJ6dcP82");
        intent.putExtra("org_to_display_uni", "ucalgary.ca");
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null) adapter.stopListening();
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
        setUpViews();               // populate UI
        setUpRecycler();            // set up posts recycler view
        setListeners(root_view);    // set up listeners
        getStudentPic();            // Do Other setups
    }

    private void declareViews(View v){
        mProfileImg = v.findViewById(R.id.studentProfile_circleImg);
        mName = v.findViewById(R.id.studentProfile_name);
        mDegree = v.findViewById(R.id.studentProfile_degree);
        mRecyclerView = v.findViewById(R.id.studentProfile_posts);
        mSeeAll = v.findViewById(R.id.studentProfile_seeAll);
        post_title = v.findViewById(R.id.studentProfile_header);
    }

    private void setUpViews(){
        if (student == null) return;
        mName.setText(student.getName());
        mDegree.setText(student.getDegree());
        if (profile_img_uri != null) Picasso.get().load(profile_img_uri).into(mProfileImg);
    }

    // Create adapter for recycler (adapter pulls posts from database)
    private void setUpRecycler(){

        // set LayoutManager and Adapter
        List<View> allViews = new ArrayList<>();
        allViews.add(mRecyclerView);
        allViews.add(mSeeAll);
        allViews.add(post_title);
        adapter = new SquareImageAdapter(student.getId(), student.getUni_domain(), Constant.PROFILE_POST_LIMIT_STUDENT, getContext(), this::onPostClick, allViews);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), Constant.PROFILE_POST_GRID_ROW_COUNT, GridLayoutManager.VERTICAL, false){
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = getWidth() / Constant.PROFILE_POST_GRID_ROW_COUNT;
                return true;
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    // Set up onClick Listeners
    private void setListeners(View v) {
        v.findViewById(R.id.studentProfile_edit).setOnClickListener(v12 -> editProfile());
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
        intent.putExtra("this_user", student);
        intent.putExtra("uni_domain", student.getUni_domain());

        // Make "Query"
        HashMap<String, Object> query_map = new HashMap<String, Object>() {{ put("author_id", student.getId()); }};
        intent.putExtra("query_map", query_map);

        startActivityForResult(intent, Constant.SEEALL_POSTS_REQUEST_CODE);
    }

    // A post in recycler was selected
    public void onPostClick(int position) {
        Log.d(TAG, "Getting post: " + adapter.getItem(position).getId());
        Intent intent = new Intent(getContext(), ViewPostOrEventActivity.class);
        intent.putExtra("this_user", student);
        intent.putExtra("uni_domain", student.getUni_domain());
        intent.putExtra("post", adapter.getItem(position));
        startActivity(intent);
    }

/* Firebase Methods
***************************************************************************************************/

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getStudentPic() {
        if (student == null) return;

        base_storage_ref.child(ImageUtils.getProfilePath(student.getId())).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) profile_img_uri = task.getResult();
                    else Log.w(TAG, task.getException());

                    // Reload views
                    setUpViews();
                });
    }
}
