package com.ivy2testing.home;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;

import java.util.ArrayList;


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder>{

    private ArrayList<Post> post_array_list;
    private FeedViewHolder.FeedClickListener feed_click_listener;




    public static class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        FeedClickListener feed_click_listener;

        public ImageView banner;
        public ImageView feed_image_view;
        public TextView feed_title;
        public TextView feed_text;
        public TextView feed_author;
        public TextView feed_pinned_name;

        public ConstraintLayout full_constraint_layout;


        public FeedViewHolder(@NonNull View itemView, FeedClickListener feed_click_listener) {
            super(itemView);
            banner = itemView.findViewById(R.id.object_banner);
            feed_image_view = itemView.findViewById(R.id.object_imageview);
            feed_title = itemView.findViewById(R.id.object_title);
            feed_text = itemView.findViewById(R.id.object_body);
            feed_author = itemView.findViewById(R.id.object_posted_by_author);
            feed_pinned_name = itemView.findViewById(R.id.object_pinned_event);
            full_constraint_layout = itemView.findViewById(R.id.full_constraint_view);


            this.feed_click_listener = feed_click_listener;

            full_constraint_layout.setOnClickListener(this);


            feed_author.setOnClickListener(this);
            feed_pinned_name.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            feed_click_listener.onFeedClick(getAdapterPosition(),v.getId());

        }

        public interface FeedClickListener{
            void onFeedClick(int position, int clicked_id);
        }
    }
    public FeedAdapter(ArrayList<Post> post_list, FeedViewHolder.FeedClickListener feed_click_listener){
        post_array_list = post_list;
        this.feed_click_listener = feed_click_listener;
    }



    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_post_object, parent,false);
        FeedViewHolder fvh = new FeedViewHolder(v, feed_click_listener);
        return fvh;
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
            if(post_array_list.get(position).getIs_event()){
                Event current_event = (Event) post_array_list.get(position);
                holder.feed_title.setText(current_event.getName());
                holder.feed_title.setVisibility(View.VISIBLE);
            }
            else
                holder.feed_title.setVisibility(View.GONE);


            if(!post_array_list.get(position).getPinned_id().equals(""))
                holder.feed_pinned_name.setText(post_array_list.get(position).getPinned_name());
            else
                holder.feed_pinned_name.setText("None");



            // There is a weird bug with the title disappearing from events when you scroll far enough passed them
            // to solve this for now... just going to make titles/names View.GONE, they will only become visible if they
            // exist. Solves the bug for now/ if similar bugs appear later


            if(post_array_list.get(position).getVisual().contains("/")){
                holder.feed_image_view.setVisibility(View.VISIBLE);
                grabPictureFromDB(holder, post_array_list.get(position));
            }
            else
                holder.feed_image_view.setVisibility(View.GONE);

            holder.feed_text.setText(post_array_list.get(position).getText().toString());
            holder.feed_author.setText(post_array_list.get(position).getAuthor_name().toString());



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

                // banner
                if (post instanceof Event) holder.banner.setVisibility(View.VISIBLE);
                else holder.banner.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO error handler
            }
        });

    }
}
