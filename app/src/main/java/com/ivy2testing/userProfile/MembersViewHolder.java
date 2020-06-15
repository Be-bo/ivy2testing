package com.ivy2testing.userProfile;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

import de.hdodenhof.circleimageview.CircleImageView;

class MembersViewHolder extends RecyclerView.ViewHolder {

    TextView name;
    ImageButton options;
    CircleImageView hodendofWeirdEuropeanNameCircleImageView;

    public MembersViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.item_members_name);
        options = itemView.findViewById(R.id.item_members_options);
        hodendofWeirdEuropeanNameCircleImageView = itemView.findViewById(R.id.item_members_image);
    }
}
