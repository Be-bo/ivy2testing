package com.ivy2testing.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.entities.Student;
import com.ivy2testing.userProfile.StudentProfileFragment;

public class MainActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "MainActivity";

    // Views
    private BottomNavigationView mainTabBar;
    private FrameLayout mFrameLayout;
    private FrameLayout loadingLayout;
    private Button mainLoginButton;
    private Button mainTestButton;

    // FireBase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private String this_uni_domain;
    private String this_user_id;
    private boolean is_organization = false;
    private int returning_fragId;
    private Student mStudent;       //TODO need abstract class User?
    private Uri profileImgUri;


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        declareViews();
        chooseDisplay();       // Which display to use depending on if user is signed in or nah
    }


/* Initialization Methods
***************************************************************************************************/

    private void declareViews(){
        mainTabBar = findViewById(R.id.main_tab_bar);
        loadingLayout = findViewById(R.id.main_loadingScreen);
        mFrameLayout = findViewById(R.id.main_fragmentContainer);
        mainLoginButton = findViewById(R.id.main_loginButton);
        mainTestButton = findViewById(R.id.main_testButton);
    }

    // Enable bottom Navigation for a logged-in user
    private void setLoggedInDisplay(){
        mainLoginButton.setVisibility(View.GONE);
        mainTestButton.setVisibility(View.GONE);
        mainTabBar.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.VISIBLE);
        mainTabBar.setSelectedItemId(returning_fragId);
        endLoading();
    }

    // Disable bottom Navigation for a logged-in user
    private void setLoggedOutDisplay(){
        mainLoginButton.setVisibility(View.VISIBLE);
        mainTestButton.setVisibility(View.VISIBLE);
        mainTabBar.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.GONE);
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
                        selectedFragment = new HomeFragment();
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

/* OnClick Methods
***************************************************************************************************/

    // Go to login screen
    public void mainLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // TEST For testing purposes only!
    public void mainTest(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("this_uni_domain","ucalgary.ca");
        intent.putExtra("this_user_id", "testID");
        intent.putExtra("isStudent", true);
        finish();
        startActivity(intent);
    }


/* Transition Methods
***************************************************************************************************/

    // Get intent extras and see if logged in
    private void chooseDisplay(){

        startLoading();     // Loading Animation overlay

        if (getIntent() != null){
            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            this_user_id = getIntent().getStringExtra("this_user_id");
            returning_fragId = getIntent().getIntExtra("returning_fragId", R.id.tab_bar_home);

            if (this_uni_domain == null || this_user_id == null){
                Log.w(TAG,"Not signed in yet!");
                setLoggedOutDisplay();
            }
            else getUserInfo();
        }
    }

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

/* Firebase Methods
***************************************************************************************************/

    // Get all student info and return student class
    public void getUserInfo(){

        db.collection("universities").document(this_uni_domain).collection("users").document(this_user_id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                        else mStudent.setId(this_user_id);
                        // Continue to rest of App
                        setNavigationListener();
                        setLoggedInDisplay();
                        return;
                    }

                    // Student or Organization?
                    is_organization = (boolean) doc.get("is_organization");
                    if (is_organization){
                        //TODO is organization
                        Log.d(TAG, "User is an organization!");
                        // Continue to rest of App
                        setNavigationListener();
                        setLoggedInDisplay();
                    }
                    else {
                        mStudent = doc.toObject(Student.class);
                        if (mStudent == null) Log.e(TAG, "Student object obtained from database is null!");
                        else mStudent.setId(this_user_id);
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
            base_storage_ref.child(mStudent.getProfile_picture()).getDownloadUrl()
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
                            // Continue to rest of App
                            setNavigationListener();
                            setLoggedInDisplay();
                        }
                    });
        }
        else {
            // Continue to rest of App
            setNavigationListener();
            setLoggedInDisplay();
        }
    }
}
