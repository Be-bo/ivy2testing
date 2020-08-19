package com.ivy2testing.bubbletabs;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

class ExploreEventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    ImageView image;
    TextView name;
    ConstraintLayout layout;
    ExploreEventsAdapter.AllEventsItemClickListener listener;

    public ExploreEventsViewHolder(@NonNull View itemView, ExploreEventsAdapter.AllEventsItemClickListener listenr) {
        super(itemView);
        image = itemView.findViewById(R.id.item_event_view_all_image);
        name = itemView.findViewById(R.id.item_evennt_view_all_name);
        layout = itemView.findViewById(R.id.item_event_view_all_layout);
        listener = listenr;
        layout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        listener.onEventClick(getAdapterPosition());
    }
}
