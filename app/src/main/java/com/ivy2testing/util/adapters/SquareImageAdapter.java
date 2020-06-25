package com.ivy2testing.util.adapters;

import android.content.Context;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.R;

import java.util.ArrayList;

/** @author Zahra Ghavasieh, Robert
 * Overview: an adapter that takes in a list of post ids and constructs square images per post
 * Used in: StudentProfile.Posts
 */
public class SquareImageAdapter extends RecyclerView.Adapter<SquareImageAdapter.SquareImgHolder> {
    private static final String TAG = "SquareImageAdapterTag";

    // Attributes
    private String author_id;
    private String uni_domain;
    private int pull_limit = 0;
    private ArrayList<Post> posts = new ArrayList<>();

    private FirebaseFirestore db_ref = FirebaseFirestore.getInstance();
    private StorageReference stor_ref = FirebaseStorage.getInstance().getReference();

    private OnPostListener post_listener;
    private Context context;
    private ListenerRegistration list_reg;


    // Constructors
    public SquareImageAdapter(String id, String uniDomain, int limit, Context mrContext, OnPostListener listener){
        this.uni_domain = uniDomain;
        this.author_id = id;
        this.context = mrContext;
        this.post_listener = listener;
        this.pull_limit = limit;
        startListening();
    }

    public void startListening(){
        Log.d(TAG, "start listening");
        setPostChangeListener(pull_limit);
    }

    // Listener Interface
    public interface OnPostListener{
        void onPostClick(int position);
    }

    public Post getItem(int position){
        return posts.get(position);
    }

    public void stopListening(){
        Log.d(TAG, "stop listening");
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

        // Input a default image in case post has no visual
        holder.image_view.setBackgroundColor(context.getColor(R.color.grey));
        holder.image_view.setImageResource(R.drawable.ic_ivy_logo_white);

        // Load visual from storage (will override default visual if it exists)
        loadImage(holder, posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }


/* Firebase related Methods
***************************************************************************************************/

    // Set up listener for changes in post
    private void setPostChangeListener(int limit){
        String address = "universities/" + uni_domain + "/posts";
        if (address.contains("null")){
            Log.e(TAG, "Address has null values.");
            return;
        }


        list_reg = db_ref.collection(address)
                .whereEqualTo("author_id", author_id).limit(limit)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){

                    for (int i = 0; i<queryDocumentSnapshots.getDocumentChanges().size(); i++) {

                        DocumentChange dc = queryDocumentSnapshots.getDocumentChanges().get(i);
                        DocumentSnapshot doc = dc.getDocument();

                        switch (dc.getType()) {
                            case ADDED:
                                if(!postAlreadyAdded(doc.getId())){
                                    Log.d(TAG, "adding: "+doc.getId());
                                    if ((boolean)doc.get("is_event")) posts.add(doc.toObject(Event.class));
                                    else if(!posts.contains(doc.toObject(Post.class))) posts.add(doc.toObject(Post.class));
                                }
                                break;

                            case MODIFIED:
                                Post modifiedPost;
                                if ((boolean)doc.get("is_event")) modifiedPost = doc.toObject(Event.class);
                                else modifiedPost = doc.toObject(Post.class);

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

    private boolean postAlreadyAdded(String id){
        for(int i = 0; i<posts.size(); i++){
            Log.d(TAG, "checking: "+posts.get(i).getId());
            if(posts.get(i).getId().equals(id)) return true;
        }
        return false;
    }


    // Load visual from storage
    private void loadImage(@NonNull SquareImgHolder holder, Post currentPost){
        if(currentPost.getVisual() != null && !currentPost.getVisual().equals("nothing"))
            stor_ref.child(currentPost.getVisual()).getDownloadUrl().addOnCompleteListener(
                    task -> {if(task.isSuccessful() && task.getResult()!=null)
                        Glide.with(context).load(task.getResult()).into(holder.image_view);});
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
