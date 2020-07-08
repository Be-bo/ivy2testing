//package com.ivy2testing.bubbletabs;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.Query;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//import com.google.firebase.firestore.QuerySnapshot;
//import com.ivy2testing.R;
//import com.ivy2testing.entities.Event;
//import com.ivy2testing.entities.Post;
//import com.ivy2testing.entities.User;
//import com.ivy2testing.home.FeedAdapter;
//import com.ivy2testing.home.ViewPostOrEventActivity;
//import com.ivy2testing.userProfile.OrganizationProfileActivity;
//import com.ivy2testing.userProfile.StudentProfileActivity;
//
//import java.util.ArrayList;
//
//public class PostsFragment extends Fragment implements FeedAdapter.FeedClickListener {
//
//    // Parent activity
//    private Context mContext;
//    private View rootView;
//
//    private User this_user;
//    private SwipeRefreshLayout refresh_layout;
//    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
//
//    // feed recyclerview
//    private final ArrayList<Post> post_arraylist = new ArrayList<Post>();
//    private RecyclerView feed_recycler_view;
//    private RecyclerView.Adapter feed_adapter;
//    private LinearLayoutManager feed_layout_manager;
//
//    // onScroll
//    private boolean array_list_updated;
//    private boolean bottom_of_db = false;
//    private TextView no_more_items_text;
//    private ProgressBar loading_progress_bar;
//
//
//    public PostsFragment(Context con) {
//        mContext = con;
//    }
//
//    // Get currently logged in user for transitions to other activities
//    public void setThisUser(User user) {
//        this_user = user;
//    }
//
//    // This entire fragment is basically home fragment with modified querys
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        rootView = inflater.inflate(R.layout.fragment_campus, container, false);
//        refreshLayoutSetup();
//
//
//        if (post_arraylist.size() == 0)
//            initializeFeedView();
//        else
//            resumeFeedView();
//
//        setRecyclerViewListener();
//
//
//        return rootView;
//    }
//
//    private void refreshLayoutSetup() {
//        refresh_layout = rootView.findViewById(R.id.campus_refresh_layout);
//
//
//        refresh_layout.setOnRefreshListener(() -> {
//            new Handler().postDelayed(() -> {
//
//                refresh_layout.setRefreshing(false);
//            }, 2000);
//
//
//            no_more_items_text.setVisibility(View.GONE);
//
//            post_arraylist.clear();
//            feed_adapter.notifyItemRangeInserted(0, post_arraylist.size());
//            feed_adapter.notifyDataSetChanged();
//            array_list_updated = false;
//            BuildArrayList();
//            bottom_of_db = false;
//        });
//    }
//
//    private void initializeFeedView() {
//        feed_recycler_view = rootView.findViewById(R.id.campus_feed_recyclerview);
//        feed_recycler_view.setHasFixedSize(true);
//        BuildArrayList();
//    }
//
//    private void resumeFeedView() {
//        feed_recycler_view = rootView.findViewById(R.id.campus_feed_recyclerview);
//        feed_recycler_view.setHasFixedSize(true);
//        feed_layout_manager = new LinearLayoutManager(getContext());
//        feed_recycler_view.setLayoutManager(feed_layout_manager);
//        feed_recycler_view.setAdapter(feed_adapter);
//    }
//
//    private void setRecyclerViewListener() {
//        no_more_items_text = rootView.findViewById(R.id.campus_no_more_items_text);
//        loading_progress_bar = rootView.findViewById(R.id.campus_more_items_progress_bar);
//
//
//        feed_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (array_list_updated) {
//
//                    if (feed_layout_manager.findLastVisibleItemPosition() > (post_arraylist.size() - 4)) {
//
//                        if (!bottom_of_db) {
//                            array_list_updated = false;
//                            pullMorePosts();
//                        }
//
//                    }
//                }
//                if (!recyclerView.canScrollVertically(1)) {
//                    if (bottom_of_db)
//                        no_more_items_text.setVisibility(View.VISIBLE);
//                    else
//                        loading_progress_bar.setVisibility(View.VISIBLE);
//
//                } else {
//                    loading_progress_bar.setVisibility(View.GONE);
//                    no_more_items_text.setVisibility(View.GONE);
//                }
//            }
//        });
//    }
//
//    private void BuildArrayList() {
//        db_reference.collection("universities").document(this_user.getUni_domain()).collection("posts")
//                .limit(15)
//                .whereEqualTo("is_event", false)
//                .whereEqualTo("main_feed_visible", true)
//                .orderBy("creation_millis", Query.Direction.DESCENDING)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            if (task.getResult() != null) {
//                                for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                    Post event_object = document.toObject(Event.class);
//                                    post_arraylist.add(event_object);
//                                }
//
//                                buildEventFeed();
//                            }
//                        } else {
//                            //  Log.d(TAG, "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
//        array_list_updated = true;
//    }
//
//    private void pullMorePosts() {
//        int start = post_arraylist.size();
//        if (post_arraylist.size() != 0) {
//            db_reference.collection("universities").document(this_user.getUni_domain()).collection("posts")
//                    .limit(15)
//                    .whereEqualTo("is_event", false)
//                    .whereEqualTo("main_feed_visible", true)
//                    .orderBy("creation_millis", Query.Direction.DESCENDING)
//                    .startAfter(post_arraylist.get(post_arraylist.size() - 1).getCreation_millis())
//                    .get()
//                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                            if (task.isSuccessful()) {
//                                if (task.getResult() != null) {
//                                    for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                        Post event_object = document.toObject(Event.class);
//                                        post_arraylist.add(event_object);
//                                    }
//                                    loading_progress_bar.setVisibility(View.GONE);
//                                    feed_adapter.notifyItemRangeInserted(start, post_arraylist.size());
//                                    array_list_updated = true;
//
//
//                                    if (task.getResult().size() == 0) {
//                                        bottom_of_db = true;
//                                    }
//                                }
//
//                            } else {
//                                //Log.d(TAG, "onComplete: No More events");
//                                //  Log.d(TAG, "Error getting documents: ", task.getException());
//                            }
//                        }
//                    });
//        } else {
//            //TODO ERROR HERE
//
//            array_list_updated = true;
//        }
//    }
//
//    private void buildEventFeed() {
//
//
//        feed_layout_manager = new LinearLayoutManager(getContext());
//        feed_adapter = new FeedAdapter(post_arraylist, this);
//        feed_recycler_view.setLayoutManager(feed_layout_manager);
//        feed_recycler_view.setAdapter(feed_adapter);
//    }
//
//    // Handles clicks on a post item
//    @Override
//    public void onFeedClick(int position, int clicked_id) {
//
//        Post clickedPost = post_arraylist.get(position); //<- this is the clicked event/post
//
//        switch (clicked_id) {
//            case R.id.item_feed_full_text_button:
//                viewPost(clickedPost);
//                break;
//
//            case R.id.item_feed_posted_by_text:
//                viewUserProfile(clickedPost.getAuthor_id(), clickedPost.getUni_domain(), clickedPost.getAuthor_is_organization());
//                break;
//
//            case R.id.item_feed_pinned_text:
//                loadPostFromDB(clickedPost.getPinned_id(), clickedPost.getUni_domain());
//                break;
//        }
//    }
//
//    // Transition to a post/event
//    private void viewPost(Post post) {
//        Log.d("PostsFragment", "Launching ViewPostOrEventActivity...");
//        Intent intent = new Intent(getActivity(), ViewPostOrEventActivity.class);
//        intent.putExtra("this_user", this_user);
//        intent.putExtra("post", post);
//        startActivity(intent);
//    }
//
//    // Transition to a user profile
//    private void viewUserProfile(String user_id, String user_uni, boolean is_organization) {
//        if (user_id == null || user_uni == null) {
//            Log.e("PostsFragment", "User not properly defined! Cannot view author's profile");
//            return;
//        } // author field wasn't defined
//
//
//        if (this_user != null && user_id.equals(this_user.getId())) {
//            Log.d("PostsFragment", "Viewer is author. Might want to change behaviour.");
//        } // Do nothing if viewer == author
//
//
//        Intent intent;
//        if (is_organization) {
//            Log.d("PostsFragment", "Starting OrganizationProfile Activity for organization " + user_id);
//            intent = new Intent(getActivity(), OrganizationProfileActivity.class);
//            intent.putExtra("org_to_display_id", user_id);
//            intent.putExtra("org_to_display_uni", user_uni);
//        } else {
//            Log.d("PostsFragment", "Starting StudentProfile Activity for student " + user_id);
//            intent = new Intent(getActivity(), StudentProfileActivity.class);
//            intent.putExtra("student_to_display_id", user_id);
//            intent.putExtra("student_to_display_uni", user_uni);
//        }
//        intent.putExtra("this_user", this_user);
//        startActivity(intent);
//    }
//
//    // Load a post from database
//    private void loadPostFromDB(String post_id, String post_uni_domain) {
//        String address = "universities/" + post_uni_domain + "/posts/" + post_id;
//        if (address.contains("null")) {
//            Log.e("PostsFragment", "Address has null values.");
//            return;
//        }
//
//        db_reference.document(address).get().addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                Post post;
//                if ((boolean) task.getResult().get("is_event"))
//                    post = task.getResult().toObject(Event.class);
//                else post = task.getResult().toObject(Post.class);
//                if (post != null) {
//                    post.setId(post_id);
//                    viewPost(post);
//                } else Log.e("PostsFragment", "Post retrieved was null.");
//            } else
//                Log.e("PostsFragment", "Something went wrong when retrieving Post.", task.getException());
//        });
//    }
//
//
//    public static PostsFragment newInstance(Context con) {
//        PostsFragment pf = new PostsFragment(con);
//
//
//        return pf;
//    }
//}
