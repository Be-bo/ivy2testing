package com.ivy2testing.userProfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.ivy2testing.util.adapters.SquarePostAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: Student Profile view fragment
 * Notes:Only used for viewing student's own profile
 */
public class StudentProfileFragment extends Fragment {

    // Constants
    private final static String TAG = "StudentProfileFragmentTag";

    private View root_view;

    // Views
    private ImageView profile_image;
    private TextView name_text;
    private TextView degree_text;
    private RecyclerView post_recycler;
    private TextView post_title;
    private TextView no_posts_text;
    private ProgressBar progress_bar;

    // Firestore
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Student student;
    private SquarePostAdapter adapter;
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
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null && is_set_up) adapter.refreshAdapter(); //each time the user comes back we have to refresh the adapter in case they edited or posted a post
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
                    Log.d(TAG, "updated: "+student.isIs_private());
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
        post_title = v.findViewById(R.id.studentProfile_header);
        no_posts_text = v.findViewById(R.id.studentProfile_no_posts_text);
        progress_bar = v.findViewById(R.id.studentProfile_progress_bar);
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
        allViews.add(post_title);
        adapter = new SquarePostAdapter(student.getId(), student.getUni_domain(), Constant.PROFILE_POST_LIMIT_STUDENT, getContext(), this::onPostClick, allViews, no_posts_text, post_recycler, progress_bar);
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
    }



/* OnClick Methods
***************************************************************************************************/

    // Edit profile
    private void editProfile(){
        Intent intent = new Intent(getActivity(), EditStudentProfileActivity.class);
        intent.putExtra("student", student);
        startActivity(intent);
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
        intent.putExtra("post_uni", adapter.getItem(position).getUni_domain());
        intent.putExtra("post_id", adapter.getItem(position).getId());
        startActivity(intent);
    }

/* Firebase Methods
***************************************************************************************************/

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getStudentPic() {
        if (student == null) return;

        base_storage_ref.child(ImageUtils.getUserImagePath(student.getId())).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) profile_img_uri = task.getResult();
                    else Log.w(TAG, task.getException());

                    // Reload views
                    setUpViews();
                });
    }
}
