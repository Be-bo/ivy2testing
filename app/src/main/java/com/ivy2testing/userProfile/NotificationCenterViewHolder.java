package com.ivy2testing.userProfile;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

class NotificationCenterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    NotificationCenterAdapter.NotificationListener notification_listener;
    public ImageView visual;
    public TextView content_text_view;
    public TextView time_text_view;
    public CardView visual_card_view;

    public NotificationCenterViewHolder(@NonNull View itemView, NotificationCenterAdapter.NotificationListener notificationListener) {
        super(itemView);
        visual = itemView.findViewById(R.id.item_notification_center_image);
        content_text_view = itemView.findViewById(R.id.item_notification_center_text);
        time_text_view = itemView.findViewById(R.id.item_notification_center_small_text);
        visual_card_view = itemView.findViewById(R.id.item_notification_center_cardview);
        this.notification_listener = notificationListener;
        itemView.setOnClickListener(this);
    }

    public void onClick(View v) {
        notification_listener.onNotificationClick(getAdapterPosition());
    }
}
