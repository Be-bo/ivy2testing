package com.ivy2testing.util.adapters;

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

import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: an adapter that takes in a list of image Uris and constructs square images
 * Used in: StudentProfile.Posts
 */
public class SquareImageAdapter extends RecyclerView.Adapter<SquareImageAdapter.SquareImgHolder> {

    // Attributes
    private List<Uri> images;
    private OnSelectionListener mSelectionListener;


    // Constructors
    public SquareImageAdapter(List<Uri> images){
        this.images = images;
    }


    // Listener Setter
    public void setOnSelectionListener(OnSelectionListener listener){
        this.mSelectionListener = listener;
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public SquareImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_grid_item, parent, false);
        return new SquareImgHolder(view, mSelectionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SquareImgHolder holder, final int position) {
        Picasso.get().load(images.get(position)).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


/* View Holder subclass
***************************************************************************************************/

    static class SquareImgHolder extends RecyclerView.ViewHolder{

        // Attributes
        ImageView mImageView;
        CardView mCardView;
        ConstraintLayout mLayout;

        // Methods
        SquareImgHolder(@NonNull View itemView, final OnSelectionListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.recyclerGridItem_img);
            mCardView = itemView.findViewById(R.id.recyclerGridItem_cardView);
            mLayout = itemView.findViewById(R.id.recyclerGridItem_layout);

            itemView.setOnClickListener(v -> {
                if (listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                        listener.onSelectionClick(position);
                }
            });
        }
    }
}
