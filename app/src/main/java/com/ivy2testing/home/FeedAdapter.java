package com.ivy2testing.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.Student;

import java.util.ArrayList;


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder>{

    private ArrayList<Post> post_array_list;




    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        public ImageView feed_image_view;
        public TextView feed_title;
        public TextView feed_text;
        public TextView feed_author;
        public TextView feed_pinned_id;


        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            feed_image_view = itemView.findViewById(R.id.object_imageview);
            feed_title = itemView.findViewById(R.id.object_title);
            feed_text = itemView.findViewById(R.id.object_body);
            feed_author = itemView.findViewById(R.id.object_posted_by_author);
            feed_pinned_id = itemView.findViewById(R.id.object_pinned_event);
        }
    }
    public FeedAdapter(ArrayList<Post> post_list){
        post_array_list = post_list;
    }



    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_object, parent,false);
        FeedViewHolder fvh = new FeedViewHolder(v);
        return fvh;
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {

   //     if(post_array_list!=null)

            if(post_array_list.get(position).getVisual().toString().contains("/")){
                holder.feed_image_view.setVisibility(View.VISIBLE);
                grabPictureFromDB( holder, post_array_list.get(position));
            }
            else
                holder.feed_image_view.setVisibility(View.GONE);

/*            if(post_array_list.get(position).getIs_event()) {
                holder.feed_title.setText(post_array_list.get(position).getName());

            }*/


           // holder.feed_title.setText(current_post.getName().toString());
            holder.feed_text.setText(post_array_list.get(position).getText().toString());
            holder.feed_author.setText(post_array_list.get(position).getText().toString());


    }

    @Override
    public int getItemCount() {

            return post_array_list.size();
    }

    public void grabPictureFromDB(FeedViewHolder holder, Post post){

        StorageReference db_storage = FirebaseStorage.getInstance().getReference();
        String visual_path = post.getVisual().toString();
        db_storage.child(visual_path).getBytes(1500000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                // bytes is an byte [] returned from storage,
                // set the image to be visible
                holder.feed_image_view.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO error handler
            }
        });

    }
}
