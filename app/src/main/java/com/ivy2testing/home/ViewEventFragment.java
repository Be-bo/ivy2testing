package com.ivy2testing.home;

import android.content.Intent;
import android.net.Uri;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.SeeAllPostsActivity;
import com.ivy2testing.main.SeeAllUsersActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.adapters.CircleImageAdapter;
import com.ivy2testing.util.Constant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/** @author Zahra Ghavasieh
 * Overview: Event view fragment
 * Note: Includes text, pinned ID, and event only views
 */
public class ViewEventFragment extends Fragment implements CircleImageAdapter.OnPersonListener {


    // Constants
    private final static String TAG = "ViewPostFragment";
    private final static int GOING_LIMIT = 5;

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
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Event event;
    private User this_user;             // Nullable!
    private Uri viewer_img;             // Nullable

    private CircleImageAdapter going_adapter;       // Recycler Adapter for going_ids
    private LinearLayoutManager layout_man;         // Recycler Layout manager
    private List<Uri> going_img_uris = new ArrayList<>(6); // non synchronous adds!
    private int lastUriPosition = 0;                // Pagination: position of last img loaded


    // Constructor
    public ViewEventFragment(Event event, User this_user){
        this.event = event;
        this.this_user = this_user;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveEventToDB();  // Save any changes to database
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
        else v.findViewById(R.id.viewEvent_linkLayout).setVisibility(View.GONE);

        if (event.getPinned_id() != null && !event.getPinned_id().isEmpty())
            tv_pinned.setText(event.getPinned_id()); //TODO change to pin name
        else v.findViewById(R.id.viewPost_pinLayout).setVisibility(View.GONE);

        // Going Users' Recycler View
        setRecyclerVisibility();
        setRecycler();

        // Hide button if user is not signed in
        if (this_user == null){
            button_going.setVisibility(View.GONE);
        }
        else if (event.getGoing_ids().contains(this_user.getId())) button_going.setChecked(true);
    }

    // OnClick and scroll Listeners
    private void setListeners(){
        if (event.getLink() != null) tv_link.setOnClickListener(v1 -> goToLink());
        if (event.getPinned_id() != null) tv_pinned.setOnClickListener(v1 -> viewPinned());
        tv_seeAll.setOnClickListener(v1 -> seeAllGoingUsers());
        if (button_going.getVisibility() == View.VISIBLE)
            button_going.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) going(); // toggle is enabled
                else notGoing();        // toggle is disabled
            });

    }

    // Set recycler for going users (at this point, going_ids isn't empty!)
    private void setRecycler(){
        if(event.getGoing_ids().size() > 0){
            if(event.getGoing_ids().size() < Constant.PROFILE_MEMBER_LIMIT) going_adapter = new CircleImageAdapter(event.getGoing_ids(), event.getUni_domain(), getContext(), this);
            else going_adapter = new CircleImageAdapter(event.getGoing_ids().subList(0, Constant.PROFILE_MEMBER_LIMIT), event.getUni_domain(), getContext(), this);
            rv_going.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false){
                @Override
                public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                    lp.width = getWidth() / Constant.PROFILE_MEMBER_LIMIT;
                    return true;
                }
            });
            rv_going.setAdapter(going_adapter);
        }
    }


    // Set Going recycler visibility
    private void setRecyclerVisibility(){
        if(event.getGoing_ids().size() > 0){
            rv_going.setVisibility(View.VISIBLE);
            tv_seeAll.setVisibility(View.VISIBLE);
        }else{
            rv_going.setVisibility(View.GONE);
            tv_seeAll.setVisibility(View.GONE);
        }
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
            seeAllPosts();  // Pass a "query" to SeeAllPostsActivity
        }

        // Pull pinned event page
        else {
            Log.d(TAG, "View Pinned Event Page...");
            loadEventFromDB();
        }
    }

    // See all posts that are pinned to the same event
    private void seeAllPosts() {
        Intent intent = new Intent(getContext(), SeeAllPostsActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("this_uni_domain", event.getUni_domain());
        intent.putExtra("title", event.getPinned_id());

        // Make "Query"
        HashMap<String, Object> query_map = new HashMap<String, Object>() {{ put("pinned_id", event.getPinned_id()); }};
        intent.putExtra("query_map", query_map);

        startActivityForResult(intent, Constant.SEEALL_POSTS_REQUEST_CODE);
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
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Constant.VIEW_POST_REQUEST_CODE);
    }

    // OnClick for See All: Launch a new Activity to view users
    private void seeAllGoingUsers(){
        Intent intent = new Intent(getContext(), SeeAllUsersActivity.class);
        Log.d(TAG, "Starting SeeAll Activity to see all going users");
        intent.putExtra("title", "Going Users");
        intent.putExtra("this_user", this_user);
        intent.putExtra("uni_domain", event.getUni_domain());
        intent.putExtra("user_ids", (ArrayList<String>) event.getGoing_ids());
        startActivityForResult(intent, Constant.SEEALL_USERS_REQUEST_CODE);
    }

    // View a user's profile
    @Override
    public void onPersonClicked(int position) {
        if (getActivity() != null) {

            // We don't know if it is a student but StudentProfile will automatically
            // transition to OrganizationProfile if user is not a student.
            Log.d(TAG, "Starting StudentProfile Activity for user " + event.getGoing_ids().get(position));
            Intent intent = new Intent(getActivity(), StudentProfileActivity.class);
            intent.putExtra("student_to_display_id", event.getGoing_ids().get(position));
            intent.putExtra("student_to_display_uni", event.getUni_domain());
            intent.putExtra("this_user", this_user);
            startActivity(intent);
        }
        else Log.e(TAG, "Parent Activity was null!");
    }

    // Add user to going list
    private void going(){
        event.addGoingIdToList(0, this_user.getId());
        setRecyclerVisibility();

        // Load viewer preview pic if not done so yet

        //TODO: has to be resolved
//        if (viewer_img == null) loadUserPic(viewer_id);
//        else {
//            going_img_uris.add(0, viewer_img);
//            going_adapter.notifyItemInserted(0);
//        }
    }

    // Remove user from going list
    private void notGoing(){
        event.deleteGoingIdFromList(this_user.getId());

        // Delete viewer preview pic from recycler
        going_img_uris.remove(viewer_img);
        going_adapter.notifyItemRemoved(0);

        setRecyclerVisibility();    // Remove Visibility if that was the last person
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

    // Save any changes to event to database
    private void saveEventToDB() {
        String address = "universities/" + event.getUni_domain() + "/posts/" + event.getId();
        if (address.contains("null")){
            Log.e(TAG, "Event Address has null values. ID:" + event.getId());
            return;
        }

        db.document(address).set(event).addOnCompleteListener(task->{
            if (task.isSuccessful()) Log.d(TAG, "Changes saved to event: " + event.getId());
            else Log.e(TAG, "Something went wrong when trying to save changes.\n");
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
