package com.ivy2testing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.userProfile.UserProfileFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView mainTabBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        // just immediately jumping into the student sign up page to test it
//        Intent intent = new Intent(this, StudentSignUpActivity.class);
//        startActivity(intent);

        // Test bottom navigation
        mainTabBar = findViewById(R.id.main_tab_bar);
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
                        selectedFragment = new UserProfileFragment(MainActivity.this);
                        break;
                }
                if (selectedFragment!= null)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragmentContainer, selectedFragment).commit();

                return true;
            }
        });
    }
}
