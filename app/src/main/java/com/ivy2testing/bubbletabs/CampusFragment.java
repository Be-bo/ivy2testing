package com.ivy2testing.bubbletabs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.FeedAdapter;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.Constant;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

// Author Clyde.
// Home Fragment will be the default format for any feed based Class, The methods can be kept the same
// and the buildArrayList and pullMorePosts can be modified with custom queries create any type of feed.

public class CampusFragment extends Fragment implements FeedAdapter.FeedClickListener {




    private static final String TAG = "CampusFragmentTag";
    private SwipeRefreshLayout refresh_layout;
    private RecyclerView feed_recycler_view;
    private TextView no_posts_text;
    private TextView reached_bottom_text;

    private FeedAdapter campus_adapter;
    private Context context;
    private View root_view;
    private User this_user;
    private String campus_domain;









    //MARK: Base & Setup

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_campus, container, false);
        declareHandles();
        setUpRecycler();
        setUpRefreshing();
        return root_view;
    }

    public CampusFragment(Context con, User thisUser) {
        context = con;
        loadUni();
        if(thisUser != null) this_user = thisUser;
    }

    private void loadUni(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences", MODE_PRIVATE);
        campus_domain = sharedPreferences.getString("campus_domain", "ucalgary.ca");
    }

    private void declareHandles(){
        refresh_layout = root_view.findViewById(R.id.campus_refresh_layout);
        feed_recycler_view = root_view.findViewById(R.id.campus_feed_recyclerview);
        no_posts_text = root_view.findViewById(R.id.campus_no_posts_text);
        reached_bottom_text = root_view.findViewById(R.id.campus_reached_bottom_text);
    }

    private void setUpRecycler() {
        campus_adapter = new FeedAdapter(this, Constant.FEED_ADAPTER_CAMPUS, campus_domain, "", getContext(), no_posts_text, reached_bottom_text);
        feed_recycler_view.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        feed_recycler_view.setAdapter(campus_adapter);
    }

    private void setUpRefreshing() { // this layout handles the swipe down to refresh mechanism
        refresh_layout.setOnRefreshListener(() -> {
            campus_adapter.refreshPosts();
            new Handler().postDelayed(() -> { refresh_layout.setRefreshing(false); }, 2000);
        });
    }

    public void refreshAdapter(){
        campus_adapter.refreshPosts();
    }

















    // MARK: Post Interaction

    @Override
    public void onFeedClick(int position, int clicked_id) { // Handles clicks on a post item
        Post clickedPost = campus_adapter.getPost_array_list().get(position); //<- this is the clicked event/post
        switch (clicked_id) {
            case R.id.item_feed_full_text_button:
                viewPost(clickedPost);
                break;
            case R.id.item_feed_posted_by_text:
                viewUserProfile(clickedPost.getAuthor_id(), clickedPost.getUni_domain(), clickedPost.getAuthor_is_organization());
                break;
            case R.id.item_feed_pinned_text:
                //TODO: transition to the post
                break;
        }
    }

    private void viewPost(Post post) { // Transition to a post/event
        Intent intent = new Intent(getActivity(), ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    private void viewUserProfile(String user_id, String user_uni, boolean is_organization) { // Transition to a user profile
        if (user_id == null || user_uni == null) {
            Log.e("HomeFragment", "User not properly defined! Cannot view author's profile");
            return;
        } // author field wasn't defined

        if (this_user != null && user_id.equals(this_user.getId())) Log.d("HomeFragment", "Viewer is author. Might want to change behaviour."); // Do nothing if viewer == author

        Intent intent;
        if (is_organization) {
            intent = new Intent(getActivity(), OrganizationProfileActivity.class);
            intent.putExtra("org_to_display_id", user_id);
            intent.putExtra("org_to_display_uni", user_uni);
        } else {
            intent = new Intent(getActivity(), StudentProfileActivity.class);
            intent.putExtra("student_to_display_id", user_id);
            intent.putExtra("student_to_display_uni", user_uni);
        }
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }
}