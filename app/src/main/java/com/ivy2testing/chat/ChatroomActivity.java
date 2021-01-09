package com.ivy2testing.chat;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Chatroom;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.Utils;

/**
 * Container activity for main messaging fragment
 * Used for private Messaging only
 * Contains private chatroom actions in navigation drawer
 */
public class ChatroomActivity extends AppCompatActivity {
    private static final String TAG = "ChatRoomActivity";

    // Views
    private DrawerLayout drawer;

    // Firebase
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    // Other Values
    private Chatroom this_chatroom;
    private User this_user;

    /* Overridden Methods
     ***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        getIntentExtras();

        if (this_user != null && this_chatroom != null){
            initViews();
            initNavDrawer();
            setFragment();
        } else Log.e(TAG, "A parcel was null!");
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END))
            drawer.closeDrawer(GravityCompat.END);
        else super.onBackPressed();
    }

    /* Initialization Methods
     ***************************************************************************************************/

    // Get User object passed from login Activity
    private void getIntentExtras(){
        if (getIntent() != null){
            this_user = getIntent().getParcelableExtra("this_user");
            this_chatroom = getIntent().getParcelableExtra("chatroom");
        }
    }

    private void initViews(){
        // Views
        drawer = findViewById(R.id.room_drawerLayout);

        // Set Room Title
        if (this_chatroom.getMembers().size() > 0 && !this_user.getId().equals(this_chatroom.getMembers().get(0)))
            setTitle(this_chatroom.getMembers().get(0));
        else if (this_chatroom.getMembers().size() > 1)
            setTitle(this_chatroom.getMembers().get(1));
        else setTitle("Chatroom"); // Shouldn't happen!
    }

    // Initialize Navigation Drawer: set colours, listener, and hide some items if necessary
    private void initNavDrawer(){
        NavigationView nav = findViewById(R.id.room_navView);

        // Navigation Drawer:
        Menu nav_Menu = nav.getMenu();
        nav.setItemIconTintList(null);  // Set colours
        Utils.colorMenuItem(nav_Menu.findItem(R.id.roomNavOptions_delete), getColor(R.color.red));

        // Drawer item selection
        nav.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    // Set main messaging fragment
    private void setFragment(){
        Fragment frag = new MessagingFragment(this_chatroom, this_user);
        getSupportFragmentManager().beginTransaction().replace(R.id.room_frameLayout, frag).commit();
    }


/* OnClick Methods
***************************************************************************************************/

    public void openOptions(View view) {
        if (drawer.isDrawerOpen(GravityCompat.END))
            drawer.closeDrawer(GravityCompat.END);
        else drawer.openDrawer(GravityCompat.END);

    }

    public void returnToLobby(View view) {
        if (isTaskRoot()) { // Got here from a notification
            Intent intent = new Intent(this, ChatFragment.class);
            intent.putExtra("this_user", this_user);
            startActivity(intent);
        }
        else { // Got here from lobby
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    // OnClick listener for drawer navigation items
    private boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.roomNavOptions_profile:
                viewUserProfile();
                break;

            case R.id.roomNavOptions_delete:
                deleteChatRoom();
                break;
        }
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }


    /* Chatroom Navigation Options
     ***************************************************************************************************/

    // Delete Chatroom completely and return to Lobby
    private void deleteChatRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.deleteRoom_title))
                .setMessage(getString(R.string.deleteRoom_message))
                .setPositiveButton("Confirm", (dialog, which) ->
                        mFirestore.collection("conversations").document(this_chatroom.getId())
                                .delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(this, getString(R.string.deleteRoom), Toast.LENGTH_SHORT).show();
                                returnToLobby(drawer);
                            } else {
                                Toast.makeText(this, getString(R.string.error_deleteRoom), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.error_deleteRoom), task.getException());
                            }
                        }))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // TODO when integrated to main App
    private void viewUserProfile() {
        Log.w(TAG, "View user Profile WIP");
    }
}
