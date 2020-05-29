package com.ivy2testing;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class HamburgerMenu extends AppCompatActivity {
    private DrawerLayout drawer;

    private Button currentButton;
    private Button button_1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);

        //testing some buttons
        button_1 = findViewById(R.id.btn_1);
        currentButton = button_1;
        currentButton.setEnabled(false);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // this is required to nullify title, title will be sent to blank character in xml too
        getSupportActionBar().setTitle(null);
        //set color of draw bar
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.interaction));

    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }


    public void toggleEnabled(View view) {
        currentButton.setEnabled(true);
        view.setEnabled(false);
        currentButton = (Button) view;
        Toast.makeText(this, ""+currentButton.getText(), Toast.LENGTH_SHORT).show();
    }
}
