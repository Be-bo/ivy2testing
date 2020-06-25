package com.ivy2testing.util.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.util.ImageUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/** @author Zahra Ghavasieh
 * Overview: an adapter that takes in a list of image Uris and constructs small(40x40dp) circle images
 * Used in: EventView.going
 */
public class CircleImageAdapter extends RecyclerView.Adapter<CircleImageAdapter.CircleImgHolder> {

    // Vars
    private List<String> user_ids;
    private String uni_domain = "";
    private Context context;
    private OnPersonListener person_listener;
    private StorageReference stor_ref = FirebaseStorage.getInstance().getReference();


    public CircleImageAdapter(List<String> ids, String domain, Context con, OnPersonListener listener) {
        this.user_ids = ids;
        this.context = con;
        this.uni_domain = domain;
        this.person_listener = listener;
    }

    public interface OnPersonListener{
        void onPersonClicked(int position);
    }

    public String getItem(int position){
        return user_ids.get(position);
    }


/* Overridden Methods
***************************************************************************************************/


    @NonNull
    @Override
    public CircleImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_profilepic_item, parent, false);
        return new CircleImgHolder(view, person_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CircleImgHolder holder, int position) {
        String path = ImageUtils.getPreviewPath(user_ids.get(position));
        stor_ref.child(path).getDownloadUrl().addOnCompleteListener(task -> { if(task.isSuccessful() && task.getResult() != null) Glide.with(context).load(task.getResult()).into(holder.circle_img); });
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
