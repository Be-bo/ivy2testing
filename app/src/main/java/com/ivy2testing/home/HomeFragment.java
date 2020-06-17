package com.ivy2testing.home;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.UserViewModel;

import java.util.ArrayList;
import java.util.Map;

public class HomeFragment extends Fragment implements FeedAdapter.FeedViewHolder.FeedClickListener,BubbleAdapter.BubbleViewHolder.BubbleClickListener {

    //Constants
    private static final String TAG = "HomeFragment";

    // Parent activity
    private Context mContext;
    private View rootView;

    // firebase
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();

    // User View Model
    private Student student;
    private boolean is_organization = false;
    private UserViewModel this_user_viewmodel;


    // main feed
    private final ArrayList<Post> post_arraylist = new ArrayList<Post>();

    private RecyclerView feed_recycler_view;
    private RecyclerView.Adapter feed_adapter;
    private RecyclerView.LayoutManager feed_layout_manager;



    // bubbles
    private final ArrayList<String> bubble_arraylist = new ArrayList<String>();

    private RecyclerView bubble_recycler_view;
    private RecyclerView.Adapter bubble_adapter;
    private RecyclerView.LayoutManager bubble_layout_manager;


    // Constructor
    public HomeFragment(Context con) {
        mContext = con;
    }




    // MARK: Get User Data This Way - always stays update and doesn't require passing anything because ViewModel is connected to the Activity that manages the fragment
    private void getUserProfile(View rootView){
        if (getActivity() != null) {
            this_user_viewmodel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            User usr = this_user_viewmodel.getThis_user().getValue();
            if(usr instanceof Student){
                // TODO: only start doing processes that depend on user profile here:
                // TODO: populate UI
                // TODO: set up listeners
                // TODO: etc.
                // NOTE: everything depends on the user profile data, only execute stuff dependent on it once you 100% have it
            }else if(usr instanceof Organization){
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





        // bubbles recycler view


        bubble_arraylist.add("University");
        bubble_arraylist.add("Events");
        bubble_arraylist.add("Posts");
        bubble_arraylist.add("For You");
        bubble_arraylist.add("Clubs");
        bubble_arraylist.add("University of Calgary Ski and Board Club");
        bubble_arraylist.add("Social");
        bubble_arraylist.add("Grind");

        bubble_recycler_view = rootView.findViewById(R.id.bubble_recycler_view);
        bubble_recycler_view.setHasFixedSize(true);


        bubble_layout_manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        bubble_adapter = new BubbleAdapter(bubble_arraylist, this);
        bubble_recycler_view.setLayoutManager(bubble_layout_manager);
        bubble_recycler_view.setAdapter(bubble_adapter);




        // feed recycler view
        BuildArrayList();

        feed_recycler_view = rootView.findViewById(R.id.feed_recycler_view);
        feed_recycler_view.setHasFixedSize(true);
   /*     //TODO not sure required context here
        feed_layout_manager = new LinearLayoutManager(getContext());
        feed_adapter = new FeedAdapter(post_arraylist, this);
        feed_recycler_view.setLayoutManager(feed_layout_manager);
        feed_recycler_view.setAdapter(feed_adapter);
*/

        feed_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
               // recyclerView.
                if (!recyclerView.canScrollVertically(1)) {
                   // Toast.makeText(getContext(), "Last" + recyclerView.computeVerticalScrollExtent(), Toast.LENGTH_LONG).show();
                   // PullMorePosts();
                }
            }
        });



        return rootView;
    }

    private void BuildArrayList(){
        db_reference.collection("universities").document("ucalgary.ca").collection("posts")
                .limit(10)
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
    }

    private void PullMorePosts(){
        db_reference.collection("universities").document("ucalgary.ca").collection("posts")
                .limit(10)
                .orderBy("creation_millis", Query.Direction.DESCENDING)
               // .startAt(post_arraylist.get(post_arraylist.size()-1).getCreation_millis() +1 )
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
                                Toast.makeText(mContext, ""+post_arraylist.size(), Toast.LENGTH_SHORT).show();
                                feed_adapter.notifyDataSetChanged();
                            }
                        }
                        else {
                            //  Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private void buildEventFeed(){

        Toast.makeText(mContext, ""+post_arraylist.size(), Toast.LENGTH_SHORT).show();
        feed_layout_manager = new LinearLayoutManager(getContext());
        feed_adapter = new FeedAdapter(post_arraylist, this);
        feed_recycler_view.setLayoutManager(feed_layout_manager);
        feed_recycler_view.setAdapter(feed_adapter);
    }



    @Override
    public void onFeedClick(int position) {
        Toast.makeText(mContext, ""+ post_arraylist.get(position).getAuthor_name(), Toast.LENGTH_SHORT).show();

        // TODO THIS IS WHERE TO NAVIGATE TO NEW ACTIVITY
        // post_array_list.get(position); <- this is the clicked event/post
    }

    @Override
    public void onBubbleClick(int position) {
       // Toast.makeText(mContext, ""+bubble_arraylist.get(position), Toast.LENGTH_SHORT).show();
        post_arraylist.clear();
        feed_adapter.notifyDataSetChanged();
        if(bubble_arraylist.get(position).equals("Posts")){
           BubbleQuery(false);
        }
        else if(bubble_arraylist.get(position).equals("Events")){
            BubbleQuery(true);

        }
        else if(bubble_arraylist.get(position).equals("Univeristy")){
            PullMorePosts();
        }

        //BubbleQuery();


        //TODO THIS IS WHERE TO HANDLE BUBBLE CLICKS

        // bubble_arraylist.get(position); <- this is the clicked bubble
    }

    private void BubbleQuery(Boolean event){


        db_reference.collection("universities").document("ucalgary.ca").collection("posts")
                .limit(10)
                .whereEqualTo("is_event",event)
                //TODO if using the order by in this area, it requires an index to be set manually, or with (start from)
                //.orderBy("creation_millis", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(mContext, ""+ post_arraylist.size(), Toast.LENGTH_SHORT).show();
                            if(task.getResult()!=null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Toast.makeText(MainActivity.this, document.getId() + " => " + document.getData(), Toast.LENGTH_SHORT).show();
                                    // Log.d(TAG, "onComplete: " + document.getData().toString());
                                    //TODO THESE ARE SAVED AS EVENTS
                                    Post event_object = document.toObject(Event.class);
                                    post_arraylist.add(event_object);
                                   // Toast.makeText(mContext, ""+ post_arraylist.size(), Toast.LENGTH_SHORT).show();
                                }


                                feed_adapter.notifyDataSetChanged();
                            }
                        }
                        else {
                              Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}