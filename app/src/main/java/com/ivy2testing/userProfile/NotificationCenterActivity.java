package com.ivy2testing.userProfile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Notification;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.util.Utils;

public class NotificationCenterActivity extends AppCompatActivity implements NotificationCenterAdapter.NotificationListener{

    private RecyclerView notification_recycler;
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
        notification_recycler = findViewById(R.id.activity_notification_center_recycler);
        if(this_user != null){
            notification_adapter = new NotificationCenterAdapter(this_user.getId(), this, this, findViewById(R.id.activity_notification_center_no_notifs));
            notification_recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            notification_recycler.setAdapter(notification_adapter);
        }
    }

    @Override
    public void onNotificationClick(int position) {
        // Toast.makeText(this, "My position is " + position, Toast.LENGTH_SHORT).show();
        Notification clicked = notification_adapter.getNotification(position);
        Intent intent = new Intent();
        switch(clicked.getType()){
            case 1:
                intent = new Intent(this, MainActivity.class);
                break;
            case 2:
                intent = new Intent(this, ViewPostOrEventActivity.class);
                intent.putExtra("post_uni", Utils.getThis_user().getUni_domain());
                intent.putExtra("post_id", clicked.getNotification_origin_name());
                intent.putExtra("this_user",Utils.getThis_user());
                break;
            case 3:
                intent = new Intent(this, MainActivity.class);
                break;
            case 4:
                intent = new Intent(this, ViewPostOrEventActivity.class);
                intent.putExtra("post_uni", Utils.getThis_user().getUni_domain());
                intent.putExtra("post_id", clicked.getNotification_origin_name());
                intent.putExtra("this_user",Utils.getThis_user());
                break;
            case 5:
                intent = new Intent(this, ViewPostOrEventActivity.class);
                intent.putExtra("post_uni", Utils.getThis_user().getUni_domain());
                intent.putExtra("post_id", clicked.getNotification_origin_name());
                intent.putExtra("this_user",Utils.getThis_user());
                break;
        }
        startActivity(intent);

    }
}
