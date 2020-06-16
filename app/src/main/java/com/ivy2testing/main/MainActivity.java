package com.ivy2testing.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.home.CreatePost;
import com.ivy2testing.R;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.entities.Student;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.userProfile.StudentProfileFragment;
import com.ivy2testing.util.Constant;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {




    // MARK: Variables and Constants

    private final static int LOGIN_CODE = 1;

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
        post_button.setOnClickListener(view -> transToLogin());
    }

    private void setUpLoggedInInteraction() { //this method will set up all the interactive elements the user has access to when logged in, by default they're hidden (tab bar + post btn)
        post_button.setImageResource(R.drawable.ic_create);
        post_button.setOnClickListener(view -> transToCreatePost());
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

        // Set home view if no fragments visible atm
        if (getSupportFragmentManager().findFragmentById(R.id.main_fragmentContainer) == null)
            bottom_navigation.setSelectedItemId(R.id.tab_bar_home);
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

    private void transToCreatePost(){
        Intent intent = new Intent(getApplicationContext(), CreatePost.class);
        startActivity(intent);
    }

    private void transToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_CODE);
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
