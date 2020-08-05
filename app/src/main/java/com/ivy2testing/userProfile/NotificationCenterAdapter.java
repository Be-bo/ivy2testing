package com.ivy2testing.userProfile;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Notification;
import com.ivy2testing.util.Constant;

import java.util.ArrayList;
import java.util.List;

public class NotificationCenterAdapter extends RecyclerView.Adapter<NotificationCenterViewHolder> {

    // MARK: Variables

    private final static String TAG = "NotificationCenterAdapterTag";
    private List<Notification> notifications = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference stor = FirebaseStorage.getInstance().getReference();
    private DocumentSnapshot last_retrieved_document;
    private Query current_query;
    private String user_id;
    private Context context;
    private boolean load_in_progress = false;
    private boolean loaded_all_notifications = false;
    private NotificationListener notification_listener;








    // MARK: Base Methods

    public NotificationCenterAdapter(String userId, NotificationListener notificationListener) {
        this.notification_listener = notificationListener;
        this.user_id = userId;
        current_query = db.collection("users").document(userId).collection("notifications").limit(Constant.NOTIFICATION_CENTER_LIMIT)
                .orderBy("timestamp", Query.Direction.DESCENDING);
        pullNotifications(current_query);
    }

    public interface NotificationListener {
        void onNotificationClick(int position);
    }

    private void pullNotifications(Query query) {
        load_in_progress = true;
        int startSize = notifications.size();
        query.get().addOnCompleteListener(querySnap -> {
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {
                if (!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot doc = querySnap.getResult().getDocuments().get(i);
                        notifications.add(doc.toObject(Notification.class));
                        if (i >= querySnap.getResult().getDocuments().size() - 1)
                            last_retrieved_document = doc;
                    }
                    notifyItemRangeInserted(startSize, notifications.size());
                }else loaded_all_notifications = true;
            }
            load_in_progress = false;
        });
    }









    // MARK: Override Methods

    @NonNull
    @Override
    public NotificationCenterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_center, parent, false);
        return new NotificationCenterViewHolder(v, notification_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationCenterViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        String header;

        switch(notification.getType()){

            case Constant.NOTIFICATION_TYPE_CHAT:
                header = notification.getAuthor_name() + " sent you a message.";
                holder.content_text_view.setText(header);
                holder.time_text_view.setText(notification.getTimestamp().toString());
                //holder.visual_card_view.setRadius(R.dimen.notification_center_circle_radius);
                Log.d(TAG, "chat message: " + holder.visual_card_view.getRadius());
                if (notification.getVisual().contains("/")) {
                    stor.child(notification.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult()!=null) Glide.with(context).load(task.getResult()).circleCrop().into(holder.visual);

                        //holder.notif_image_view_square.setClipToOutline(true);
                    });
                } else holder.visual.setImageResource(R.drawable.ic_profile_selected);
                break;


            case Constant.NOTIFICATION_TYPE_COMMENT:
                header = notification.getAuthor_name() + " commented on " + notification.getTarget_name();
                holder.content_text_view.setText(header);
                holder.time_text_view.setText(notification.getTimestamp().toString());
                holder.visual_card_view.setRadius(R.dimen.notification_center_circle_radius);
                if (notification.getVisual().contains("/")) {
                    stor.child(notification.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult() != null)Glide.with(context).load(task.getResult()).circleCrop().into(holder.visual);
                    });
                } else holder.visual.setImageResource(R.drawable.ic_profile_selected);

                break;


            case Constant.NOTIFICATION_TYPE_FEATURED:
                header = notification.getAuthor_name() + " featured their " + notification.getTarget_name();
                holder.content_text_view.setText(header);
                holder.time_text_view.setText(notification.getTimestamp().toString());
                holder.visual_card_view.setRadius(R.dimen.standard_corner_radius);
                Log.d(TAG, "featured: " + holder.visual_card_view.getRadius());
                if (notification.getVisual().contains("/")) {
                    stor.child(notification.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult() != null)Glide.with(context).load(task.getResult()).circleCrop().into(holder.visual);
                    });
                } else holder.visual.setImageResource(R.drawable.ivy_logo);
                break;


            case Constant.NOTIFICATION_TYPE_ORG_EVENT:
                header = notification.getAuthor_name() + " added a new event: " + notification.getTarget_name();
                holder.content_text_view.setText(header);
                holder.time_text_view.setText(notification.getTimestamp().toString());
                holder.visual_card_view.setRadius(R.dimen.standard_corner_radius);
                if (notification.getVisual().contains("/")) {
                    stor.child(notification.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult() != null)Glide.with(context).load(task.getResult()).circleCrop().into(holder.visual);
                    });
                } else holder.visual.setImageResource(R.drawable.ivy_logo);
                break;


            case Constant.NOTIFICATION_TYPE_ORG_POST:
                header = notification.getAuthor_name() + " posted " + notification.getTarget_name();
                holder.content_text_view.setText(header);
                holder.time_text_view.setText(notification.getTimestamp().toString());
                holder.visual_card_view.setRadius(R.dimen.standard_corner_radius);
                if (notification.getVisual().contains("/")) {
                    stor.child(notification.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult() != null)Glide.with(context).load(task.getResult()).circleCrop().into(holder.visual);
                    });
                } else holder.visual.setImageResource(R.drawable.ivy_logo);
                break;
        }

        if (!load_in_progress && position >= (notifications.size() - Constant.NOTIFICATION_BATCH_TOLERANCE)) {
            if (last_retrieved_document != null && !loaded_all_notifications)
                pullNotifications(current_query.startAfter(last_retrieved_document));
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}
