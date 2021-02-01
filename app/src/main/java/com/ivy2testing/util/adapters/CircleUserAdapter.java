package com.ivy2testing.util.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.util.ImageUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/** @author Zahra Ghavasieh
 * Overview: an adapter that takes in a list of image Uris and constructs small(40x40dp) circle images
 * Used in: EventView.going
 */
public class CircleUserAdapter extends RecyclerView.Adapter<CircleUserAdapter.CircleImgHolder> {

    private static final String TAG = "CircleUserAdapter";
    // Vars
    private final List<String> user_ids;
    private final Context context;
    private final OnPersonListener person_listener;
    private final StorageReference stor_ref = FirebaseStorage.getInstance().getReference();


    public CircleUserAdapter(List<String> ids, Context con, OnPersonListener listener) {
        this.user_ids = ids;
        this.context = con;
        this.person_listener = listener;
    }

    public interface OnPersonListener{
        void onPersonClicked(int position);
    }

    public String getItem(int position){
        return user_ids.get(position);
    }

    public void removeUser(String userId){
        user_ids.remove(userId);
        notifyDataSetChanged();
    }

    public void addUser(String userId){
        user_ids.add(userId);
        notifyDataSetChanged();
    }


/* Overridden Methods
***************************************************************************************************/


    @NonNull
    @Override
    public CircleImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_horizontal_people, parent, false);
        return new CircleImgHolder(view, person_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CircleImgHolder holder, int position) {
        String path = ImageUtils.getUserImagePreviewPath(user_ids.get(position));
        try {
            stor_ref.child(path).getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null)
                    Glide.with(context).load(task.getResult()).into(holder.circle_img);
            });
        } catch (Exception e) {
            Log.w(TAG, "StorageException! No Preview Image for this user.");
        }
    }

    @Override
    public int getItemCount() {
        return user_ids.size();
    }


/* View Holder subclass
***************************************************************************************************/

    static class CircleImgHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Attributes
        CircleImageView circle_img;
        ConstraintLayout constraint_layout;
        OnPersonListener person_listener;


        public CircleImgHolder(@NonNull View itemView, final OnPersonListener listener) {
            super(itemView);
            circle_img = itemView.findViewById(R.id.recyclerCircleItem_image);
            constraint_layout = itemView.findViewById(R.id.recyclerCircleItem_layout);
            person_listener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            person_listener.onPersonClicked(getAdapterPosition());
        }
    }
}
