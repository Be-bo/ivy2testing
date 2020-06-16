package com.ivy2testing.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.util.Constant;

/** @author Zahra Ghavasieh
 * Overview: Post view fragment only includes text and pinned ID
 */
public class ViewPostFragment extends Fragment {

    // Constants
    private final static String TAG = "ViewPostFragment";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Other Variables
    private Post post;
    private String viewer_id;   // Nullable!


    // Constructor
    public ViewPostFragment(Post post, String viewer_id){
        this.post = post;
        this.viewer_id = viewer_id;
    }


/* Override Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root_view = inflater.inflate(R.layout.fragment_viewpost, container, false);

        // Initialization Methods
        setUp(root_view);
        return root_view;
    }

/* Initialization Methods
***************************************************************************************************/

    private void setUp(View v){
        // Populate Text View
        TextView tv_description = v.findViewById(R.id.viewPost_description);
        tv_description.setText(post.getText());

        // Handle Pinned Event
        TextView tv_pinned_post = v.findViewById(R.id.viewPost_pinned);
        if (post.getPinned_id() != null){
            tv_pinned_post.setText(post.getPinned_id()); //TODO change to pin name
            tv_pinned_post.setOnClickListener(v1 -> viewPinned());
        }
        else v.findViewById(R.id.viewPost_pinLayout).setVisibility(View.GONE);
    }


/* Transition and OnClick Methods
***************************************************************************************************/

    // OnClick for pinned Event:
    // Start new Activity to view posts relating to event if pinned == event
    // Else open the pinned event page
    private void viewPinned(){
        String address = "universities/" + post.getUni_domain() + "/posts";
        if (address.contains("null")){
            Log.e(TAG, "Event Address has null values. ID:" + post.getUni_domain());
            return;
        }

        // Pull all posts relating to event
        if (post.getId().equals(post.getPinned_id())){
            Log.d(TAG, "View all posts related to this event...");
            //TODO pass a query to seeAllActivity
        }

        // Pull pinned event page
        else {
            Log.d(TAG, "View Pinned Event Page...");
            loadEventFromDB();
        }
    }

    // Start new Activity to view event
    private void viewEventPage(Event event){
        if (event == null || event.getId() == null){
            Log.e(TAG, "Event was null!");
            return;
        }

        if (event.getId().equals(post.getId())){
            Log.d(TAG, "Event was pinned by itself");
            return;
        }

        Intent intent = new Intent(getContext(), ViewPostOrEventActivity.class);
        Log.d(TAG, "Starting ViewPost Activity for event " + event.getId());
        intent.putExtra("post", event);
        intent.putExtra("this_user_id", viewer_id);
        startActivityForResult(intent, Constant.VIEW_POST_REQUEST_CODE);
    }


/* Firebase Related Methods
***************************************************************************************************/

    // Pull event from database
    private void loadEventFromDB(){
        String address = "universities/" + post.getUni_domain() + "/posts/" + post.getPinned_id();
        if (address.contains("null")){
            Log.e(TAG, "Event Address has null values. ID:" + post.getPinned_id());
            return;
        }

        db.document(address).get().addOnCompleteListener(task->{
           if (task.isSuccessful() && task.getResult() != null) {
               Event event = task.getResult().toObject(Event.class);
               if (event != null) {
                   event.setId(post.getPinned_id());
                   viewEventPage(event);
               } else Log.e(TAG, "Event was null!");
           }
           else {
               Log.e(TAG, "loadEventFromDB: unsuccessful or does not exist.");
           }
        });
    }
}
