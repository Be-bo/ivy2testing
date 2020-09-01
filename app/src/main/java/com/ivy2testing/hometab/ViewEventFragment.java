package com.ivy2testing.hometab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.SeeAllPostsActivity;
import com.ivy2testing.main.SeeAllUsersActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.adapters.CircleUserAdapter;
import com.ivy2testing.util.Constant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author Zahra Ghavasieh
 * Overview: Event view fragment
 * Note: Includes text, pinned ID, and event only views
 */
public class ViewEventFragment extends Fragment implements CircleUserAdapter.OnPersonListener {


    // Constants
    private final static String TAG = "ViewEventFragmentTag";
    private final static int GOING_LIMIT = 5;

    // Views
    private TextView tv_time;
    private TextView tv_location;
    private TextView tv_description;
    private TextView tv_pinned;
    private RecyclerView going_recycler;
    private TextView tv_seeAll;
    private ImageButton button_going;
    private TextView nobody_going_text;
    private TextView link_button_text;
    private TextView going_button_text;
    private TextView whos_going_text;

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Other Variables
    private Event event;
    private User this_user;
    private CircleUserAdapter going_adapter;

    // button tray
    private ConstraintLayout button_tray;
    private ImageButton share_button;
    private ImageButton link_button;
    private ImageButton calendar_button;


    // Constructor
    public ViewEventFragment(Event event, User this_user) {
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


    /* Initialization Methods
     ***************************************************************************************************/

    // Define and initialize views
    private void declareViews(View v) {
        tv_time = v.findViewById(R.id.viewEvent_time);
        tv_location = v.findViewById(R.id.viewEvent_place);
        tv_description = v.findViewById(R.id.viewPost_description);
        tv_pinned = v.findViewById(R.id.viewPost_pinned);
        going_recycler = v.findViewById(R.id.viewEvent_goingRecycler);
        tv_seeAll = v.findViewById(R.id.viewEvent_seeAll);
        button_going = v.findViewById(R.id.viewEvent_goingButton);
        nobody_going_text = v.findViewById(R.id.viewEvent_nobody_going_text);
        whos_going_text = v.findViewById(R.id.viewEvent_whos_going_text);

        // button tray
        button_tray = v.findViewById(R.id.button_tray_constraint);
        share_button = v.findViewById(R.id.view_event_share_button);
        link_button = v.findViewById(R.id.view_event_link_button);
        calendar_button = v.findViewById(R.id.viewEvent_add_calendar_button);
        link_button_text = v.findViewById(R.id.viewEvent_link_button_text);
        going_button_text = v.findViewById(R.id.viewEvent_going_button_text);
    }

    // Populate views
    private void setFields(View v) {

        // Time
        String time = convertMillisToReadableDisplay(event.getStart_millis())
                + " - " + convertMillisToReadableDisplay(event.getEnd_millis());
        tv_time.setText(time);

        // Mandatory text fields
        tv_location.setText(event.getLocation());
        tv_description.setText(event.getText());

        if (event.getPinned_id() != null && !event.getPinned_id().isEmpty())
            tv_pinned.setText(event.getPinned_id()); //TODO change to pin name
        else v.findViewById(R.id.viewPost_pinLayout).setVisibility(View.GONE);

        // Going Users' Recycler View
        setGoingLayout();
        setUpGoingAdapter();

        // Hide button if user is not signed in
        if (this_user == null || !this_user.getUni_domain().equals(event.getUni_domain())) { //either not logged in, or not from this uni, or is org -> can't say going // now updated to not show button tray
            button_tray.setVisibility(View.GONE);
        } else if (event.getGoing_ids().contains(this_user.getId())){
            button_going.setImageResource(R.drawable.ic_going);
            going_button_text.setText(getString(R.string.going));
        }
    }

    // OnClick and scroll Listeners
    private void setListeners() {
        if (event.getLink() != null) {
            link_button.setVisibility(View.VISIBLE);
            link_button_text.setVisibility(View.VISIBLE);
            link_button.setOnClickListener(v1 -> goToLink());
        }
        if (event.getPinned_id() != null) tv_pinned.setOnClickListener(v1 -> viewPinned());
        tv_seeAll.setOnClickListener(v1 -> seeAllGoingUsers());
        if (button_going.getVisibility() == View.VISIBLE)
            button_going.setOnClickListener(view -> {
                if (!event.getGoing_ids().contains(this_user.getId())) setThisUserGoing(); // toggle is enabled
                else setThisUserNotGoing();        // toggle is disabled
            });

        share_button.setOnClickListener(v1 -> {
            shareExternal();
        });
        calendar_button.setOnClickListener(v1 -> {
            addToCalendar();
        });

    }

    // Set recycler for going users (at this point, going_ids isn't empty!)
    private void setUpGoingAdapter(){
        if(event.getGoing_ids().size() < Constant.PEOPLE_PREVIEW_LIMIT) going_adapter = new CircleUserAdapter(event.getGoing_ids(), getContext(), this);
        else going_adapter = new CircleUserAdapter(event.getGoing_ids().subList(0, Constant.PEOPLE_PREVIEW_LIMIT), getContext(), this);
        going_recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false){
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = getWidth() / Constant.PEOPLE_PREVIEW_LIMIT;
                return true;
            }
        });
        going_recycler.setAdapter(going_adapter);
    }


    // Set Going recycler visibility
    private void setGoingLayout(){
        if(event.getGoing_ids().size() > 0){
            whos_going_text.setVisibility(View.VISIBLE);
            nobody_going_text.setVisibility(View.GONE);
            going_recycler.setVisibility(View.VISIBLE);
            if(event.getGoing_ids().size() > Constant.PEOPLE_PREVIEW_LIMIT) tv_seeAll.setVisibility(View.VISIBLE); //only show see all if the ppl going exceed the capacity
            else tv_seeAll.setVisibility(View.GONE);
        }else{
            whos_going_text.setVisibility(View.GONE);
            nobody_going_text.setVisibility(View.VISIBLE);
            going_recycler.setVisibility(View.INVISIBLE);
            tv_seeAll.setVisibility(View.GONE);
        }
        if(this_user!=null && !this_user.getIs_organization()) {
            if (event.getGoing_ids().contains(this_user.getId())) {
                button_going.setImageResource(R.drawable.ic_going);
                going_button_text.setText(getString(R.string.going));
            } else {
                button_going.setImageResource(R.drawable.ic_not_going);
                going_button_text.setText(getString(R.string.not_going));
            }
        }else{ //if user null / is org -> hide the button
            going_button_text.setVisibility(View.GONE);
            button_going.setVisibility(View.GONE);
        }
    }


    /* OnClick and Transition Methods
     ***************************************************************************************************/

    // OnClick for link: Go to a browser to view link
    private void goToLink() {
        Log.d(TAG, "Opening browser...");
        if (URLUtil.isValidUrl(event.getLink())) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getLink()));
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Can't open link properly :(", Toast.LENGTH_SHORT).show();
        }
    }

    // sends a plain text send intent to any apps with proper receiving parameters.
    // uses Sharesheet, instead of intent resolver
    private void shareExternal() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, createShareText(100));
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, "Choose where to share");
        startActivity(shareIntent);
    }


    // method will check size of description text, and build a text block to send external
    private String createShareText(int max_description_size) {

        // determine proper size for description.
        int length = event.getText().length();
        String description = "";
        if (length > max_description_size) description = event.getText().substring(0, max_description_size) + "...";
        else description = event.getText();

        String share_text =
                "Check out this event on Ivy: \n"
                        + event.getName() + "\n"
                        + "from: " + convertMillisToReadableShare(event.getStart_millis()) + "\n"
                        + "until: " + convertMillisToReadableShare(event.getEnd_millis()) + "\n"
                        + "at: " + event.getLocation() + "\n"
                        + description;

        if (event.getLink() != null) share_text += "\nLink: " + event.getLink();

        return share_text;
    }

    // takes user to default calendar and autofills fields
    private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStart_millis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEnd_millis())
                .putExtra(CalendarContract.Events.TITLE, event.getName())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getText())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocation())
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        startActivity(intent);
    }


    // OnClick for pinned Event:
    // Start new Activity to view posts relating to event if pinned == event
    // Else open the pinned event page
    private void viewPinned() {
        String address = "universities/" + event.getUni_domain() + "/posts";
        if (address.contains("null")) {
            Log.e(TAG, "Event Address has null values. ID:" + event.getUni_domain());
            return;
        }

        // Pull all posts relating to event
        if (event.getId().equals(event.getPinned_id())) {
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
        HashMap<String, Object> query_map = new HashMap<String, Object>() {{
            put("pinned_id", event.getPinned_id());
        }};
        intent.putExtra("query_map", query_map);

        startActivityForResult(intent, Constant.SEEALL_POSTS_REQUEST_CODE);
    }

    // Start new Activity to view event
    private void viewEventPage(Event event) {
        if (event == null || event.getId() == null) {
            Log.e(TAG, "Event was null!");
            return;
        }

        Intent intent = new Intent(getContext(), ViewPostOrEventActivity.class);
        Log.d(TAG, "Starting ViewPost Activity for event " + event.getId());
        intent.putExtra("post", event);
        intent.putExtra("this_user", this_user);
        intent.putExtra("author_id", event.getAuthor_id());
        startActivityForResult(intent, Constant.VIEW_POST_REQUEST_CODE);
    }

    // OnClick for See All: Launch a new Activity to view users
    private void seeAllGoingUsers() {
        if(this_user != null){
            Intent intent = new Intent(getContext(), SeeAllUsersActivity.class);
            Log.d(TAG, "Starting SeeAll Activity to see all going users");
            intent.putExtra("title", "Going Users");
            intent.putExtra("this_user", this_user);
            intent.putExtra("uni_domain", event.getUni_domain());
            intent.putExtra("user_ids", (ArrayList<String>) event.getGoing_ids());
            startActivityForResult(intent, Constant.SEEALL_USERS_REQUEST_CODE);
        }else{
            Toast.makeText(getContext(), "You have to log in.", Toast.LENGTH_LONG).show();
        }
    }

    // View a user's profile
    @Override
    public void onPersonClicked(int position) {
        if (getActivity() != null && this_user != null) {

            // We don't know if it is a student but StudentProfile will automatically
            // transition to OrganizationProfile if user is not a student.
            Log.d(TAG, "Starting StudentProfile Activity for user " + event.getGoing_ids().get(position));
            Intent intent = new Intent(getActivity(), StudentProfileActivity.class);
            intent.putExtra("student_to_display_id", event.getGoing_ids().get(position));
            intent.putExtra("student_to_display_uni", event.getUni_domain());
            intent.putExtra("this_user", this_user);
            startActivity(intent);
        } else Toast.makeText(getContext(), "You have to log in.", Toast.LENGTH_LONG).show();
    }

    // Add user to going list
    private void setThisUserGoing() {
        db.collection("universities").document(event.getUni_domain()).collection("posts").document(event.getId()).update("going_ids", FieldValue.arrayUnion(this_user.getId())).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                going_adapter.addUser(this_user.getId());
                event.addGoingIdToList(0, this_user.getId());
                setGoingLayout();
            } else {
                Toast.makeText(getContext(), "Failed to add user as going to the event.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Remove user from going list
    private void setThisUserNotGoing() {
        db.collection("universities").document(event.getUni_domain()).collection("posts").document(event.getId()).update("going_ids", FieldValue.arrayRemove(this_user.getId())).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                going_adapter.removeUser(this_user.getId());
                event.deleteGoingIdFromList(this_user.getId());
                setGoingLayout();    // Remove Visibility if that was the last person
            } else {
                Toast.makeText(getContext(), "Failed to remove user as going to the event.", Toast.LENGTH_LONG).show();
            }
        });
    }


    /* Firebase Related Methods
     ***************************************************************************************************/

    // Pull pinned event from database
    private void loadEventFromDB() {
        String address = "universities/" + event.getUni_domain() + "/posts/" + event.getPinned_id();
        if (address.contains("null")) {
            Log.e(TAG, "Event Address has null values. ID:" + event.getUni_domain());
            return;
        }

        db.document(address).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Event event = task.getResult().toObject(Event.class);
                if (event != null) {
                    event.setId(event.getPinned_id());
                    viewEventPage(event);
                } else Log.e(TAG, "Event was null!");
            } else {
                Log.e(TAG, "loadEventFromDB: unsuccessful or does not exist.");
            }
        });
    }

    /* Utility Methods
     ***************************************************************************************************/

    // Convert time in Millis to readable text for sharing externally
    private String convertMillisToReadableShare(Long millis) {

        // Format string in locale timezone base on device settings
        DateFormat formatter = new SimpleDateFormat("EEE MMM d,", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        return formatter.format(cal.getTime()) + " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(millis);
    }


    // A long of millis will run the gauntlet to see if it is same day or  within a day/ week from now
    private String convertMillisToReadableDisplay(Long millis) {

        Calendar cal_today = Calendar.getInstance();
        int year_today = cal_today.get(cal_today.YEAR);
        int week_today = cal_today.get(cal_today.WEEK_OF_YEAR);
        int day_today = cal_today.get(cal_today.DAY_OF_YEAR);

        // Calendar instantiated with time to check
        Calendar check_cal = Calendar.getInstance();
        check_cal.setTimeInMillis(millis);
        int check_year = check_cal.get(check_cal.YEAR);
        int check_week = check_cal.get(check_cal.WEEK_OF_YEAR);
        int check_day = check_cal.get(check_cal.DAY_OF_YEAR);


        String return_string = null;

        // Must check year first
        if (year_today == check_year) {

            DateFormat formatter = new SimpleDateFormat("EEE,", Locale.getDefault());

            if (day_today == check_day) {
                return_string = "Today";

            } else if (day_today + 1 == check_day ) {
                return_string = "Tomorrow";

            } else if(day_today - 1 ==check_day){
                return_string = "Yesterday";

            } else if(week_today == check_week){
                return_string = "This " + formatter.format(check_cal.getTime());

            } else if(week_today + 1 == check_week){
                return_string = "Next " + formatter.format(check_cal.getTime());

            } else if(week_today - 1 == check_week){
                return_string = "Last " + formatter.format(check_cal.getTime());

            }
        }

        // if Return string did not get updated, return to standard format
        if (return_string == null){
            DateFormat formatter_default = new SimpleDateFormat("EEE MMM d,", Locale.getDefault());
            return_string = formatter_default.format(check_cal.getTime());
        }

        return return_string + " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(millis);
    }
}

