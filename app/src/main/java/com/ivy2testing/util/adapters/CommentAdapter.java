package com.ivy2testing.util.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Comment;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.OnSelectionListener;
import com.ivy2testing.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Zahra Ghavasieh
 * Overview: an adapter that takes in a list of Comment Items and creates a list of them
 * Used in: ViewPostOrEventActivity
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private static final String TAG = "CommentAdapterTag";

    // Attributes
    private static final int BATCH_LIMIT = 15;
    private static final int NEW_BATCH_TOLERANCE = 4;
    private int max_height;
    private ArrayList<Comment> comments;
    private Context context;
    OnSelectionListener listener;
    private String campus_domain;
    private String post_id;

    // firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference firebase_storage = FirebaseStorage.getInstance().getReference();

    private DocumentSnapshot last_retrieved_document;
    private Query default_query;
    private boolean loaded_all_comments = false;
    private boolean load_in_progress = false;
    private RecyclerView recycler;


    // Constructors
    public CommentAdapter(Context context, RecyclerView rec, String campus_domain, String post_id, int max_height) {
        comments = new ArrayList<>();
        this.context = context;
        this.recycler = rec;
        this.campus_domain = campus_domain;
        this.post_id = post_id;
        this.max_height = max_height;


        default_query = db.collection("universities").document(campus_domain).collection("posts").document(post_id).collection("comments").orderBy("creation_millis", Query.Direction.DESCENDING).limit(BATCH_LIMIT);

        fetchCommentBatch(default_query);
    }

    // Listener Setter
    public void setOnSelectionListener(OnSelectionListener listener) {
        this.listener = listener;
    }


    /* Overridden Methods
     ***************************************************************************************************/

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment currentComment = comments.get(position);
        holder.tv_username.setText(comments.get(position).getAuthor_name());
        loadAthorImage(holder, currentComment.getAuthor_id());

        if(currentComment.getType() == 1){
            holder.iv_comment_image.setImageURI(null);
            holder.tv_comment.setText(currentComment.getText());
            holder.iv_comment_image.setVisibility(View.GONE);
            holder.tv_comment.setVisibility(View.VISIBLE);
        }
        else if (currentComment.getType() == 2){
            holder.tv_comment.setText(null);
            holder.iv_comment_image.setVisibility(View.VISIBLE);
            holder.tv_comment.setVisibility(View.GONE);
            loadCommentImage(holder, currentComment.getText());
        }

        //readjust recycler height so it doesn't infinitely pull from db
        if(position==12){
            ViewGroup.LayoutParams params = recycler.getLayoutParams();
            params.height = max_height;
            recycler.setLayoutParams(params);
        }

        if(!load_in_progress && position >= (comments.size() - NEW_BATCH_TOLERANCE)){
            if(last_retrieved_document != null && !loaded_all_comments){
                fetchCommentBatch(default_query.startAfter(last_retrieved_document));
            }
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }


    /* Firebase Methods
     ***************************************************************************************************/

    private void loadAthorImage(CommentViewHolder holder, String user_id) {
        // Load a placeholder first in case something goes wrong
        holder.circle_img.setImageDrawable(context.getDrawable(R.drawable.ic_profile_selected));

        // Find address of possible image
        String address = ImageUtils.getUserImagePreviewPath(user_id);
        if (address.contains("null")) {
            Log.e(TAG, "Address contained null! UserId: " + user_id);
            return;
        }

        try {
            firebase_storage.child(address).getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null)
                    Picasso.get().load(task.getResult()).into(holder.circle_img);
                else Log.w(TAG, "this user's image doesn't exist! user: " + user_id);
            });
        } catch (Exception e) {
            Log.w(TAG, "StorageException! No Preview Image for this user.");
        }
    }

    private void loadCommentImage(CommentViewHolder holder, String address){
        if(address != null && address.contains("/")){
            firebase_storage.child(address).getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null)
                            Picasso.get().load(task.getResult()).into(holder.iv_comment_image);
                        else Log.w(TAG, "Something went wrong");
                    });
        }
    }

    private void fetchCommentBatch(Query query) { //fetch all posts (events and standard posts) combined based on the input query
        load_in_progress = true;
        query.get().addOnCompleteListener(querySnap -> {
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {
                if (!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot newComment = querySnap.getResult().getDocuments().get(i);
                        Comment comment = newComment.toObject(Comment.class);
                        if (comment != null ) {
                            comment.setId(newComment.getId());
                            comments.add(comment);
                        }
                        Log.d(TAG, "id " + comment.getId());
                        if (i >= querySnap.getResult().getDocuments().size() - 1)
                            last_retrieved_document = newComment;
                    }

                    if (comments.size() < 1) { //if the size is still 0 we need to check for the next batch (because if size 0 onBindViewHolder won't get called)
                        if (last_retrieved_document != null && !loaded_all_comments) {
                            fetchCommentBatch(default_query.startAfter(last_retrieved_document)); //next batch has to be loaded from where the previous one left off
                        }
                    }
                    notifyDataSetChanged();
                } else loaded_all_comments = true; //no more data to load
            }
            //checkEmptyAdapter();
            load_in_progress = false;
        });
    }

    private boolean commentAlreadyAdded(String commmentId) { //TODO
        for (Comment current : comments) {
            if (current.getId().equals(commmentId)) return true;
        }
        return false;


    }

    public Comment getComment(int position) {
        return comments.get(position);
    }

    public void newComment(Comment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
        recycler.scrollToPosition(0);

    }





    /* View Holder subclass
     ***************************************************************************************************/

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circle_img;
        TextView tv_username;
        TextView tv_comment;
        ImageView iv_comment_image;


        public CommentViewHolder(@NonNull View itemView, final OnSelectionListener listener) {
            super(itemView);

            // Initialize Views
            circle_img = itemView.findViewById(R.id.comment_userImage);
            tv_username = itemView.findViewById(R.id.comment_userName);
            tv_comment = itemView.findViewById(R.id.comment_commentText);
            iv_comment_image = itemView.findViewById(R.id.comment_imageView);


            // Set Listeners on user image and name only
            circle_img.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onSelectionClick(position);
                }
            });

            tv_username.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onSelectionClick(position);
                }
            });
        }
    }
}
