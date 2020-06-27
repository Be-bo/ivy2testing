package com.ivy2testing.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements FeedAdapter.FeedViewHolder.FeedClickListener {

    //Constants
    private static final String TAG = "HomeFragment";

    private SwipeRefreshLayout refresh_layout;

    // Parent activity
    private Context mContext;
    private View rootView;

    // firebase
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();


    // User View Model
    private User this_user;
    private boolean is_organization = false;
    private UserViewModel this_user_viewmodel;




    // main feed
    private final ArrayList<Post> post_arraylist = new ArrayList<Post>();

    private RecyclerView feed_recycler_view;
    private RecyclerView.Adapter feed_adapter;
    private LinearLayoutManager feed_layout_manager;





    private boolean array_list_updated = false;
    private boolean bottom_of_db;

    //
    private TextView no_more_items_text;
    private TextView loading_progress_bar;



    // Constructor
    public HomeFragment(Context con) {
        mContext = con;
    }






    // MARK: Get User Data This Way - always stays update and doesn't require passing anything because ViewModel is connected to the Activity that manages the fragment
    private void getUserProfile(View rootView){
        if (getActivity() != null) {
            this_user_viewmodel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            this_user = this_user_viewmodel.getThis_user().getValue();
            if(this_user instanceof Student){
                // TODO: only start doing processes that depend on user profile here:
                // TODO: populate UI
                // TODO: set up listeners
                // TODO: etc.
                // NOTE: everything depends on the user profile data, only execute stuff dependent on it once you 100% have it
            }else if(this_user instanceof Organization){
                //TODO: -||-
            }

            this_user_viewmodel.getThis_user().observe(getActivity(), (User updatedProfile) -> { //listen to realtime user profile changes afterwards
                if(updatedProfile instanceof Student){
                    // TODO: if stuff needs to be updated whenever the user profile receives an update, DO SO HERE
                }else if(updatedProfile instanceof Organization){
                    // TODO: if stuff needs to be updated whenever the user profile receives an update, DO SO HERE
                }
            });
        }
    }
    // MARK: ------------------------------------------------------------------------------------------------------------------------------------------------------------




    /* Overridden Methods
     ***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        getUserProfile(rootView); // TODO: @Clyde may want to move this but "this_user" is needed for the onClicks
        refreshLayoutSetup();

        // feed recycler view
        if(post_arraylist.size() == 0) initializeFeedView();

        else resumeFeedView();

        //array_list_updated = true;
       setRecyclerViewListener();

        return rootView;
    }

    private void refreshLayoutSetup(){
        refresh_layout = rootView.findViewById(R.id.feed_swipe_refresh_layout);

        refresh_layout.setOnRefreshListener(()->{
            Toast.makeText(mContext, "REFRESH ME", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(()->{

                refresh_layout.setRefreshing(false);
            },2000);

            post_arraylist.clear();
            feed_adapter.notifyItemRangeInserted(0,post_arraylist.size());
            feed_adapter.notifyDataSetChanged();
            array_list_updated = false;
            BuildArrayList();
            bottom_of_db = false;
        });
    }

    private void initializeFeedView(){
        feed_recycler_view = rootView.findViewById(R.id.feed_recycler_view);
        feed_recycler_view.setHasFixedSize(true);
        BuildArrayList();
    }

    private void resumeFeedView(){
        feed_recycler_view = rootView.findViewById(R.id.feed_recycler_view);
        feed_recycler_view.setHasFixedSize(true);
        feed_layout_manager = new LinearLayoutManager(getContext());
        feed_recycler_view.setLayoutManager(feed_layout_manager);
        feed_recycler_view.setAdapter(feed_adapter);

    }

    private void setRecyclerViewListener(){
        no_more_items_text = rootView.findViewById(R.id.no_more_items_text);
        loading_progress_bar = rootView.findViewById(R.id.loading_more_items_text);


        feed_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(array_list_updated) {

                    if (feed_layout_manager.findLastVisibleItemPosition() > (post_arraylist.size() - 4 )){
                       // Toast.makeText(mContext, "7", Toast.LENGTH_SHORT).show();
                        if(!bottom_of_db) {
                            Log.d(TAG, "onScrolled: SEVEN");
                            array_list_updated = false;
                            pullMorePosts();
                        }
                    }
                    //TODO here our outside of the above if
                }
                if (!recyclerView.canScrollVertically(1)) {
                    if (bottom_of_db)
                        no_more_items_text.setVisibility(View.VISIBLE);
                    else
                        loading_progress_bar.setVisibility(View.VISIBLE);

                }
                else{
                    loading_progress_bar.setVisibility(View.GONE);
                    no_more_items_text.setVisibility(View.GONE);
                }


            }
        });
    }

    private void BuildArrayList(){
        db_reference.collection("universities").document("ucalgary.ca").collection("posts")
                .limit(15)
                .whereEqualTo("main_feed_visible",true)
                .orderBy("creation_millis", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult()!=null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Toast.makeText(MainActivity.this, document.getId() + " => " + document.getData(), Toast.LENGTH_SHORT).show();
                                   // Log.d(TAG, "onComplete: " + document.getData().toString());
                                    //TODO THESE ARE SAVED AS EVENTS
                                    Post event_object = document.toObject(Event.class);
                                    post_arraylist.add(event_object);
                                }
                                buildEventFeed();
                            }
                        }
                        else {
                            //  Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        array_list_updated = true;
    }



    // Update Methods

    private void pullMorePosts(){
        int start = post_arraylist.size();
        if(post_arraylist.size() != 0) {
            db_reference.collection("universities").document("ucalgary.ca").collection("posts")
                    .limit(15)
                    .whereEqualTo("main_feed_visible",true)
                    .orderBy("creation_millis", Query.Direction.DESCENDING)
                    //TODO arraylist can get stumbled hup her if it gets deleted while updating
                    .startAfter(post_arraylist.get(post_arraylist.size() - 1).getCreation_millis())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // Toast.makeText(MainActivity.this, document.getId() + " => " + document.getData(), Toast.LENGTH_SHORT).show();
                                        // Log.d(TAG, "onComplete: " + document.getData().toString());
                                        //TODO THESE ARE SAVED AS EVENTS
                                        Post event_object = document.toObject(Event.class);
                                        post_arraylist.add(event_object);

                                    }
                                    loading_progress_bar.setVisibility(View.GONE);
                                    feed_adapter.notifyItemRangeInserted(start, post_arraylist.size());

                                    // notify data set changed is jerky and causes hiccups

                                    // Toast.makeText(mContext, ""+post_arraylist.size(), Toast.LENGTH_SHORT).show();
                                   // feed_adapter.notifyDataSetChanged();
                                    //feed_adapter.notifyItemRangeChanged(0, post_arraylist.size());
                                    array_list_updated = true;


                                    if(task.getResult().size() == 0){
                                        Log.d(TAG, "onComplete: No more items");
                                        bottom_of_db = true;


                                    }

                                }

                            } else {
                                //Log.d(TAG, "onComplete: No More events");
                                //  Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
        else {
            //TODO ERROR HERE
            Log.d(TAG, "PullMorePosts: post_array_list_size" + post_arraylist.size());
            array_list_updated = true;
        }
    }

    private void buildEventFeed(){

        // TODO The linear layout manager is not default recyclerview LLM
       // Toast.makeText(mContext, ""+post_arraylist.size(), Toast.LENGTH_SHORT).show();
        feed_layout_manager = new LinearLayoutManager(getContext());
        feed_adapter = new FeedAdapter(post_arraylist, this);
        feed_recycler_view.setLayoutManager(feed_layout_manager);
        feed_recycler_view.setAdapter(feed_adapter);
    }



    // Handles clicks on a post item
    @Override
    public void onFeedClick(int position, int clicked_id) {

        Post clickedPost = post_arraylist.get(position); //<- this is the clicked event/post

        switch(clicked_id){
            case R.id.full_constraint_view:
                viewPost(clickedPost);
                break;

            case R.id.object_posted_by_author:
                viewUserProfile(clickedPost.getAuthor_id(), clickedPost.getUni_domain(), clickedPost.getAuthor_is_organization());
                break;

            case R.id.object_pinned_event:
                loadPostFromDB(clickedPost.getPinned_id(), clickedPost.getUni_domain());
                break;
        }
    }

    // Transition to a post/event
    private void viewPost(Post post) {
        Log.d("HomeFragment", "Launching ViewPostOrEventActivity...");
        Intent intent = new Intent(getActivity(), ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    // Transition to a user profile
    private void viewUserProfile(String user_id, String user_uni, boolean is_organization){
        if (user_id == null || user_uni == null){
            Log.e("HomeFragment", "User not properly defined! Cannot view author's profile");
            return;
        } // author field wasn't defined


        if (this_user != null && user_id.equals(this_user.getId())) {
            Log.d("HomeFragment", "Viewer is author. Might want to change behaviour.");
        } // Do nothing if viewer == author


        Intent intent;
        if (is_organization){
            Log.d("HomeFragment", "Starting OrganizationProfile Activity for organization " + user_id);
            intent = new Intent(getActivity(), OrganizationProfileActivity.class);
            intent.putExtra("org_to_display_id", user_id);
            intent.putExtra("org_to_display_uni", user_uni);
        }
        else {
            Log.d("HomeFragment", "Starting StudentProfile Activity for student " + user_id);
            intent = new Intent(getActivity(), StudentProfileActivity.class);
            intent.putExtra("student_to_display_id", user_id);
            intent.putExtra("student_to_display_uni", user_uni);
        }
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }

    // Load a post from database
    private void loadPostFromDB(String post_id, String post_uni_domain) {
        String address = "universities/" + post_uni_domain + "/posts/" + post_id;
        if (address.contains("null")){
            Log.e("HomeFragment", "Address has null values.");
            return;
        }

        db_reference.document(address).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Post post;
                if ((boolean)task.getResult().get("is_event")) post = task.getResult().toObject(Event.class);
                else post = task.getResult().toObject(Post.class);
                if (post != null){
                    post.setId(post_id);
                    viewPost(post);
                }
                else Log.e("HomeFragment", "Post retrieved was null.");
            }
            else Log.e("HomeFragment", "Something went wrong when retrieving Post.", task.getException());
        });
    }



    public static HomeFragment newInstance(Context con) {
        HomeFragment hf = new HomeFragment(con);


        return hf;
    }

}