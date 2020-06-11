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

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.home.CreatePost;
import com.ivy2testing.R;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.entities.Student;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.userProfile.StudentProfileFragment;
import com.ivy2testing.util.FragCommunicator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements FragCommunicator {

    // Constants
    private final static String TAG = "MainActivity";

    // Hamburger menu
    private DrawerLayout drawer;

    private BottomNavigationView mBottomNav;
    private FrameLayout mFrameLayout;
    private FrameLayout loadingLayout;

    private Button post_button;

    // Firebase
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private boolean is_organization = false;
    private Student mStudent;       //TODO need abstract class User?
    private Uri profileImgUri;
    private String this_user_id;
    private String this_uni_domain;


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
        //attemptAutoLogin();

        // Set up home fragment
        Fragment selectedFragment = new HomeFragment(MainActivity.this, mStudent);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragmentContainer, selectedFragment).commit();

        // this method is kept so people can swap fragments when need be
        // fragmentHandler();

    }

    private void setToolBar() {
        // toolbar is an <include> in xml, referencing the file main_toolbar.xml
        Toolbar main_toolbar = findViewById(R.id.main_toolbar_id);

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
        mBottomNav.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.VISIBLE);
        mBottomNav.setSelectedItemId(R.id.tab_bar_home);
        setNavigationListener();
        endLoading();
    }

    // Disable bottom Navigation for a logged-in user
    private void setLoggedOutDisplay(){
        mBottomNav.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
        endLoading();
    }

    private void setNavigationListener() {
        // Test bottom navigation
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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
        mBottomNav = findViewById(R.id.main_tab_bar);
        loadingLayout = findViewById(R.id.main_loadingScreen);
        post_button = findViewById(R.id.post_button);
        mFrameLayout = findViewById(R.id.main_fragmentContainer);
    }

    /* ************************************************************************************************** */
    // setting listeners on the post icon, and disables current button

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
        else if (obj instanceof Organization){
            Log.e(TAG, "Oh no! Organization view not implemented yet!");
        }
        else if (obj instanceof Uri) {
            profileImgUri = (Uri) obj;
            Log.d(TAG, "Image Uri got updated!");
        }
        else if(obj instanceof Boolean) {
            is_organization = (Boolean) obj;
            Log.d(TAG, "Is_Organization updated to: " + is_organization);
        }

        return null;
    }

    @Override
    public void mapMessage(Map<Object, Object> map) {
        if (map.get("this_user_id") != null && map.get("this_uni_domain") != null) {
            this_user_id = Objects.requireNonNull(map.get("this_user_id")).toString();
            this_uni_domain = Objects.requireNonNull(map.get("this_uni_domain")).toString();
            getUserInfo();
        }
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

    // Auto Login
    /* ************************************************************************************************** */

    // Check to see if we still have user's Firebase Auth token if we do, attempt login
    private void attemptAutoLogin() {
        startLoading();     // Loading Animation overlay

        FirebaseUser user = auth.getCurrentUser();
        this_user_id = auth.getUid();
        if (user != null && this_user_id != null && user.isEmailVerified()) {
            loadPreferences();
            if (!this_uni_domain.equals("")) {
                Log.d(TAG, "autologin User: " + user.getUid());
                getUserInfo();
            } else {
                Log.d(TAG,"Couldn't perform auto-login.");
                setLoggedOutDisplay();
            }
        }
    }

    // Load the university domain for auto login
    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        this_uni_domain = sharedPreferences.getString("domain", "");
    }


    /* Firebase Methods
     ***************************************************************************************************/

    // Get all student info and return student class
    public void getUserInfo(){
        startLoading();     // Display loading screen
        String address = "universities/" + this_uni_domain + "/users/" + this_user_id;
        if (address.contains("null")){
            Log.e(TAG, "User Address has null values.");
            return;
        }

        db_reference.document(address).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc == null){
                        Log.e(TAG, "Document doesn't exist");
                        return;
                    }

                    // what TODO if field doesn't exist?
                    if (doc.get("is_organization") == null){
                        Log.e(TAG, "getUserInfo: 'is_organization' field doesn't exist");
                        mStudent = doc.toObject(Student.class);
                        if (mStudent == null) Log.e(TAG, "Student object obtained from database is null!");
                        else {
                            mStudent.setId(doc.getId());
                            setLoggedInDisplay();   // Logged in!
                        }

                        return;
                    }

                    // Student or Organization?
                    is_organization = (boolean) doc.get("is_organization");
                    if (is_organization){
                        //TODO is organization
                        Log.d(TAG, "User is an organization!");
                        setLoggedInDisplay();   // Continue to rest of App TODO change to org view
                    }
                    else {
                        mStudent = doc.toObject(Student.class);
                        if (mStudent == null) Log.e(TAG, "Student object obtained from database is null!");
                        else mStudent.setId(doc.getId());
                        getStudentPic();
                    }
                }
                else Log.e(TAG,"getUserInfo: unsuccessful!");
            }
        });
    }

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getStudentPic() {

        // Make sure student has a profile image already
        if (mStudent.getProfile_picture() != null){
            db_storage.child(mStudent.getProfile_picture()).getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                profileImgUri = task.getResult();
                            }
                            else {
                                Log.w(TAG, task.getException());
                                mStudent.setProfile_picture(""); // image doesn't exist
                            }
                            setLoggedInDisplay(); // Logged In!
                        }
                    });
        }
        else setLoggedInDisplay(); // Logged In with no pic!
    }
}
