package com.ivy2testing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.userProfile.Student;
import com.ivy2testing.userProfile.StudentProfileFragment;

public class MainActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "MainActivity";

    // Views
    private BottomNavigationView mainTabBar;

    // FireBase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Other Variables
    private String this_uni_domain;
    private String this_user_id;
    private boolean isStudent;
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
    }

    // Enable bottom Navigation for a logged-in user
    private void setLoggedInDisplay(){
        findViewById(R.id.main_loginButton).setVisibility(View.GONE);
        findViewById(R.id.main_testButton).setVisibility(View.GONE);
        mainTabBar.setSelectedItemId(R.id.tab_bar_home);
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
    private void chooseDisplay(){
        if(getIntent() != null){
            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            this_user_id = getIntent().getStringExtra("this_user_id");
            isStudent = getIntent().getBooleanExtra("isStudent",true);

            if(this_uni_domain == null || this_user_id == null){
                Log.w(TAG,"Not signed in yet!");
                mainTabBar.setVisibility(View.GONE);
            }
            else {
                // Loading screen?
                if (isStudent) getStudentInfo();
                // else organization sign in
            }
        }
    }

/* Firebase Methods
***************************************************************************************************/

    // Get all student info and return student class TODO
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
                    setNavigationListener();
                    setLoggedInDisplay();
                }
                else Log.e(TAG,"getStudentInfo: unsuccessful!");
            }
        });
    }
}
