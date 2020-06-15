package com.ivy2testing.userProfile;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.util.OnSelectionListener;
import com.ivy2testing.R;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImgHolder> {

    // Attributes
    private List<Uri> images;
    private OnSelectionListener mSelectionListener;


    // Constructors
    ImageAdapter(List<Uri> images){
        this.images = images;
    }


    // Listener Setter
    void setOnSelectionListener(OnSelectionListener listener){
        this.mSelectionListener = listener;
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public ImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_grid_item, parent, false);
        return new ImgHolder(view, mSelectionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImgHolder holder, final int position) {
        Picasso.get().load(images.get(position)).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


/* View Holder subclass
***************************************************************************************************/

    static class ImgHolder extends RecyclerView.ViewHolder{

        // Attributes
        ImageView mImageView;
        CardView mCardView;
        ConstraintLayout mLayout;

        // Methods
        ImgHolder(@NonNull View itemView, final OnSelectionListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.recyclerGridItem_img);
            mCardView = itemView.findViewById(R.id.recyclerGridItem_cardView);
            mLayout = itemView.findViewById(R.id.recyclerGridItem_layout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                            listener.onSelectionClick(position);
                    }
                }
            });
        }
    }
}
