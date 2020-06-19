package com.ivy2testing.util.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.ImageUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**@author Zahra Ghavasieh
 * Overview: an adapter that takes in a list of image Uris and user names
 * Used in: SeeAllUsers Activity
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private static final String TAG = "UserAdapter";

    // Attributes
    private List<User> users;
    private Context context;
    OnUserItemClickListener selection_listener;

    private StorageReference firebase_storage = FirebaseStorage.getInstance().getReference();


    public UserAdapter(List<User> users) {
        this.users = users;
    }

    public void setOnUserItemClickListener (OnUserItemClickListener listener){
        this.selection_listener = listener;
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_members, parent, false);
        return new UserViewHolder(view, selection_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.tv_name.setText(users.get(position).getName());
        loadImage(holder, users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


/* Firebase Methods
***************************************************************************************************/

    private void loadImage(UserViewHolder holder, User user) {
        // Load a placeholder first in case something goes wrong
        holder.circle_img.setImageDrawable(context.getDrawable(R.drawable.ic_profile_selected));

        // Find address of possible image
        String address = ImageUtils.getPreviewPath(user.getId());
        if (address.contains("null")){
            Log.e(TAG, "Address contained null! UserId: " + user.getId());
            return;
        }

        firebase_storage.child(address).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        Picasso.get().load(task.getResult()).into(holder.circle_img);
                    else Log.w(TAG, "this user's image doesn't exist! user: " + user.getId());
                });
    }


/* View Holder subclass
***************************************************************************************************/

    static class UserViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circle_img;
        TextView tv_name;
        ImageButton button_options;


        public UserViewHolder(@NonNull View itemView, final OnUserItemClickListener listener) {
            super(itemView);
            circle_img = itemView.findViewById(R.id.item_members_image);
            tv_name = itemView.findViewById(R.id.item_members_name);
            button_options = itemView.findViewById(R.id.item_members_options);

            tv_name.setOnClickListener(v -> {
                if (listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                        listener.onNameClick(position);
                }
            });

            button_options.setOnClickListener(v -> {
                if (listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                        listener.onOptionsClick(position);
                }
            });
        }
    }


/* Item Click Interface (specifically for user items)
***************************************************************************************************/

    public interface OnUserItemClickListener {
        void onNameClick(int position);
        void onOptionsClick(int position);
    }
}
