package com.ivy2testing.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.util.Constant;

/** @author Zahra Ghavasieh
 * Overview: Event view fragment
 * Note: Includes text, pinned ID, and event only views
 */
public class ViewEventFragment extends Fragment {


    // Constants
    private final static String TAG = "ViewPostFragment";

    // Views
    private TextView tv_time;
    private TextView tv_location;
    private TextView tv_link;
    private TextView tv_description;
    private TextView tv_pinned;
    private RecyclerView rv_going;
    private TextView tv_seeAll;
    private ToggleButton button_going;

    // FireBase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Other Variables
    private Event event;
    private String viewer_id;       // Nullable!


    // Constructor
    public ViewEventFragment(Event event, String viewer_id){
        this.event = event;
        this.viewer_id = viewer_id;
    }


    /* Override Methods
     ***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root_view = inflater.inflate(R.layout.fragment_viewevent, container, false);

        // Initialization Methods
        declareViews(root_view);
        setFields(root_view);
        setListeners(root_view);
        return root_view;
    }

    /* Initialization Methods
     ***************************************************************************************************/

    private void declareViews(View v){
        tv_description = v.findViewById(R.id.viewPost_description);
        tv_description.setText(event.getText());

        // Handle Pinned Event
        tv_pinned = v.findViewById(R.id.viewPost_pinned);
        if (event.getPinned_id() != null){
            tv_pinned.setText(event.getPinned_id()); //TODO change to pin name
            tv_pinned.setOnClickListener(v1 -> loadEventFromDB());
        }
        else v.findViewById(R.id.viewPost_pinLayout).setVisibility(View.GONE);
    }

    private void setFields(View v){

    }

    private void setListeners(View v){

    }


    /* Transition Methods
     ***************************************************************************************************/

    // Start new Activity to view event
    private void viewEventPage(Event event){
        if (event == null || event.getId() == null){
            Log.e(TAG, "Event was null!");
            return;
        }

        if (event.getId().equals(event.getId())){
            Log.d(TAG, "Event was pinned by itself");
            return;
        }

        Intent intent = new Intent(getContext(), ViewPostOrEventActivity.class);
        Log.d(TAG, "Starting ViewPost Activity for event " + event.getId());
        intent.putExtra("event", event);
        intent.putExtra("this_user_id", viewer_id);
        startActivityForResult(intent, Constant.VIEW_POST_REQUEST_CODE);
    }


    /* Firebase Related Methods
     ***************************************************************************************************/

    // Pull event from database
    private void loadEventFromDB(){
        String address = "universities/" + event.getUni_domain() + "/posts/" + event.getPinned_id();
        if (address.contains("null")){
            Log.e(TAG, "Event Address has null values. ID:" + event.getPinned_id());
            return;
        }

        db.document(address).get().addOnCompleteListener(task->{
            if (task.isSuccessful() && task.getResult() != null) {
                Event event = task.getResult().toObject(Event.class);
                if (event != null) {
                    event.setId(event.getPinned_id());
                    viewEventPage(event);
                } else Log.e(TAG, "Event was null!");
            }
            else {
                Log.e(TAG, "loadEventFromDB: unsuccessful or does not exist.");
            }
        });
    }
}
