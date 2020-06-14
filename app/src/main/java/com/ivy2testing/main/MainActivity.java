package com.ivy2testing.main;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.home.CreatePost;
import com.ivy2testing.R;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.entities.Student;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.userProfile.StudentProfileFragment;

public class MainActivity extends AppCompatActivity {




    // MARK: Variables and Constants

    private final static String TAG = "MainActivity";
    private DrawerLayout drawer;
    private BottomNavigationView bottom_navigation;
    private FrameLayout loading_layout;
    private ImageButton post_button;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private boolean is_organization = false;
    private String this_uni_domain;
    private UserViewModel user_view_model;
    private Student this_student;
    private Organization this_organization;









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












    // MARK: Setup Methods

    private void setUp(){
        setUpToolbar();
        setHandlers();
        attemptLogin();
    }

    private void setHandlers(){
        bottom_navigation = findViewById(R.id.main_tab_bar);
        bottom_navigation.setSelectedItemId(R.id.tab_bar_home);
        loading_layout = findViewById(R.id.main_loadingScreen);
        post_button = findViewById(R.id.post_button);
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

    private void setUpLoggedInInteraction() { //this method will set up all the interactive elements the user has access to when logged in, by default they're hidden (tab bar + post btn)
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragmentContainer, new HomeFragment(this)).commit();
        post_button.setVisibility(View.VISIBLE);
        post_button.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CreatePost.class);
            startActivity(intent);
        });
        bottom_navigation.setVisibility(View.VISIBLE);
        bottom_navigation.setOnNavigationItemSelectedListener((menuItem) -> {
            Fragment selectedFragment = null;
            switch (menuItem.getItemId()){
                //TODO: making a new fragment each time is inefficient
                case R.id.tab_bar_chat:
                    selectedFragment = new ChatFragment();
                    break;
                case R.id.tab_bar_home:
                    selectedFragment = new HomeFragment(this);
                    break;
                case R.id.tab_bar_profile:
                    selectedFragment = new StudentProfileFragment(true);
                    break;
            }
            if (selectedFragment!= null) getSupportFragmentManager().beginTransaction().replace(R.id.main_fragmentContainer, selectedFragment).commit();
            return true;
        });
    }









    // MARK: Base Methods

    private void attemptLogin(){
        startLoading();
        loadPreferences();
        user_view_model = new ViewModelProvider(this).get(UserViewModel.class);
        if(auth.getCurrentUser() != null && auth.getUid() != null && this_uni_domain != null){
            user_view_model.startListening(auth.getUid(), this_uni_domain);
            if(user_view_model.isOrganization()){
                user_view_model.getThisOrganization().observe(this, (Organization updatedUser) -> {
                    if(updatedUser != null){
                        //TODO: deal with banning, age update, notifications, etc.
                        is_organization = true;
                        this_organization = updatedUser;
                        setUpLoggedInInteraction();
                        endLoading();
                    }
                });
            }else{
                user_view_model.getThisStudent().observe(this, (Student updatedUser) -> {
                    if(updatedUser != null){
                        //TODO: deal with banning, age update, notifications, etc.
                        is_organization = false;
                        this_student = updatedUser;
                        setUpLoggedInInteraction();
                        endLoading();
                    }
                });
            }
        }else{
            //TODO: set up login button (either ham menu or instead of create post)
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragmentContainer, new HomeFragment(this)).commit();
            endLoading();
        }
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        this_uni_domain = sharedPreferences.getString("domain", "");
    }










    // MARK: Other Methods

    private void startLoading(){
        loading_layout.setVisibility(View.VISIBLE);      // Bring up view to cover entire screen
        getSupportFragmentManager().beginTransaction().replace(R.id.main_loadingScreen, new LoadingPageFragment(this)).commit();      // Populate View with loading page layout
    }

    // Fade out loading page animation
    private void endLoading(){
        loading_layout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));  // Start fade out animation
        loading_layout.setVisibility(View.GONE);
    }















    // MARK: Deprecated, wasn't sure if could be deleted

    // this handler will allow another fragment to be placed in the fragment container
    // the container should be set to a frame layout to allow fragments on top of eachother
    // it is set to a scrollview atm, but we don't call fragments yet

    private void fragmentHandler() {
        MainFragment mf = MainFragment.newInstance();
        //Get the Fragment Manager and start a transaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // add the fragment
        // this code here allows info from other fragments to be added to the back stack
        // pressing back will reload previous fragments, I don't think we need this for ivy, + how far back should they stack?
        fragmentTransaction.add(R.id.main_fragmentContainer, mf).addToBackStack(null).commit();
        closeFragment();
    }

    // Close format will close the previous fragment
    private void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Check to see if a fragment is already showing.
        MainFragment mf = (MainFragment) fragmentManager.findFragmentById(R.id.main_fragmentContainer);
        if (mf != null) {
            //create and commit the transaction to remove the fragment
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // i believe replace allows the previous fragment to be saved in memory, while swapping views..
            //fragmentTransaction.replace(R.id.fragment_container, sf);

            //close
            fragmentTransaction.remove(mf).commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }
}
