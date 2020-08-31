package com.ivy2testing.notifications;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import static com.ivy2testing.util.Constant.CHANNEL_1_ID;
import static com.ivy2testing.util.Constant.CHANNEL_2_ID;
import static com.ivy2testing.util.Constant.CHANNEL_3_ID;
import static com.ivy2testing.util.Constant.CHANNEL_4_ID;
import static com.ivy2testing.util.Constant.CHANNEL_5_ID;

// Author: Clyde B

// This application will be called at the same time as on create in main activity, It creates the
// proper notification channels, and assigns them custom functions like vibration patterns or lights


public class ChannelCreationApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
//        createNotificationChannels();


    }

    // --Notification channels do not exist before Oreo

    // --Importance level decides if notification pops up on screen or just goes into system tray

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel message_channel = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Message Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            message_channel.setDescription("Messages sent to you through ivy will show here ");


            NotificationChannel comment_channel = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Comment Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            comment_channel.setDescription("Comments on your events and posts will show here ");


            NotificationChannel featured_channel = new NotificationChannel(
                    CHANNEL_3_ID,
                    "Featured Event Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            featured_channel.setDescription("News about featured events will show here ");


            NotificationChannel org_event_channel = new NotificationChannel(
                    CHANNEL_4_ID,
                    "Organization Events",
                    NotificationManager.IMPORTANCE_HIGH
            );
            org_event_channel.setDescription("When an organization posts a new event you will see it here ");

            NotificationChannel org_post_channel = new NotificationChannel(
                    CHANNEL_5_ID,
                    "Organization posts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            org_post_channel.setDescription("When an organization makes a post you will see it here ");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(message_channel);
            manager.createNotificationChannel(comment_channel);
            manager.createNotificationChannel(featured_channel);
            manager.createNotificationChannel(org_event_channel);
            manager.createNotificationChannel(org_post_channel);
        }
    }
}
