package com.ivy2testing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.userProfile.StudentProfileFragment;

public class MainActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "MainActivity";

    // Views
    private BottomNavigationView mainTabBar;

    // Other Variables
    private String this_uni_domain;
    private String this_user_id;


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        declareViews();
        chooseDisplay();       // Which display to use depending on if user is signed in or nah
        setNavigationListener();
    }


/* Initialization Methods
***************************************************************************************************/

    private void declareViews(){
        mainTabBar = findViewById(R.id.main_tab_bar);
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
                        selectedFragment = new StudentProfileFragment(MainActivity.this, this_user_id);
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

    public void mainTest(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("this_uni_domain","ucalgary.ca");
        intent.putExtra("this_user_id", "1ltpu347OQd0MxEtvEfVSO5HpqT2");
        finish();
        startActivity(intent);
    }


/* Transition Methods
***************************************************************************************************/
    private void chooseDisplay(){
        if(getIntent() != null){
            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            this_user_id = getIntent().getStringExtra("this_user_id");

            if(this_uni_domain == null || this_user_id == null){
                Log.w(TAG,"Not signed in yet!");
                mainTabBar.setVisibility(View.GONE);
            } else {
                findViewById(R.id.main_loginButton).setVisibility(View.GONE);
                findViewById(R.id.main_testButton).setVisibility(View.GONE);
            }
        }
    }
}
