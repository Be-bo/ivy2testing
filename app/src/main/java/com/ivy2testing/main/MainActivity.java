package com.ivy2testing.main;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
    private HomeFragment home_fragment = new HomeFragment(this);
    private OrganizationProfileFragment org_fragment = new OrganizationProfileFragment();
    private StudentProfileFragment stud_fragment = new StudentProfileFragment(true);
    private NoSwipeViewPager tab_view_pager;

    // bubbles

    private final ArrayList<String> bubble_arraylist = new ArrayList<String>();

    private RecyclerView bubble_recycler_view;
    private RecyclerView.Adapter bubble_adapter;

    private RecyclerView.LayoutManager bubble_layout_manager;

    private HomeFragment hf = null;
    private EventsFragment ef = null;
    private PostsFragment pf = null;






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
        Log.d(TAG, "Setting up main");
        setUpToolbar();
        setHandlers();
        attemptLogin();
        bubbleBarSetup();
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

        Log.d(TAG, "size: "+tab_adapter.getCount());

        tab_adapter.addFragment(chat_fragment, "");
        tab_adapter.addFragment(home_fragment, "");
        if(this_user.getIs_organization()) tab_adapter.addFragment(org_fragment, "");
        else tab_adapter.addFragment(stud_fragment, "");

        Log.d(TAG, "size: "+tab_adapter.getCount());

        tab_view_pager.setAdapter(tab_adapter);
        tab_view_pager.setOffscreenPageLimit(5);

        bottom_navigation.setOnNavigationItemSelectedListener((menuItem) -> {
            bubble_recycler_view.setVisibility(View.GONE);
            Fragment selectedFragment = null;
            switch (menuItem.getItemId()){
                case R.id.tab_bar_chat:
                    tab_view_pager.setCurrentItem(0);
                    return true;
                case R.id.tab_bar_home:
                    tab_view_pager.setCurrentItem(1);
                    return true;
                    bubble_recycler_view.setVisibility(View.VISIBLE);
                    if (hf == null){
                        hf = HomeFragment.newInstance(MainActivity.this);
                    }
                    selectedFragment = hf;
                    break;
                case R.id.tab_bar_profile:
                    if(!org_fragment.isIs_set_up() && !stud_fragment.isIs_set_up()){
                        Log.d(TAG, "inside");
                        if(this_user.getIs_organization()){
                            Log.d(TAG, "org");
                            org_fragment.setUp();
                        }
                        else{
                            Log.d(TAG, "student");
                            stud_fragment.setUp();
                        }
                    }
                    tab_view_pager.setCurrentItem(2);
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
                    if(tab_adapter.getCount() < 1) setUpLoggedInInteraction();
                    endLoading();
                }
            });
        }else{
            tab_adapter.addFragment(home_fragment, "");
            tab_view_pager.setAdapter(tab_adapter);
            endLoading();
        }
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        this_uni_domain = sharedPreferences.getString("domain", "");
    }


    private void bubbleBarSetup(){
        bubble_arraylist.add("University");
        bubble_arraylist.add("Events");
        bubble_arraylist.add("Posts");
        bubble_arraylist.add("For You");
        bubble_arraylist.add("Clubs");
        bubble_arraylist.add("University of Calgary Ski and Board Club");
        bubble_arraylist.add("Social");
        bubble_arraylist.add("Grind");

        bubble_recycler_view = findViewById(R.id.bubble_sample_rv);
        bubble_recycler_view.setHasFixedSize(true);

        bubble_layout_manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bubble_adapter = new BubbleAdapter(bubble_arraylist, new BubbleAdapter.BubbleViewHolder.BubbleClickListener() {
            @Override
            public void onBubbleClick(int position) {




                Fragment selectedFragment = null;
                switch (position){

                    case 0:
                        if (hf == null){
                            hf = HomeFragment.newInstance(MainActivity.this);
                        }
                        selectedFragment = hf;


                        break;
                    case 1:
                        if(ef == null){
                            ef = EventsFragment.newInstance(MainActivity.this);
                        }
                        selectedFragment = ef;

                        break;
                    case 2:
                        if(pf == null)
                            pf = PostsFragment.newInstance(MainActivity.this);
                        selectedFragment = pf;
                }
                if (selectedFragment!= null) getSupportFragmentManager().beginTransaction().replace(R.id.main_fragmentContainer, selectedFragment).commit();


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
