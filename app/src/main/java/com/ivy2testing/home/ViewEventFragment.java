package com.ivy2testing.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    private String viewer_id;           // Nullable!
    private ArrayAdapter<Uri> adapter;  // Recycler Adapter for going_ids


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
        setListeners();
        return root_view;
    }

/* Initialization Methods
***************************************************************************************************/

    // Define and initialize views
    private void declareViews(View v){
        tv_time = v.findViewById(R.id.viewEvent_time);
        tv_location = v.findViewById(R.id.viewEvent_place);
        tv_link = v.findViewById(R.id.viewEvent_link);
        tv_description = v.findViewById(R.id.viewPost_description);
        tv_pinned = v.findViewById(R.id.viewPost_pinned);
        rv_going = v.findViewById(R.id.viewEvent_goingRecycler);
        tv_seeAll = v.findViewById(R.id.viewEvent_seeAll);
        button_going = v.findViewById(R.id.viewEvent_goingButton);
    }

    // Populate views
    private void setFields(View v){

        // Time
        String time = convertMillisToReadable(event.getStart_millis())
                + " - " + convertMillisToReadable(event.getEnd_millis());
        tv_time.setText(time);

        // Mandatory text fields
        tv_location.setText(event.getLocation());
        tv_description.setText(event.getText());

        // Optional text Fields
        if (event.getLink() != null) tv_link.setText(event.getLink());
        else tv_link.setVisibility(View.GONE);

        if (event.getPinned_id() != null) tv_pinned.setText(event.getPinned_id()); //TODO change to pin name
        else v.findViewById(R.id.viewPost_pinLayout).setVisibility(View.GONE);

        // Going Users' Recycler View
        if (event.getGoing_ids().isEmpty()) {
            rv_going.setVisibility(View.GONE);
            tv_seeAll.setVisibility(View.VISIBLE);
        }
        else {
            //TODO set adapter and recyclerView
            //TODO add pagination for loading
        }
    }

    // OnClick Listeners
    private void setListeners(){
        if (event.getLink() != null) tv_link.setOnClickListener(v1 -> goToLink());
        if (event.getPinned_id() != null) tv_pinned.setOnClickListener(v1 -> viewPinned());
        if (!event.getGoing_ids().isEmpty()) tv_seeAll.setOnClickListener(v1 -> seeAllGoingUsers());
        button_going.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) going(); //toggle is enabled
            else notGoing();        // toggle is disabled
        });
    }


/* OnClick and Transition Methods
***************************************************************************************************/

    // OnClick for link: Go to a browser to view link
    private void goToLink(){
        Log.d(TAG, "Opening browser...");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(event.getLink()));     // link not null!!
        startActivity(intent);
    }

    // OnClick for pinned Event:
    // Start new Activity to view posts relating to event if pinned == event
    // Else open the pinned event page
    private void viewPinned(){
        String address = "universities/" + event.getUni_domain() + "/posts";
        if (address.contains("null")){
            Log.e(TAG, "Event Address has null values. ID:" + event.getUni_domain());
            return;
        }

        // Pull all posts relating to event
        if (event.getId().equals(event.getPinned_id())){
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

        Intent intent = new Intent(getContext(), ViewPostOrEventActivity.class);
        Log.d(TAG, "Starting ViewPost Activity for event " + event.getId());
        intent.putExtra("post", event);
        intent.putExtra("this_user_id", viewer_id);
        startActivityForResult(intent, Constant.VIEW_POST_REQUEST_CODE);
    }

    // OnClick for See All: Launch a new Activity to view users
    private void seeAllGoingUsers(){
        //TODO
    }

    // Add user to going list
    private void going(){
        //TODO
    }

    // Remove user from going list
    private void notGoing(){
        //TODO
    }


/* Firebase Related Methods
***************************************************************************************************/

    // Pull pinned event from database
    private void loadEventFromDB(){
        String address = "universities/" + event.getUni_domain() + "/posts/" + event.getPinned_id();
        if (address.contains("null")){
            Log.e(TAG, "Event Address has null values. ID:" + event.getUni_domain());
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

/* Utility Methods
***************************************************************************************************/

    // Convert time in Millis to readable text
    private String convertMillisToReadable(Long millis){

        // Format string in locale timezone base on device settings
        DateFormat formatter = new SimpleDateFormat("EEE MMM d, hh a", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        return formatter.format(cal.getTime());
    }
}
