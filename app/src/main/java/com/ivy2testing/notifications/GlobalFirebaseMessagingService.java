package com.ivy2testing.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ivy2testing.R;


import java.util.HashMap;
import java.util.Map;

public class GlobalFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "GlobalFirebaseMessagingService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channel_1";
    private FirebaseFirestore base_database_reference = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String this_user_id = "";
    private String this_uni_domain = "";
    private PendingIntent conversation_intent;

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
        try{
            if(remoteMessage.getNotification() != null){
                final String notificationTitle = remoteMessage.getNotification().getTitle();
                final String notificationBody = remoteMessage.getNotification().getBody();
                pushNotification(notificationTitle, notificationBody);
            }

        }catch (NullPointerException npe){
            Log.e(TAG, "onMessageReceived: NullPointerException: "+ npe.getMessage());
        }
    }

    private void pushNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

//        Intent eventIntent = new Intent(this, MainActivity.class);
//        eventIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent eventPendingIntent = PendingIntent.getActivity(this, 0, eventIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(eventPendingIntent);

        builder.setSmallIcon(R.drawable.ic_ivy_logo).setContentTitle(title).setContentText(message).setAutoCancel(true).setOnlyAlertOnce(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //for API 26 and above
            CharSequence name = "Primary Channel";
            String description = "This is ivy's primary notification channel.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setShowBadge(false);
            if(notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
        if(notificationManager != null) notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onNewToken(String token) { //when FCM token needs to be changed and we get notified that it has been we send the new one to the server
//        super.onNewToken(token);
        if(auth.getUid() != null) base_database_reference.collection("users").document(auth.getUid()).update("messaging_token", token);
    }
}
