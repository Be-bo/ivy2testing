package com.ivy2testing.main;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.BubbleAdapter;
import com.ivy2testing.home.CreatePost;
import com.ivy2testing.R;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.home.EventsFragment;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.home.PostsFragment;
import com.ivy2testing.userProfile.OrganizationProfileFragment;
import com.ivy2testing.userProfile.StudentProfileFragment;
import com.ivy2testing.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {




    // MARK: Variables and Constants

    private final static String TAG = "MainActivityTag";
    private DrawerLayout drawer;
    private BottomNavigationView bottom_navigation;
    private FrameLayout loading_layout;
    private ImageButton post_button;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private String this_uni_domain;
    private UserViewModel user_view_model;
    private User this_user;

    private SectionsPageAdapter tab_adapter = new SectionsPageAdapter(getSupportFragmentManager());
    private ChatFragment chat_fragment = new ChatFragment();
    private OrganizationProfileFragment org_fragment = new OrganizationProfileFragment();
    private StudentProfileFragment stud_fragment = new StudentProfileFragment(true);
    private NoSwipeViewPager tab_view_pager;

    private final ArrayList<String> bubble_arraylist = new ArrayList<String>();
    private RecyclerView bubble_recycler_view;
    private RecyclerView.Adapter bubble_adapter;
    private RecyclerView.LayoutManager bubble_layout_manager;
    private HomeFragment campus_fragment = new HomeFragment(this);
    private EventsFragment event_fragment = null;
    private PostsFragment post_fragment = null;
    private String selected_bubble = "campus";







    // MARK: Override Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUp();

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            //resetMainBubble();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.LOGIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) attemptLogin();
        } else
            Log.w(TAG, "Don't know how to handle the request code, \"" + requestCode + "\" yet!");
    }










    // MARK: Setup Methods

    private void setUp(){
        setUpToolbar();
        setHandlers();
        attemptLogin();
    }

    private void setUpToolbar() {
        Toolbar main_toolbar = findViewById(R.id.main_toolbar_id);
        setSupportActionBar(main_toolbar);
        drawer = findViewById(R.id.main_layout_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, main_toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(null);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.interaction));
    }

    private void setHandlers(){
        bottom_navigation = findViewById(R.id.main_tab_bar);
        bottom_navigation.setSelectedItemId(R.id.tab_bar_home);
        loading_layout = findViewById(R.id.main_loadingScreen);
        post_button = findViewById(R.id.post_button);
        tab_view_pager = findViewById(R.id.tab_view_pager);
        post_button.setOnClickListener(view -> transToLogin());
    }

    private void setUpLoggedInInteraction() { //this method will set up all the interactive elements the user has access to when logged in, by default they're hidden (tab bar + post btn)
        post_button.setImageResource(R.drawable.ic_create);
        post_button.setOnClickListener(view -> transToCreatePost());
        bottom_navigation.setVisibility(View.VISIBLE);

        tab_adapter.addFragment(chat_fragment, "chat");
        tab_adapter.addFragment(campus_fragment, "campus");
        if(this_user.getIs_organization()) tab_adapter.addFragment(org_fragment, "organization");
        else tab_adapter.addFragment(stud_fragment, "student");

        tab_view_pager.setAdapter(tab_adapter);
        //TODO: Not ideal, all the bubbles essentially act as separate tabs -> limit 6 so that we can keep everything in mem, will have to be reworked in the future:
        //TODO: i.e. killing old bubbles when we reach the limit we decide on, instead of killing starting from 0th index (which will start killing actual tabs)
        tab_view_pager.setOffscreenPageLimit(6);

        bottom_navigation.setOnNavigationItemSelectedListener((menuItem) -> {
            switch (menuItem.getItemId()){
                case R.id.tab_bar_chat:
                    bubble_recycler_view.setVisibility(View.GONE);
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("chat"));
                    return true;
                case R.id.tab_bar_home:
                    bubble_recycler_view.setVisibility(View.VISIBLE);
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition(selected_bubble));
                    return true;
                case R.id.tab_bar_profile:
                    bubble_recycler_view.setVisibility(View.GONE);
                    if(this_user.getIs_organization()){
                        if(!org_fragment.isIs_set_up()) org_fragment.setUp();
                        tab_view_pager.setCurrentItem(tab_adapter.getPosition("organization"));
                    }
                    else{
                        if(!stud_fragment.isIs_set_up()) stud_fragment.setUp();
                        tab_view_pager.setCurrentItem(tab_adapter.getPosition("student"));
                    }
                    return true;
            }
            return false;
        });
        tab_view_pager.setCurrentItem(1);
    }











    // MARK: Base Methods

    private void attemptLogin(){
        startLoading();
        loadPreferences();
        user_view_model = new ViewModelProvider(this).get(UserViewModel.class);
        if(auth.getCurrentUser() != null && auth.getUid() != null && this_uni_domain != null){
            user_view_model.startListening(auth.getUid(), this_uni_domain);
            user_view_model.getThis_user().observe(this, (User updatedUser) -> {
                if(updatedUser != null){
                    //TODO: deal with banning, age update, notifications, etc.
                    this_user = updatedUser;
                    bubbleBarSetup();
                    if(tab_adapter.getCount() < 1) setUpLoggedInInteraction();
                    endLoading();
                }
            });
        }else{
            bubbleBarSetup();
            tab_adapter.addFragment(campus_fragment, "campus");
            tab_view_pager.setAdapter(tab_adapter);
            endLoading();
        }
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        this_uni_domain = sharedPreferences.getString("domain", "");
    }

    private void bubbleBarSetup(){
        bubble_arraylist.add("Campus");
        bubble_arraylist.add("Events");
        bubble_arraylist.add("Posts");
        bubble_recycler_view = findViewById(R.id.bubble_sample_rv);
        bubble_recycler_view.setHasFixedSize(true);
        bubble_layout_manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bubble_adapter = new BubbleAdapter(bubble_arraylist, position -> {
            switch (position){
                case 0:
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("campus"));
                    selected_bubble = "campus";
                    break;
                case 1:
                    if(event_fragment == null){
                        event_fragment = new EventsFragment(this);
                        tab_adapter.addFragment(event_fragment, "event");
                    }
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("event"));
                    selected_bubble = "event";
                    break;
                case 2:
                    if(post_fragment == null){
                        post_fragment = new PostsFragment(this);
                        tab_adapter.addFragment(post_fragment, "post");
                    }
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("post"));
                    selected_bubble = "post";
                    break;
            }
        });
        bubble_recycler_view.setLayoutManager(bubble_layout_manager);
        bubble_recycler_view.setAdapter(bubble_adapter);
    }










    // MARK: Other Methods

    private void transToCreatePost(){
        Intent intent = new Intent(getApplicationContext(), CreatePost.class);
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }

    private void transToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, Constant.LOGIN_REQUEST_CODE);
    }

    private void startLoading(){
        loading_layout.setVisibility(View.VISIBLE);      // Bring up view to cover entire screen
        getSupportFragmentManager().beginTransaction().replace(R.id.main_loadingScreen, new LoadingPageFragment(this)).commit();      // Populate View with loading page layout
    }

    // Fade out loading page animation
    private void endLoading(){
        loading_layout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));  // Start fade out animation
        loading_layout.setVisibility(View.GONE);
    }
}
