package com.ivy2testing.util.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

public class SquareImgHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    // Attributes
    ImageView banner;
    TextView banner_text;
    TextView info_text;
    ImageView image_view;
    CardView card_view;
    ConstraintLayout whole_layout;
    SquarePostAdapter.OnPostListener post_listener;

    // Methods
    SquareImgHolder(@NonNull View itemView, final SquarePostAdapter.OnPostListener listener) {
        super(itemView);
        banner = itemView.findViewById(R.id.grid_item_banner);
        image_view = itemView.findViewById(R.id.recyclerGridItem_img);
        card_view = itemView.findViewById(R.id.recyclerGridItem_cardView);
        whole_layout = itemView.findViewById(R.id.recyclerGridItem_layout);
        banner_text = itemView.findViewById(R.id.grid_item_banner_text);
        info_text = itemView.findViewById(R.id.grid_item_text_info);
        post_listener = listener;

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        post_listener.onPostClick(getAdapterPosition());
    }
}