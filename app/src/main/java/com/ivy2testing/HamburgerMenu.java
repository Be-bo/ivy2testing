package com.ivy2testing;

import android.content.Context;
import android.os.Bundle;
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
    private Button button_2;
    private Button button_3;
    private Button button_4;
    private Button button_5;
    private Button button_6;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);

        //testing some buttons
        button_1 = findViewById(R.id.btn_1);
        button_1.setEnabled(false);
        currentButton = button_1;
        button_1.setTextColor(getResources().getColor(R.color.android_default_bg));
        button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButton(button_1);
            }
        });
        button_2 = findViewById(R.id.btn_2);
        button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButton(button_2);
            }
        });
        button_3 = findViewById(R.id.btn_3);
        button_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButton(button_3);
            }
        });
        button_4 = findViewById(R.id.btn_4);
        button_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButton(button_4);
            }
        });
        button_5 = findViewById(R.id.btn_5);
        button_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButton(button_5);
            }
        });
        button_6 = findViewById(R.id.btn_6);
        button_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButton(button_6);
            }
        });






        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // this is required to nullify title, title will be sent to blank character in xml too
        getSupportActionBar().setTitle(null);
        //set color of draw bar
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.interaction));

    }
    public void updateButton(Button b) {
        currentButton.setEnabled(true);
        currentButton.setTextColor(getResources().getColor(R.color.interaction));
        b.setEnabled(false);
        b.setTextColor(getResources().getColor(R.color.android_default_bg));
        currentButton = b;
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

}
