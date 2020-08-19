package com.ivy2testing.bubbletabs;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView event_name;
    ImageView event_image;
    ImageView author_image;
    CardView cardview;
    EventAdapter.EventClickListener event_listener;
    int adapter_type = 0;

    public EventViewHolder(@NonNull View itemView, EventAdapter.EventClickListener listener, int adapterType) {
        super(itemView);
        event_listener = listener;
        cardview = itemView.findViewById(R.id.item_event_cardview);
        event_image = itemView.findViewById(R.id.item_event_image);
        author_image = itemView.findViewById(R.id.item_event_author_image);
        event_name = itemView.findViewById(R.id.item_event_name);
        adapter_type = adapterType;
        cardview.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        event_listener.onEventClick(getAdapterPosition(), v.getId(), adapter_type);
    }
}
