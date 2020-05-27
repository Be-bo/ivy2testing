package com.ivy2testing.userProfile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.MainActivity;
import com.ivy2testing.R;
import com.ivy2testing.authentication.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment {

    // Constants
    private final static String TAG = "UserProfileFragment";

    // Views
    private Context mContext;
    private ImageView mProfileImg;
    private TextView mName;
    private TextView mDegree;
    private RecyclerView mRecyclerView;

    // FireBase

    // Other Variables


    // Constructor
    public UserProfileFragment(Context context){
        mContext = context;
    }


/* Override Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_userprofile, container, false);

        // Initialization Methods
        declareViews(rootView);
        setUpRecycler();

        return rootView;
    }

/* Initialization Methods
***************************************************************************************************/

    // TODO
    private void declareViews(View v){
        mRecyclerView = v.findViewById(R.id.userProfile_posts);
    }

    // TODO
    private void setUpRecycler(){

        // Get list of image ids
        List<Integer> imageIds = new ArrayList<>();
        imageIds.add(R.drawable.test_flower);

        // Adapter
        ImageAdapter adapter = new ImageAdapter(mContext, imageIds);
        GridLayoutManager manager =
                new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);

    }

/* OnClick Methods
***************************************************************************************************/

    // Edit Profile Picture TODO


    // Edit profile TODO
    void editProfile(View v){}

    // See all posts TODO
    void seeAllPosts(View v){}


/* Firebase related Methods
***************************************************************************************************/

/* Transition Methods
***************************************************************************************************/

/* UI related Methods
***************************************************************************************************/

/* Utility Methods
***************************************************************************************************/

    private void toastError(String msg){
        Log.w(TAG, msg);
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
