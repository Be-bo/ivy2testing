package com.ivy2testing.userProfile;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;
import com.ivy2testing.entities.User;

public class NotificationCenterActivity extends AppCompatActivity implements NotificationCenterAdapter.NotificationListener{

    private RecyclerView notification_recycler;
    private NotificationCenterAdapter notification_adapter;
    private User this_user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);
        setUp();
    }

    private void setUp(){
        setTitle("Notification Center");
        this_user = getIntent().getParcelableExtra("this_user");
        notification_recycler = findViewById(R.id.activity_notification_center_recycler);
        if(this_user != null){
            notification_adapter = new NotificationCenterAdapter(this_user.getId(), this, this);
            notification_recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            notification_recycler.setAdapter(notification_adapter);
        }
    }

    @Override
    public void onNotificationClick(int position) {
        //TODO
    }
}
