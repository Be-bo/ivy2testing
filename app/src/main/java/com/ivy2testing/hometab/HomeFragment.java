package com.ivy2testing.hometab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ivy2testing.R;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.Utils;


public class HomeFragment extends Fragment implements FeedAdapter.FeedClickListener {






    // MARK: Variables

    private static final String TAG = "CampusFragmentTag";
    private SwipeRefreshLayout refresh_layout;
    private RecyclerView feed_recycler_view;
    private TextView no_posts_text;
    private TextView reached_bottom_text;
    private ProgressBar progress_bar;

    private FeedAdapter campus_adapter;
    private final Context context;
    private View root_view;
    private User this_user;


    // MARK: Base

    public HomeFragment(Context con, User thisUser) {
        context = con;
        if(thisUser != null) this_user = thisUser;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_campus, container, false);
        declareHandles();
        setUpViewModel();
        setUpRecycler();
        setUpRefreshing();
        return root_view;
    }



    private void declareHandles(){
        refresh_layout = root_view.findViewById(R.id.campus_refresh_layout);
        feed_recycler_view = root_view.findViewById(R.id.campus_feed_recyclerview);
        no_posts_text = root_view.findViewById(R.id.campus_no_posts_text);
        reached_bottom_text = root_view.findViewById(R.id.campus_reached_bottom_text);
        progress_bar = root_view.findViewById(R.id.campus_feed_progress_bar);
    }

    private void setUpViewModel(){
        if (getActivity() != null) {
            UserViewModel this_user_vm = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            this_user = this_user_vm.getThis_user().getValue();
            this_user_vm.getThis_user().observe(getActivity(), (User updatedProfile) -> { //listen to realtime user profile changes
                if(updatedProfile != null) this_user = updatedProfile;
            });
        }
    }









    // MARK: Adapter Stuff

    private void setUpRecycler() {
        campus_adapter = new FeedAdapter(this, Utils.getCampusUni(context), context, no_posts_text, reached_bottom_text, feed_recycler_view, progress_bar);
        feed_recycler_view.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        feed_recycler_view.setAdapter(campus_adapter);
    }

    private void setUpRefreshing() { // this layout handles the swipe down to refresh mechanism
        refresh_layout.setOnRefreshListener(() -> {
            campus_adapter.refreshPosts();
            new Handler().postDelayed(() -> refresh_layout.setRefreshing(false), 2000);
        });
    }

    public void refreshAdapter(){
        if (campus_adapter == null) setUpRecycler();
        campus_adapter.refreshPosts();
    }

    public void changeUni(){
        setUpRecycler();
    }

















    // MARK: Post Interaction

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onFeedClick(int position, int clicked_id) { // Handles clicks on a post item
        Post clickedPost = campus_adapter.getPost_array_list().get(position); //<- this is the clicked event/post
        switch (clicked_id) {
            case R.id.item_feed_author_preview_image:
                if (this_user != null) viewUserProfile(clickedPost.getAuthor_id(), clickedPost.getUni_domain(), clickedPost.getAuthor_is_organization());
                break;
            case R.id.item_feed_pinned_text:
            case R.id.item_feed_pin_icon:
                viewPost(clickedPost.getUni_domain(), clickedPost.getPinned_id(), clickedPost.getAuthor_id()); //can only pin events to posts on that campus -> same uni
                break;
            default:
                viewPost(clickedPost.getUni_domain(), clickedPost.getId(), clickedPost.getAuthor_id());
                break;
        }
    }

    private void viewPost(String uni, String id, String authorId) { // Transition to a post/event
        Intent intent = new Intent(getActivity(), ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post_id", id);
        intent.putExtra("post_uni", uni);
        intent.putExtra("author_id", authorId);
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