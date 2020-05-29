package com.ivy2testing.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
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

    // Other Variables
    private String this_uni_domain;
    private String this_user_id;
    private boolean isStudent;
    private int returning_fragId;
    private Student mStudent;


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
                        selectedFragment = new StudentProfileFragment(MainActivity.this, mStudent);
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

        if(getIntent() != null){
            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            this_user_id = getIntent().getStringExtra("this_user_id");
            isStudent = getIntent().getBooleanExtra("isStudent",true);
            returning_fragId = getIntent().getIntExtra("returning_fragId", R.id.tab_bar_home);

            if(this_uni_domain == null || this_user_id == null){
                Log.w(TAG,"Not signed in yet!");
                setLoggedOutDisplay();
            }
            else {
                if (isStudent) getStudentInfo();
                // TODO else organization sign in
            }
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
    public void getStudentInfo(){

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
                    mStudent = doc.toObject(Student.class);
                    if (mStudent == null) Log.e(TAG, "Student object obtained from database is null!");
                    else mStudent.setId(this_user_id);
                    setNavigationListener();
                    setLoggedInDisplay();
                }
                else Log.e(TAG,"getStudentInfo: unsuccessful!");
            }
        });
    }
}
