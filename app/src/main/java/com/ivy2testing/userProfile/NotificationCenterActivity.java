package com.ivy2testing.userProfile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Notification;
import com.ivy2testing.entities.User;
import com.ivy2testing.hometab.ViewPostOrEventActivity;
import com.ivy2testing.main.MainActivity;

public class NotificationCenterActivity extends AppCompatActivity implements NotificationCenterAdapter.NotificationListener{



    private static final String TAG = "NotificationCenterActivityTag";
    private RecyclerView notification_recycler;
    private ProgressBar progress_bar;
    private NotificationCenterAdapter notification_adapter;
    private User this_user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);
        setUp();
    }

    private void setUp(){
        setTitle("Notification Center");
        this_user = getIntent().getParcelableExtra("this_user");
        if(this_user == null){
            Toast.makeText(this, "Couldn't get user data :-(.", Toast.LENGTH_LONG).show();
            finish();
        }
        notification_recycler = findViewById(R.id.activity_notification_center_recycler);
        progress_bar = findViewById(R.id.activity_notification_center_progress_bar);
        if(this_user != null){
            notification_adapter = new NotificationCenterAdapter(this_user.getId(), this, this, findViewById(R.id.activity_notification_center_no_notifs), progress_bar, notification_recycler);
            notification_recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            notification_recycler.setAdapter(notification_adapter);
        }
    }

    @Override
    public void onNotificationClick(int position) {
        Notification clicked = notification_adapter.getNotification(position);
        Intent intent = new Intent();
        switch(clicked.getType()){
            case 1:
                intent = new Intent(this, MainActivity.class);
                break;
            case 3:
                intent = new Intent(this, MainActivity.class);
                break;
            default:
                intent = new Intent(this, ViewPostOrEventActivity.class);
                intent.putExtra("post_uni",  this_user.getUni_domain());
                intent.putExtra("post_id", clicked.getNotification_origin_id());
                intent.putExtra("this_user",this_user);
                break;
        }
        startActivity(intent);

    }
}
