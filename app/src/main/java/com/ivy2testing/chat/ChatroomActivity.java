package com.ivy2testing.chat;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Chatroom;
import com.ivy2testing.entities.User;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.Utils;

import java.util.ArrayList;

/**
 * Container activity for main messaging fragment
 * Used for private Messaging only
 * Contains private chatroom actions in navigation drawer
 */
public class ChatroomActivity extends AppCompatActivity {
    private static final String TAG = "ChatRoomActivity";

    // Views
    private DrawerLayout drawer;
    private MessagingFragment messagingFrag;

    // Firebase
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    // Other Values
    private Chatroom this_chatroom;
    private User this_user;
    private User partner;           // Could be null!!!

    /* Overridden Methods
     ***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        getIntentExtras();

        if (this_user != null && this_chatroom != null){
            setFragment();
            initViews();
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
            partner = getIntent().getParcelableExtra("partner");
        }
    }

    private void initViews(){

        // Set Room Title
        if (partner != null) setTitle(partner.getName());
        else setTitle("Chatroom"); // Shouldn't happen!

        // Views
        drawer = findViewById(R.id.room_drawerLayout);
        NavigationView nav = findViewById(R.id.room_navView);
        //((TextView)findViewById(R.id.chatroom_ham_title)).setText(partner.getName());

        // Navigation Drawer:
        Menu nav_Menu = nav.getMenu();
        nav.setItemIconTintList(null);  // Set colours
        Utils.colorMenuItem(nav_Menu.findItem(R.id.roomNavOptions_delete), getColor(R.color.red));

        // Drawer item selection
        nav.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    // Set main messaging fragment
    private void setFragment(){
        messagingFrag = new MessagingFragment(this_chatroom, this_user, partner);
        getSupportFragmentManager().beginTransaction().replace(R.id.room_frameLayout, messagingFrag).commit();
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatroom_nav_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.chatroom_open_nav)
            openOptions();
        return super.onOptionsItemSelected(item);
    }


/* OnClick Methods
***************************************************************************************************/

    public void openOptions() {
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


    // View User Profile
    private void viewUserProfile() {
        Intent intent;
        if (partner.getIs_organization()) {
            Log.d(TAG, "Starting OrganizationProfile Activity for organization " + partner.getId());
            intent = new Intent(this, OrganizationProfileActivity.class);
            intent.putExtra("org_to_display_id",  partner.getId());
            intent.putExtra("org_to_display_uni", partner.getUni_domain());
        } else {
            Log.d(TAG, "Starting StudentProfile Activity for student " +  partner.getId());
            intent = new Intent(this, StudentProfileActivity.class);
            intent.putExtra("student_to_display",  partner);
        }
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }


    // Confirmation dialog
    private void deleteChatRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.deleteRoom_title))
                .setMessage(getString(R.string.deleteRoom_message))
                .setPositiveButton("Confirm", (dialog, which) -> deleteChatRoomFromDB())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    // Delete Chatroom completely and return to Lobby
    private void deleteChatRoomFromDB() {

        // Remove from messaging list
        mFirestore.document(User.getPath(this_user.getId()))
            .update("messaging_users", FieldValue.arrayRemove(partner.getId()));

        // Remove user from chatroom members. if empty list -> delete document
        DocumentReference chatroomDoc = mFirestore.collection("conversations").document(this_chatroom.getId());
        chatroomDoc.update("members", FieldValue.arrayRemove(this_user.getId()))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatroomDoc.get().addOnCompleteListener( task1 -> {
                            if (task1.isSuccessful() && task1.getResult() != null) {

                                // Delete if no members left, else do nothing
                                Chatroom updatedRoom = task1.getResult().toObject(Chatroom.class);
                                if (updatedRoom != null && updatedRoom.getMembers().isEmpty()) {
                                    chatroomDoc.delete().addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            Toast.makeText(this, getString(R.string.deleteRoom), Toast.LENGTH_SHORT).show();
                                            returnToLobby(drawer);
                                        } else {
                                            Toast.makeText(this, getString(R.string.error_deleteRoom), Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, getString(R.string.error_deleteRoom), task2.getException());
                                        }
                                    });
                                } else {
                                    Toast.makeText(this, getString(R.string.deleteRoom), Toast.LENGTH_SHORT).show();
                                    returnToLobby(drawer);
                                }

                            } else {
                                Toast.makeText(this, getString(R.string.error_deleteRoom), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Couldn't retrieve updated chatroom", task1.getException());
                            }
                        });
                    } else {
                        Toast.makeText(this, getString(R.string.error_deleteRoom), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Couldn't remove member from chatroom.", task.getException());
                    }
                });
    }
}
