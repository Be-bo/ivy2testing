package com.ivy2testing.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    //Constants
    private static final String TAG = "HomeFragment";

    // Parent activity
    private Context mContext;
    private View rootView;

    private Button uni_button;
    private Button current_button;

    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();

    private Student student;
    private boolean is_organization = false;
    private UserViewModel this_user_viewmodel;


    private final ArrayList<Post> post_arraylist = new ArrayList<Post>();
    private final ArrayList<Event> event_arraylist = new ArrayList<Event>();

    private RecyclerView feed_recycler_view;
    private RecyclerView.Adapter feed_adapter;
    private RecyclerView.LayoutManager feed_layout_manager;

    public HomeFragment(){

        // REQUIRED no argument public constructor
    }

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
        // Initialization
        uni_button = rootView.findViewById(R.id.btn_1);


        current_button = uni_button;
        current_button.setEnabled(false);

        // recycler view

        BuildArrayList();

//        Toast.makeText(mContext, ""+event_arraylist.get(0).getAuthor_name(), Toast.LENGTH_SHORT).show();

        feed_recycler_view = rootView.findViewById(R.id.feed_recycler_view);
        feed_recycler_view.setHasFixedSize(true);
        //TODO not sure required context here
        feed_layout_manager = new LinearLayoutManager(getContext());
        feed_adapter = new FeedAdapter(event_arraylist);
        feed_recycler_view.setLayoutManager(feed_layout_manager);
        feed_recycler_view.setAdapter(feed_adapter);

        return rootView;
    }



    /* ************************************************************************************************** */
    // resets main "Uni" button to be selected/ deselects others,
    // this method is can be called when returning from other activities or back presses

    private void resetMainBubble() {
        current_button.setEnabled(true);
        uni_button.setEnabled(false);
        current_button = uni_button;
    }

    /* ************************************************************************************************** */
    // the buttons are set to enabled to be clickable and disabled  when selected so they can't be constantly clicked
    // they can be set to selected and unselected but it depends on what function they perform when clicked
    // I was struggling to get the buttons to stay clicked so i threw together this enabled solution, and it works,
    // but we can change the states... for now this works

    // all bubbles are also set with an onClick function so they call this function, and locally, whichever button
    // triggers the function, is saved locally and the view is updated. current_button = most recently clicked button, the last button will be re-enabled
    // functions can be placed here to perform a function based off the text/ information in a button i.e a search with current_button.getText

    public void toggleEnabled(View view) {
        current_button.setEnabled(true);
        view.setEnabled(false);
        current_button = (Button) view;
        //  Toast.makeText(this, "" + current_button.getText(), Toast.LENGTH_SHORT).show();

    }

    /* ************************************************************************************************** */
    // this function navigates to the collection containing posts, checks if an item is a post. It then converts the
    // item to a hash map and creates a post in the fragment container view

    private void buildMainFeed() {
        //DocumentReference sampler = db_reference.collection("universities").document("ucalgary.ca").collection("posts").document("5c2fbbf4-c06b-406c-92e9-71321f046d43");
        db_reference.collection("universities").document("ucalgary.ca").collection("posts")
                .whereEqualTo("is_event", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Toast.makeText(MainActivity.this, document.getId() + " => " + document.getData(), Toast.LENGTH_SHORT).show();
                                Map<String, Object> is_post = document.getData();
                                postCreator(is_post);
                            }
                        } else {
                            //  Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /* ************************************************************************************************** */
    //This function takes a hash map and fills a sample post with its information
    //https://stackoverflow.com/questions/6216547/android-dynamically-add-views-into-view

    private void postCreator(Map<String, Object> post) {
        // layout inflater puts views inside other views
        LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.sample_post, null);

        // fill in any details dynamically here
        final ImageView sample_image = v.findViewById(R.id.sample_image);
        //TODO do posts have titles?
        TextView tv = v.findViewById(R.id.sample_name);
        TextView description = v.findViewById(R.id.sample_description);
        TextView author_name = v.findViewById(R.id.sample_author_nam);
        description.setText((String) post.get("text"));
        author_name.setText((String) post.get("author_name"));
        String visual_path = (String) post.get("visual");
        // if the visual path contains a / it will be a link to storage location
        if (visual_path.contains("/")) {
            // max download size = 1.5mb
            db_storage.child(visual_path).getBytes(1500000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                    // bytes is an byte [] returned from storage,
                    // set the image to be visible
                    sample_image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    sample_image.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO error handler
                }
            });
        }


        // insert into main view
        // currently inserts newest post last... needs better queries
       /* ViewGroup insertPoint = rootView.findViewById(R.id.main_fragment_linear_layout);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));*/
    }
    private void BuildArrayList(){
        db_reference.collection("universities").document("testucalgary.ca").collection("posts")
                .whereEqualTo("is_event", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult()!=null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Toast.makeText(MainActivity.this, document.getId() + " => " + document.getData(), Toast.LENGTH_SHORT).show();
                                    post_arraylist.add(document.toObject(Post.class));
                                }
                            }
                        }
                        else {
                            //  Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


        db_reference.collection("universities").document("testucalgary.ca").collection("posts")
                .whereEqualTo("is_event", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    event_arraylist.add(document.toObject(Event.class));
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
    private void buildEventFeed(){

        Toast.makeText(mContext, ""+event_arraylist.size(), Toast.LENGTH_SHORT).show();
        feed_layout_manager = new LinearLayoutManager(getContext());
        feed_adapter = new FeedAdapter(event_arraylist);
        feed_recycler_view.setLayoutManager(feed_layout_manager);
        feed_recycler_view.setAdapter(feed_adapter);
    }

}