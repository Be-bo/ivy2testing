package com.ivy2testing.userProfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.SeeAllPostsActivity;
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
    private ImageView profile_image;
    private TextView name_text;
    private TextView degree_text;
    private RecyclerView post_recycler;
    private TextView seeall_posts;
    private TextView post_title;
    private TextView no_posts_text;

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
        Intent intent = new Intent(getContext(), OrganizationProfileActivity.class);
        intent.putExtra("this_user", student);
        intent.putExtra("org_to_display_id", "Z2xem5pMPsQzQqA27ZMpNJ6dcP82");
        intent.putExtra("org_to_display_uni", "ucalgary.ca");
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null && is_set_up) adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null && is_set_up) adapter.startListening();
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
        profile_image = v.findViewById(R.id.studentProfile_circleImg);
        name_text = v.findViewById(R.id.studentProfile_name);
        degree_text = v.findViewById(R.id.studentProfile_degree);
        post_recycler = v.findViewById(R.id.studentProfile_posts);
        seeall_posts = v.findViewById(R.id.studentProfile_seeAll);
        post_title = v.findViewById(R.id.studentProfile_header);
        no_posts_text = v.findViewById(R.id.studentProfile_no_posts_text);
    }

    private void setUpViews(){
        if (student == null) return;
        name_text.setText(student.getName());
        degree_text.setText(student.getDegree());
        if (profile_img_uri != null) Picasso.get().load(profile_img_uri).into(profile_image);
    }

    // Create adapter for recycler (adapter pulls posts from database)
    private void setUpRecycler(){

        // set LayoutManager and Adapter
        List<View> allViews = new ArrayList<>();
        allViews.add(post_recycler);
        allViews.add(seeall_posts);
        allViews.add(post_title);
        adapter = new SquareImageAdapter(student.getId(), student.getUni_domain(), Constant.PROFILE_POST_LIMIT_STUDENT, getContext(), this::onPostClick, allViews, no_posts_text);
        post_recycler.setLayoutManager(new GridLayoutManager(getContext(), Constant.PROFILE_POST_GRID_ROW_COUNT, GridLayoutManager.VERTICAL, false){
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = getWidth() / Constant.PROFILE_POST_GRID_ROW_COUNT;
                return true;
            }
        });
        post_recycler.setAdapter(adapter);
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
        intent.putExtra("title", "Your Posts");
        intent.putExtra("this_user", student);
        intent.putExtra("uni_domain", student.getUni_domain());
        intent.putExtra("author_id", student.getId());
        startActivity(intent);
    }

    // A post in recycler was selected
    public void onPostClick(int position) {
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
