package com.ivy2testing.userProfile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.entities.OnSelectionListener;
import com.ivy2testing.R;
import com.ivy2testing.entities.Student;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: Student Profile view fragment
 * Notes: Image view not implemented to show student's img yet, Recycler items currently hard-coded
 */
public class StudentProfileFragment extends Fragment {

    // Constants
    private final static String TAG = "StudentProfileFragment";

    // Views
    private Context mContext;
    private ImageView mProfileImg;
    private TextView mName;
    private TextView mDegree;
    private RecyclerView mRecyclerView;

    // Other Variables
    private Student student;
    private ImageAdapter adapter;


    // Constructor
    public StudentProfileFragment(Context context, Student student) {
        mContext = context;
        this.student = student;
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

/* Initialization Methods
***************************************************************************************************/

    private void declareViews(View v){
        mProfileImg = v.findViewById(R.id.userProfile_circleImg);
        mName = v.findViewById(R.id.userProfile_name);
        mDegree = v.findViewById(R.id.userProfile_degree);
        mRecyclerView = v.findViewById(R.id.userProfile_posts);
    }

    // TODO image
    private void setupViews(){
        mName.setText(student.getName());
        mDegree.setText(student.getDegree());
    }

    // TODO set up with Post objects later
    private void setUpRecycler(){

        // Get list of image ids
        List<Integer> imageIds = new ArrayList<>();
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);

        // set LayoutManager and Adapter
        adapter = new ImageAdapter(imageIds);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(adapter);
    }

    // Set up onClick Listeners
    private void setListeners(View v){
        v.findViewById(R.id.userProfile_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {editProfile();}
        });
        v.findViewById(R.id.userProfile_seeAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {seeAllPosts();}
        });
        adapter.setOnSelectionListener(new OnSelectionListener() {
            @Override
            public void onSelectionClick(int position) {selectPost();}
        });
    }

/* OnClick Methods
***************************************************************************************************/

    // Edit profile TODO
    private void editProfile(){
        Intent intent = new Intent(getActivity(), EditStudentProfileActivity.class);
        Log.d(TAG, "domain: " + student.getUni_domain() + ", id: " + student.getId());
        intent.putExtra("this_uni_domain",student.getUni_domain());
        intent.putExtra("this_user_id", student.getId());
        startActivity(intent);
    }

    // See all posts TODO
    private void seeAllPosts(){}

    // A post in recycler was selected  TODO
    private void selectPost() {
        Toast.makeText(mContext,"I was clicked here! ", Toast.LENGTH_SHORT).show();
    }


/* Utility Methods
***************************************************************************************************/

    private void toastError(String msg){
        Log.w(TAG, msg);
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
