package com.ivy2testing.quadtab;

import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.Utils;
import com.squareup.picasso.Picasso;

/**
 * @author Shanna Hollingworth
 * Overview: Quad view fragment
 */

public class QuadFragment extends Fragment implements QuadAdapter.QuadClickListener {

    //Constants
    private final static String TAG = "QuadFragmentTag";
    private View root_view;

    //Views
    private RecyclerView card_recycler;
    private TextView no_users_text;
    private QuadAdapter quad_adapter;

    // Firestore
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Student student;
    private ProgressBar progressbar;
    private boolean is_set_up = false;

    public boolean isIs_set_up() {
        return is_set_up;
    }

    /* Override Methods
     ***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_quad, container, false);
        declareViews(root_view);
        return root_view;
    }

    public void setUpQuad(){
        is_set_up = true;
        if (student == null) getUserProfile();
        else setUpElements();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onStart() {
        super.onStart();
        if(quad_adapter!=null && is_set_up) quad_adapter.refreshAdapter(); //each time the user comes back we have to refresh the adapter in case a new user has been added
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
        }
    }

    private void setUpElements(){
        Log.d(TAG, "Showing student: " + student.getId() + ", name: " + student.getName());
        setUpRecycler();            // set up posts recycler view
        //setListeners(root_view);    // set up listeners
    }

    private void declareViews(View v) {
        card_recycler = v.findViewById(R.id.student_card_recyclerview);
        no_users_text = v.findViewById(R.id.quad_no_users_text);
        progressbar = v.findViewById(R.id.student_card_progress_bar);
    }


//    // Set up onClick Listeners
//    private void setListeners(View v) {
//        v.findViewById(R.id.chatButton).setOnClickListener(v12 -> editProfile());
//    }

    private void setUpRecycler() {
        UserViewModel user_view_model = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        User usr = user_view_model.getThis_user().getValue();
        quad_adapter = new QuadAdapter(this, Constant.USERS_LOAD_LIMIT, student.getUni_domain(), getContext(), no_users_text, usr, card_recycler, progressbar);
        card_recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        card_recycler.setAdapter(quad_adapter);
    }

    /* Firebase Methods
     ***************************************************************************************************/

    @Override
    public void onQuadClick(int position, int clicked_id) {

    }
}
