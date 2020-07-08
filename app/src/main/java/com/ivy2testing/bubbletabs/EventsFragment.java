package com.ivy2testing.bubbletabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ivy2testing.R;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.FeedAdapter;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.Constant;

import static android.content.Context.MODE_PRIVATE;

public class EventsFragment extends Fragment implements FeedAdapter.FeedClickListener {


    private Context context;
    private View root_view;
    private SwipeRefreshLayout refresh_layout;
    private TextView reached_bottom_text;

    private String campus_domain;
    private RecyclerView feed_recycler_view;
    private User this_user;
    private FeedAdapter feed_adapter;
    private TextView no_events_text;

    private Button happening_now_button;
    private Button starting_soon_button;
    private Button past_events_button;

    private CardView featured_cardview;
    private TextView featured_title;
    private TextView featured_text;
    private TextView featured_author;
    private TextView featured_pinned;















    // MARK: Base & Setup

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_events, container, false);
        declareHandles();
        setUpRecycler();
        refreshLayoutSetup();
        return root_view;
    }

    private void declareHandles(){
        refresh_layout = root_view.findViewById(R.id.events_refresh_layout);
        feed_recycler_view = root_view.findViewById(R.id.events_feed_recycler_view);
        no_events_text = root_view.findViewById(R.id.events_no_events_text);
        reached_bottom_text = root_view.findViewById(R.id.events_reached_bottom_text);

        featured_cardview = root_view.findViewById(R.id.item_feed_cardview);
        featured_title = featured_cardview.findViewById(R.id.item_feed_event_title);
        featured_title.setVisibility(View.VISIBLE);
        featured_text = featured_cardview.findViewById(R.id.item_feed_text);
        featured_author = featured_cardview.findViewById(R.id.item_feed_posted_by_text);
        featured_pinned = featured_cardview.findViewById(R.id.item_feed_pinned_text);

        featured_title.setText("Want your event featured on Ivy?");
        featured_text.setText("It's totally possible, click on this event to find out how!");
        featured_author.setText("This could be you!");
        featured_pinned.setText("last pinned event");
    }

    public EventsFragment(Context con, User thisUser) {
        context = con;
        loadUni();
        if(thisUser != null) this_user = thisUser;
    }

    private void loadUni(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences", MODE_PRIVATE);
        campus_domain = sharedPreferences.getString("campus_domain", "ucalgary.ca");
    }

    private void refreshLayoutSetup() {
        refresh_layout.setOnRefreshListener(() -> {
            feed_adapter.refreshPosts();
            new Handler().postDelayed(() -> { refresh_layout.setRefreshing(false); }, 2000);
        });
    }

    private void setUpRecycler(){
        feed_adapter = new FeedAdapter(this, Constant.FEED_ADAPTER_EVENTS, campus_domain, "", context, no_events_text, reached_bottom_text);
        feed_recycler_view.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        feed_recycler_view.setAdapter(feed_adapter);
    }

    public void refreshAdapter(){
        feed_adapter.refreshPosts();
    }


//    private void setUpButtons() {
//        happening_now_button = root_view.findViewById(R.id.happening_now_button);
//        happening_now_button.setOnClickListener(v -> {
//            clearEnabled(happening_now_button);
//            current_arraylist = happening_arraylist;
//            search_method = HAPPENING_NOW;
//            if (happening_arraylist.size()!= 0){
//                feed_recycler_view.swapAdapter(happening_adapter,true);
//            }
//            else {
//                switchQuery();
//            }
//        });
//
//        past_events_button = root_view.findViewById(R.id.past_events_button);
//        past_events_button.setOnClickListener(v -> {
//            current_arraylist = past_arraylist;
//            search_method = PAST_EVENTS;
//            clearEnabled(past_events_button);
//            if (past_arraylist.size()!= 0){
//                feed_recycler_view.swapAdapter(past_adapter,true);
//            }
//            else {
//                switchQuery();
//            }
//        });
//
//        starting_soon_button = root_view.findViewById(R.id.upcoming_button);
//        starting_soon_button.setOnClickListener(v -> {
//            search_method = UPCOMING;
//            current_arraylist = upcoming_arraylist;
//            clearEnabled(starting_soon_button);
//            if (upcoming_arraylist.size()!= 0){
//                feed_recycler_view.swapAdapter(upcoming_adapter,true);
//            }
//            else {
//                switchQuery();
//            }
//        });
//    }










    // MARK: Event Interaction

    @Override
    public void onFeedClick(int position, int clicked_id) {     // Handles clicks on a post item

        Post clickedPost = feed_adapter.getPost_array_list().get(position); //<- this is the clicked event/post

        switch (clicked_id) {
            case R.id.item_feed_full_text_button:
                viewPost(clickedPost);
                break;

            case R.id.item_feed_posted_by_text:
                viewUserProfile(clickedPost.getAuthor_id(), clickedPost.getUni_domain(), clickedPost.getAuthor_is_organization());
                break;

            case R.id.item_feed_pinned_text:
                //TODO
                break;
        }
    }

    private void viewPost(Post post) { // Transition to a post/event
        Intent intent = new Intent(getActivity(), ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    private void viewUserProfile(String user_id, String user_uni, boolean is_organization){ // Transition to a user profile
        if (user_id == null || user_uni == null){
            Log.e("EventsFragment", "User not properly defined! Cannot view author's profile");
            return;
        } // author field wasn't defined


        if (this_user != null && user_id.equals(this_user.getId())) {
            Log.d("EventsFragment", "Viewer is author. Might want to change behaviour.");
        } // Do nothing if viewer == author


        Intent intent;
        if (is_organization){
            intent = new Intent(getActivity(), OrganizationProfileActivity.class);
            intent.putExtra("org_to_display_id", user_id);
            intent.putExtra("org_to_display_uni", user_uni);
        }
        else {
            intent = new Intent(getActivity(), StudentProfileActivity.class);
            intent.putExtra("student_to_display_id", user_id);
            intent.putExtra("student_to_display_uni", user_uni);
        }
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }
}
