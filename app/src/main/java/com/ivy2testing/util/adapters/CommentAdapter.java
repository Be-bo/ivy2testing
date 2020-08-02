package com.ivy2testing.util.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Comment;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.OnSelectionListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/** @author Zahra Ghavasieh
 * Overview: an adapter that takes in a list of Comment Items and creates a list of them
 * Used in: ViewPostOrEventActivity
 */
public class CommentAdapter extends  RecyclerView.Adapter<CommentAdapter.CommentViewHolder>{
    private static final String TAG = "CommentAdapter";

    // Attributes
    private List<Comment> comments;
    private Context context;
    OnSelectionListener listener;

    private StorageReference firebase_storage = FirebaseStorage.getInstance().getReference();


    // Constructors
    public CommentAdapter(List<Comment> comments){
        this.comments = comments;
    }

    // Listener Setter
    public void setOnSelectionListener(OnSelectionListener listener){
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
        holder.tv_username.setText(comments.get(position).getAuthor_name());
        holder.tv_comment.setText(comments.get(position).getText());
        loadImage(holder, comments.get(position).getAuthor_id());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }


/* Firebase Methods
***************************************************************************************************/

    private void loadImage(CommentViewHolder holder, String user_id) {
        // Load a placeholder first in case something goes wrong
        holder.circle_img.setImageDrawable(context.getDrawable(R.drawable.ic_profile_selected));

        // Find address of possible image
        String address = ImageUtils.getUserImagePreviewPath(user_id);
        if (address.contains("null")){
            Log.e(TAG, "Address contained null! UserId: " + user_id);
            return;
        }

        firebase_storage.child(address).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        Picasso.get().load(task.getResult()).into(holder.circle_img);
                    else Log.w(TAG, "this user's image doesn't exist! user: " + user_id);
                });
    }


/* View Holder subclass
***************************************************************************************************/

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circle_img;
        TextView tv_username;
        TextView tv_comment;


        public CommentViewHolder(@NonNull View itemView, final OnSelectionListener listener) {
            super(itemView);

            // Initialize Views
            circle_img = itemView.findViewById(R.id.comment_userImage);
            tv_username = itemView.findViewById(R.id.comment_userName);
            tv_comment = itemView.findViewById(R.id.comment_commentText);


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
