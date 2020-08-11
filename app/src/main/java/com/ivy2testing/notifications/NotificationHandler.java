package com.ivy2testing.notifications;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.userProfile.NotificationCenterActivity;
import com.ivy2testing.util.Utils;

import static com.ivy2testing.util.Constant.CHANNEL_1_ID;
import static com.ivy2testing.util.Constant.CHANNEL_2_ID;
import static com.ivy2testing.util.Constant.CHANNEL_3_ID;
import static com.ivy2testing.util.Constant.CHANNEL_4_ID;
import static com.ivy2testing.util.Constant.CHANNEL_5_ID;

public class NotificationHandler extends FirebaseMessagingService {
    public static final String TAG = "NotificationHandler";
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();
    public static int CHANNEL_1_COUNT = 1;
    public static int CHANNEL_2_COUNT = 11;
    public static int CHANNEL_3_COUNT = 21;
    public static int CHANNEL_4_COUNT = 31;
    public static int CHANNEL_5_COUNT = 41;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("NEW_TOKEN", token);
        sendRegistrationToServer(token);
    }


    private void sendRegistrationToServer(String token) {
        Utils.sendRegistrationToServer(token);

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: message received");
        if (remoteMessage.getData().get("type_data") != null) {
            grabPhoto(remoteMessage.getData().get("visual_data"), remoteMessage);
        }
    }

    private void grabPhoto(String path, RemoteMessage remoteMessage) {
        if (path.contains("/")) {
            db_storage.child(path).getBytes(1048576).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    //   Log.d(TAG, "onComplete: " + task.isSuccessful());
                    if (task.isSuccessful() && task.getResult() != null)
                        buildNotification(BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length), remoteMessage);
                    else buildNotification(remoteMessage);
                }
            });
        } else {
            buildNotification(remoteMessage);
        }
    }

    private void buildNotification(Bitmap large_img, RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, NotificationCenterActivity.class);
        String channel_id = "";
        int notif_id = 0;

        switch (remoteMessage.getData().get("type_data")) {
            case "1":
                channel_id = CHANNEL_1_ID;
                intent = new Intent(this, NotificationCenterActivity.class);
                notif_id = idBuilder(1);
                break;
            case "2":
                channel_id = CHANNEL_2_ID;
                intent = new Intent(this, ViewPostOrEventActivity.class);
                intent.putExtra("post_id", remoteMessage.getData().get("intent_data"));
                intent.putExtra("post_uni",Utils.getThis_user().getUni_domain());
                intent.putExtra("this_user",Utils.getThis_user());
                notif_id = idBuilder(2);
                break;
            case "3":
                channel_id = CHANNEL_3_ID;
                intent = new Intent(this, NotificationCenterActivity.class);
                notif_id = idBuilder(3);
                break;
            case "4":
                channel_id = CHANNEL_4_ID;
                intent = new Intent(this, NotificationCenterActivity.class);
                notif_id = idBuilder(4);
                break;
            case "5":
                channel_id = CHANNEL_5_ID;
                intent = new Intent(this, NotificationCenterActivity.class);
                notif_id = idBuilder(5);
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);


        builder.setSmallIcon(R.drawable.ivy_logo) // ivy logo
                .setLargeIcon(getCircleBitmap(large_img)) // ivy logo
                .setContentTitle(remoteMessage.getData().get("title_data"))
                .setContentText(remoteMessage.getData().get("body_data")) // TODO
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setChannelId(channel_id)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notif_id, builder.build());

    }


    private void buildNotification(RemoteMessage remoteMessage) {
        Log.d(TAG, "buildNotification: called");
        Intent intent = new Intent(this, NotificationCenterActivity.class);
        String channel_id = "";
        int notif_id = 0;


        switch (remoteMessage.getData().get("type_data")) {
            case "1":
                channel_id = CHANNEL_1_ID;
                intent = new Intent(this, NotificationCenterActivity.class);
                notif_id = idBuilder(1);
                break;
            case "2":
                channel_id = CHANNEL_2_ID;
                intent = new Intent(this, ViewPostOrEventActivity.class);
                intent.putExtra("post_uni",Utils.getThis_user().getUni_domain());
                intent.putExtra("post_id", remoteMessage.getData().get("intent_data"));
                intent.putExtra("this_user",Utils.getThis_user());
                notif_id = idBuilder(2);
                break;
            case "3":
                channel_id = CHANNEL_3_ID;
                intent = new Intent(this, NotificationCenterActivity.class);
                notif_id = idBuilder(3);
                break;
            case "4":
                channel_id = CHANNEL_4_ID;
                intent = new Intent(this, NotificationCenterActivity.class);
                notif_id = idBuilder(4);
                break;
            case "5":
                channel_id = CHANNEL_5_ID;
                intent = new Intent(this, NotificationCenterActivity.class);
                notif_id = idBuilder(5);
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);


        builder.setSmallIcon(R.drawable.ivy_logo)
                .setContentTitle(remoteMessage.getData().get("title_data"))
                .setContentText(remoteMessage.getData().get("title_data")) // TODO is this necessary?
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setChannelId(channel_id)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notif_id, builder.build());
    }


    private int idBuilder(int to_check) {
        switch (to_check) {
            case 1:
                if (CHANNEL_1_COUNT % 5 == 0) CHANNEL_1_COUNT = 1;
                else CHANNEL_1_COUNT += 1;
                return CHANNEL_1_COUNT;
            case 2:
                if (CHANNEL_2_COUNT % 3 == 0) CHANNEL_2_COUNT = 11;
                else CHANNEL_2_COUNT += 1;
                return CHANNEL_2_COUNT;
            case 3:
                return CHANNEL_3_COUNT;
            case 4:
                if (CHANNEL_4_COUNT % 5 == 0) CHANNEL_4_COUNT = 31;
                else CHANNEL_4_COUNT += 1;
                return CHANNEL_4_COUNT;
            case 5:
                if (CHANNEL_5_COUNT % 3 == 0) CHANNEL_5_COUNT = 41;
                else CHANNEL_5_COUNT += 1;
                return CHANNEL_5_COUNT;
        }
        return 0;
    }


    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height) {
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        } else {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width) / 2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        bitmap.recycle();

        return output;
    }


}
