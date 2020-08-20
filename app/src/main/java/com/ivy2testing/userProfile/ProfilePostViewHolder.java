package com.ivy2testing.userProfile;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;
import com.ivy2testing.userProfile.ProfilePostAdapter;

public class ProfilePostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    // Attributes
    public ImageView banner;
    public TextView banner_text;
    public TextView info_text;
    public ImageView image_view;
    public CardView card_view;
    public ConstraintLayout whole_layout;
    public ProfilePostAdapter.OnPostListener post_listener;

    // Methods
    ProfilePostViewHolder(@NonNull View itemView, final ProfilePostAdapter.OnPostListener listener) {
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