package com.ivy2testing.userProfile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImgHolder> {

    // Attributes
    private List<Integer> images;
    private Context context;


    // Constructor
    ImageAdapter(Context context, List<Integer> images){
        this.context = context;
        this.images = images;
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public ImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_grid_item, parent, false);
        return new ImgHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImgHolder holder, int position) {
        holder.mImageView.setImageResource(images.get(position));
    }


    @Override
    public int getItemCount() {
        return images.size();
    }


/* Item View Subclass
***************************************************************************************************/

    class ImgHolder extends RecyclerView.ViewHolder{

        // Attributes
        ImageView mImageView;

        // Methods
        ImgHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.recyclerGridItem_img);
        }
    }

}
