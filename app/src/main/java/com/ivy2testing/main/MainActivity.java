package com.ivy2testing.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.home.CreatePost;
import com.ivy2testing.R;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.entities.Student;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.userProfile.StudentProfileFragment;
import com.ivy2testing.util.FragCommunicator;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements FragCommunicator {

    // Constants
    private final static String TAG = "MainActivity";

    // Hamburger menu
    private DrawerLayout drawer;
    private Toolbar main_toolbar;


    private BottomNavigationView mainTabBar;
    private FrameLayout mFrameLayout;
    private FrameLayout loadingLayout;


    private Button post_button;

    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private boolean is_organization = false;
    private Student mStudent;       //TODO need abstract class User?
    private Uri profileImgUri;


    // On create
    /* ************************************************************************************************** */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Up Top Navigation Toolbar
        setToolBar();

        setHandlers();
        setListeners();
        startLoading();     // Loading Animation overlay

        // this method is kept so people can swap fragments when need be
        // fragmentHandler();

    }

    private void setToolBar() {
        // toolbar is an <include> in xml, referencing the file main_toolbar.xml
        main_toolbar = findViewById(R.id.main_toolbar_id);

        // setting support action bar builds the toolbar (required for a hamburger menu)
        setSupportActionBar(main_toolbar);

        // activity_main is a drawer layout/ the drawer drawn off screen
        drawer = findViewById(R.id.main_layout_drawer);

        // toggle is the hamburger menu itself
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, main_toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // this is required to nullify title, title is set to empty character in xml too but still writes
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(null);

        // set color of hamburger menu
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.interaction));
    }

    /* ************************************************************************************************** */

    // these methods close the drawer before calling another back press
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
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof StudentProfileFragment) {
            StudentProfileFragment profileFragment = (StudentProfileFragment) fragment;
            profileFragment.setCommunicator(this);
        }
        else if (fragment instanceof HomeFragment) {
            HomeFragment profileFragment = (HomeFragment) fragment;
            profileFragment.setCommunicator(this);
        }
    }

    // Enable bottom Navigation for a logged-in user
    private void setLoggedInDisplay(){
        mainTabBar.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.VISIBLE);
        mainTabBar.setSelectedItemId(R.id.tab_bar_home);
        endLoading();
    }

    // Disable bottom Navigation for a logged-in user
    private void setLoggedOutDisplay(){
        mainTabBar.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
        endLoading();
    }

    private void setNavigationListener() {
        // Test bottom navigation
        mainTabBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()){
                    case R.id.tab_bar_chat:
                        selectedFragment = new ChatFragment();
                        break;
                    case R.id.tab_bar_home:
                        selectedFragment = new HomeFragment(MainActivity.this, mStudent);
                        break;
                    case R.id.tab_bar_profile:
                        if (is_organization) Log.w(TAG, "Organization Profile View under construction.");
                        else selectedFragment = new StudentProfileFragment(MainActivity.this, mStudent, profileImgUri);
                        break;
                }
                if (selectedFragment!= null)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragmentContainer, selectedFragment).commit();

                return true;
            }
        });
    }

    /* ************************************************************************************************** */
    // grabbing views and setting current_button to handle clicks on bubbles
    private void setHandlers(){
        mainTabBar = findViewById(R.id.main_tab_bar);
        loadingLayout = findViewById(R.id.main_loadingScreen);
        post_button = findViewById(R.id.post_button);
    }

    /* ************************************************************************************************** */
    // setting listeners on the post icon, and disables current button

    // Go to login screen
    public void mainLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // TEST For testing purposes only!
    public void mainTest(View view) {
        Intent intent = new Intent(this, com.ivy2testing.main.MainActivity.class);
        intent.putExtra("this_uni_domain","ucalgary.ca");
        intent.putExtra("this_user_id", "testID");
        intent.putExtra("isStudent", true);
        finish();
        startActivity(intent);
    }

    private void setListeners(){
        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreatePost.class);
                startActivity(intent);
            }
        });
    }

    /* Transition Methods
     ***************************************************************************************************/


    // Set loading page animation
    private void startLoading(){
        loadingLayout.setVisibility(View.VISIBLE);      // Bring up view to cover entire screen
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_loadingScreen, new LoadingPageFragment(this)).commit();      // Populate View with loading page layout
    }

    // Fade out loading page animation
    private void endLoading(){
        loadingLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));  // Start fade out animation
        loadingLayout.setVisibility(View.GONE);
    }


    /* Interface Methods
     ***************************************************************************************************/

    // Frag Communicator
    @Override
    public Object message(Object obj) {
        if (obj instanceof Student) {
            mStudent = (Student) obj;   // Update student user
            Log.d(TAG, "Student " + mStudent.getName() + " got updated!");
        }
        else if (obj instanceof String) {
            switch (obj.toString()){
                case "loggedIn":
                    setLoggedInDisplay();
                    Log.d(TAG, "Display got updated!");
                    break;
                case "loggedOut":
                    setLoggedOutDisplay();
                    Log.d(TAG, "Display got updated!");
                    break;
                default:
                    Log.e(TAG, "Did not recognize the String " + obj.toString());
            }
        }
        else if (obj instanceof Uri) {
            profileImgUri = (Uri) obj;
            Log.d(TAG, "Image Uri got updated!");
        }
        return null;
    }



    /* ************************************************************************************************** */
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
