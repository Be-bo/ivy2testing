package com.ivy2testing.util.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.entities.Post;
import com.ivy2testing.R;

import java.util.ArrayList;

/** @author Zahra Ghavasieh, Robert
 * Overview: an adapter that takes in a list of post ids and constructs square images per post
 * Used in: StudentProfile.Posts
 */
public class SquareImageAdapter extends RecyclerView.Adapter<SquareImageAdapter.SquareImgHolder> {

    // Attributes
    private String author_id;
    private String uni_domain;

    private ArrayList<Post> posts = new ArrayList<>();

    private FirebaseFirestore db_ref = FirebaseFirestore.getInstance();
    private StorageReference stor_ref = FirebaseStorage.getInstance().getReference();

    private OnPostListener post_listener;
    private Context context;
    private ListenerRegistration list_reg;


    //TODO: set up 0 post case


    // Constructors
    public SquareImageAdapter(String id, String uniDomain, int limit, Context mrContext, OnPostListener listener){
        this.uni_domain = uniDomain;
        this.author_id = id;
        this.context = mrContext;
        this.post_listener = listener;
        list_reg = db_ref.collection("universities").document(uni_domain).collection("posts").whereEqualTo("author_id", author_id).limit(limit).addSnapshotListener((queryDocumentSnapshots, e) -> {
            if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                for (int i = 0; i<queryDocumentSnapshots.getDocumentChanges().size(); i++) {
                    DocumentChange dc = queryDocumentSnapshots.getDocumentChanges().get(i);
                    switch (dc.getType()) {
                        case ADDED:
                            posts.add(dc.getDocument().toObject(Post.class));
                            break;
                        case MODIFIED:
                            Post modifiedPost = dc.getDocument().toObject(Post.class);
                            for(int j=0; j<posts.size(); j++){
                                if(posts.get(j).getId().equals(modifiedPost.getId())){
                                    posts.set(j, modifiedPost);
                                    break;
                                }
                            }
                            break;
                        case REMOVED:
                            //TODO later
                            break;
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    // Listener Interface
    public interface OnPostListener{
        void onPostClick(int position);
    }

    public Post getItem(int position){
        return posts.get(position);
    }

    public void cleanUp(){
        list_reg.remove();
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public SquareImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_grid_item, parent, false);
        return new SquareImgHolder(view, post_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SquareImgHolder holder, final int position) {
        Post currentPost = posts.get(position);

        // Input a default image in case post has no visual
        holder.image_view.setBackgroundColor(context.getColor(R.color.grey));
        holder.image_view.setImageResource(R.drawable.ic_ivy_logo_white);

        // Load visual from storage (will override default visual if it exists)
        if(currentPost.getVisual() != null && !currentPost.getVisual().equals("nothing"))
            stor_ref.child(currentPost.getVisual()).getDownloadUrl().addOnCompleteListener(
                    task -> {if(task.isSuccessful() && task.getResult()!=null)
                        Glide.with(context).load(task.getResult()).into(holder.image_view);});
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    /* View Holder subclass
***************************************************************************************************/

    static class SquareImgHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        // Attributes
        ImageView image_view;
        CardView card_view;
        ConstraintLayout whole_layout;
        OnPostListener post_listener;

        // Methods
        SquareImgHolder(@NonNull View itemView, final OnPostListener listener) {
            super(itemView);
            image_view = itemView.findViewById(R.id.recyclerGridItem_img);
            card_view = itemView.findViewById(R.id.recyclerGridItem_cardView);
            whole_layout = itemView.findViewById(R.id.recyclerGridItem_layout);
            post_listener = listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            post_listener.onPostClick(getAdapterPosition());
        }
    }
}
