package com.ivy2testing.util.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;
import com.ivy2testing.util.OnSelectionListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/** @author Zahra Ghavasieh
 * Overview: an adapter that takes in a list of image Uris and constructs small(40x40dp) circle images
 * Used in: EventView.going
 */
public class CircleImageAdapter extends RecyclerView.Adapter<CircleImageAdapter.CircleImgHolder> {

    // Attributes
    private List<Uri> images;
    private Context context;
    OnSelectionListener selection_listener;


    public CircleImageAdapter(List<Uri> images) {
        this.images = images;
    }

    public void setOnSelectionListener (OnSelectionListener listener){
        this.selection_listener = listener;
    }


/* Overridden Methods
***************************************************************************************************/


    @NonNull
    @Override
    public CircleImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_profilepic_item, parent, false);
        return new CircleImgHolder(view, selection_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CircleImgHolder holder, int position) {
        if (images.get(position) != null) Picasso.get().load(images.get(position)).into(holder.circle_img);
        else holder.circle_img.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }




    /* View Holder subclass
***************************************************************************************************/

    static class CircleImgHolder extends RecyclerView.ViewHolder {

        // Attributes
        CircleImageView circle_img;
        ConstraintLayout constraint_layout;


        public CircleImgHolder(@NonNull View itemView, final OnSelectionListener listener) {
            super(itemView);
            circle_img = itemView.findViewById(R.id.recyclerCircleItem_image);
            constraint_layout = itemView.findViewById(R.id.recyclerCircleItem_layout);

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
