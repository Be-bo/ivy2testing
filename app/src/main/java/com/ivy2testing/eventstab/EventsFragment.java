package com.ivy2testing.eventstab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.hometab.ViewPostOrEventActivity;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.Utils;

public class EventsFragment extends Fragment implements EventAdapter.EventClickListener {

    // MARK: Variables

    private View root_view;
    private SwipeRefreshLayout refresh_layout;
    private User this_user;
    private UserViewModel this_user_vm;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference stor = FirebaseStorage.getInstance().getReference();

    private RecyclerView for_you_recycler;
    private RecyclerView today_recycler;
    private RecyclerView this_week_recycler;
    private RecyclerView upcoming_recycler;

    private ProgressBar for_you_progress_bar; //not connected
    private ProgressBar today_progress_bar;
    private ProgressBar this_week_progress_bar;
    private ProgressBar upcoming_progress_bar;

    private TextView for_you_title;
    private TextView today_title;
    private TextView this_week_title;
    private TextView upcoming_title;

    private EventAdapter today_adapter;
    private EventAdapter for_you_adapter;
    private EventAdapter this_week_adapter;
    private EventAdapter upcoming_adapter;


    private Button explore_all_btn;
    private CardView featured_cardview;
    private ImageView featured_imageview;
    private ProgressBar featured_progress_bar;
    private TextView featured_title;















    // MARK: Base

    public EventsFragment(Context con, User thisUser) {
        if(thisUser != null) this_user = thisUser;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_events, container, false);
        declareHandles();
        setUpViewModel();
        setUpFeatured();
        setUpRecyclers();
        setUpExploreAll();
        refreshLayoutSetup();
        return root_view;
    }

    private void declareHandles(){
        refresh_layout = root_view.findViewById(R.id.events_refresh_layout);
        featured_cardview = root_view.findViewById(R.id.fragment_events_featured_cardview);
        featured_imageview = root_view.findViewById(R.id.fragment_events_featured_image);
        for_you_recycler = root_view.findViewById(R.id.fragment_events_for_you_recycler);
        today_recycler = root_view.findViewById(R.id.fragment_events_today_recycler);
        this_week_recycler = root_view.findViewById(R.id.fragment_events_this_week_recycler);
        upcoming_recycler = root_view.findViewById(R.id.fragment_events_upcoming_recycler);
        today_title = root_view.findViewById(R.id.fragment_events_today_title);
        for_you_title = root_view.findViewById(R.id.fragment_events_for_you_title);
        this_week_title = root_view.findViewById(R.id.fragment_events_this_week_title);
        upcoming_title = root_view.findViewById(R.id.fragment_events_upcoming_title);
        explore_all_btn = root_view.findViewById(R.id.fragment_events_explore_all_button);
        today_progress_bar = root_view.findViewById(R.id.fragment_events_today_progress_bar);
        this_week_progress_bar = root_view.findViewById(R.id.fragment_events_this_week_progress_bar);
        upcoming_progress_bar = root_view.findViewById(R.id.fragment_events_upcoming_progress_bar);
        featured_progress_bar = root_view.findViewById(R.id.fragment_events_featured_progress_bar);
        featured_title = root_view.findViewById(R.id.fragment_events_featured_title);
    }

    private void setUpViewModel(){
        if (getActivity() != null) {
            this_user_vm = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            this_user = this_user_vm.getThis_user().getValue();
            this_user_vm.getThis_user().observe(getActivity(), (User updatedProfile) -> { //listen to realtime user profile changes
                if(updatedProfile != null) this_user = updatedProfile;
            });
        }
    }







    // MARK: Setup

    private void setUpExploreAll(){
        if(getContext() != null){
            db.collection("universities").document(Utils.getCampusUni(getContext())).collection("posts").whereEqualTo("is_event", true).whereEqualTo("is_active", true).whereEqualTo("is_featured", false).limit(1).get().addOnCompleteListener(querySnap -> {
                if(querySnap.isSuccessful() && querySnap.getResult() != null && !querySnap.getResult().isEmpty()){ //there's at least one relevant event
                    explore_all_btn.setVisibility(View.VISIBLE);
                    explore_all_btn.setOnClickListener(view -> goToAllEvents());
                }else{
                    explore_all_btn.setVisibility(View.GONE);
                }
            });
        }
    }

    private void setUpRecyclers(){
        if(getContext() != null){
            today_adapter = new EventAdapter(getContext(), Constant.EVENT_ADAPTER_TODAY, Utils.getCampusUni(getContext()), this, today_recycler, today_title, today_progress_bar);
            today_recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
            today_recycler.setAdapter(today_adapter);

            this_week_adapter = new EventAdapter(getContext(), Constant.EVENT_ADAPTER_THIS_WEEK, Utils.getCampusUni(getContext()), this, this_week_recycler, this_week_title, this_week_progress_bar);
            this_week_recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
            this_week_recycler.setAdapter(this_week_adapter);

            upcoming_adapter = new EventAdapter(getContext(), Constant.EVENT_ADAPTER_UPCOMING, Utils.getCampusUni(getContext()), this, upcoming_recycler, upcoming_title, upcoming_progress_bar);
            upcoming_recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
            upcoming_recycler.setAdapter(upcoming_adapter);
        }
    }

    private void setUpFeatured(){
        if(getContext()!=null){
            db.collection("universities").document(Utils.getCampusUni(getContext())).get().addOnCompleteListener(task -> {
                if(task.isSuccessful() && task.getResult() != null){
                    String featuredId = String.valueOf(task.getResult().get("featured_id"));
                    if(!featuredId.equals("null") && !featuredId.equals("")){
                        db.collection("universities").document(Utils.getCampusUni(getContext())).collection("posts").document(featuredId).get().addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful() && task1.getResult() != null){
                                Event featuredEvent = task1.getResult().toObject(Event.class);
                                if(featuredEvent != null && featuredEvent.getVisual() != null && featuredEvent.getVisual().contains("/")){
                                    stor.child(featuredEvent.getVisual()).getDownloadUrl().addOnCompleteListener(task2 -> {
                                        if(task2.isSuccessful() && task2.getResult() != null && getContext() != null){
                                            Glide.with(getContext()).load(task2.getResult()).into(featured_imageview);
                                            featured_cardview.setOnClickListener(view -> viewEvent(featuredEvent));
                                            featured_progress_bar.setVisibility(View.GONE);
                                            featured_cardview.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }else setFeaturedPlaceholder();
                            }else setFeaturedPlaceholder();
                        });
                    } else setFeaturedPlaceholder();
                }else setFeaturedPlaceholder();
            });
        }
    }

    private void setFeaturedPlaceholder(){
        featured_title.setVisibility(View.VISIBLE);
        featured_cardview.setVisibility(View.VISIBLE);
        featured_cardview.setElevation(0f);
        featured_progress_bar.setVisibility(View.GONE);
        featured_cardview.setOnClickListener(view -> Toast.makeText(getContext(), "Shoot us an email at theivysocialnetwork@gmail.com", Toast.LENGTH_LONG).show());
    }

    private void refreshLayoutSetup() {
        refresh_layout.setOnRefreshListener(() -> {
            refreshAdapters();
            new Handler().postDelayed(() -> { refresh_layout.setRefreshing(false); }, 1000);
        });
    }

    public void refreshAdapters(){
        if(for_you_adapter != null) for_you_adapter.refreshAdapter();
        if(today_adapter!= null)today_adapter.refreshAdapter();
        if(this_week_adapter!=null)this_week_adapter.refreshAdapter();
        if(upcoming_adapter!=null)upcoming_adapter.refreshAdapter();
    }

    public void changeUni(){
        setUpRecyclers();
    }












    // MARK: Event Interaction

    @Override
    public void onEventClick(int position, int clickedId, int adapterType) {
        switch(adapterType){
            case Constant.EVENT_ADAPTER_FOR_YOU:
                //TODO
                break;
            case Constant.EVENT_ADAPTER_TODAY:
                handleEventItemClick(today_adapter.getItem(position), clickedId);
                break;
            case Constant.EVENT_ADAPTER_THIS_WEEK:
                handleEventItemClick(this_week_adapter.getItem(position), clickedId);
                break;
            case Constant.EVENT_ADAPTER_UPCOMING:
                handleEventItemClick(upcoming_adapter.getItem(position), clickedId);
                break;
        }
    }

    private void handleEventItemClick(Event event, int clickedId){
        if(clickedId == R.id.item_event_author_image) viewUserProfile(event.getAuthor_id(), event.getUni_domain(), event.getAuthor_is_organization());
        else viewEvent(event);
    }

    private void viewEvent(Post event) { // Transition to a post/event
        if(event != null){
            Intent intent = new Intent(getContext(), ViewPostOrEventActivity.class);
            intent.putExtra("this_user", this_user);
            intent.putExtra("post_id", event.getId());
            intent.putExtra("post_uni", event.getUni_domain());
            intent.putExtra("author_id", event.getAuthor_id());
            startActivity(intent);
        }
    }

    private void goToAllEvents(){
        Intent intent = new Intent(getContext(), ExploreAllEventsActivity.class);
        intent.putExtra("this_user", this_user);
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
