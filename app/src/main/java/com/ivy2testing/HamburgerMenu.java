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



    private Button addBtn;
    private ConstraintLayout constLayout;
    private LinearLayout bblContainer;
    private Button Ucalgary;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);

        //testing some bs
        addBtn = findViewById(R.id.button2);
        constLayout = findViewById(R.id.constraintlayout);
        bblContainer = findViewById(R.id.bubble_container);
        Ucalgary = findViewById(R.id.button);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // this is required to nullify title, title will be sent to blank character in xml too
        getSupportActionBar().setTitle(null);
        //set color of draw bar
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.interaction));
        final Button newButton = new Button(this);
        newButton.setText("Hey Im new around here");



        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HamburgerMenu.this, "suuup homeslice", Toast.LENGTH_SHORT).show();

                //bblContainer.addView(newButton);
                buttonCreator();
            }
        });
        Ucalgary.setTextColor(getResources().getColor(R.color.white));
        Ucalgary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HamburgerMenu.this, "UCALGARYY", Toast.LENGTH_SHORT).show();
                Ucalgary.setEnabled(false);
                Ucalgary.setTextColor(getResources().getColor(R.color.interaction));
            }
        });

    }
    private void buttonCreator(){
        ContextThemeWrapper newContext = new ContextThemeWrapper(getBaseContext(), R.style.Bubble);
        Button newButton2 = new Button(newContext);
        bblContainer.addView(newButton2);
        newButton2.setText("Hey Im new around here2");
        bblContainer.addView(newButton2);


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
