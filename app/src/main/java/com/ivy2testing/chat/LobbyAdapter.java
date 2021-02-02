package com.ivy2testing.chat;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Chatroom;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.Utils;

public class LobbyAdapter extends RecyclerView.Adapter<LobbyAdapter.LobbyViewHolder> {

    private static final String TAG = "LobbyAdapter";
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private final StorageReference db_storage = FirebaseStorage.getInstance().getReference();


    // Attributes
    private final User this_user;
    private SortedList<Chatroom> chatrooms;
    OnChatroomClickListener selection_listener;
    private final Context context;

    // Constructor
    public LobbyAdapter(User this_user, OnChatroomClickListener listener, Context context) {
        this.this_user = this_user;
        this.selection_listener = listener;
        this.context = context;
    }

    public void setChatrooms(SortedList<Chatroom> chatrooms) {
        this.chatrooms = chatrooms;
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public LobbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_chatroom, parent, false);
        return new LobbyViewHolder(view, selection_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull LobbyViewHolder holder, int position) {
        Chatroom this_chatroom = chatrooms.get(position);

        // Set Chat Title
        if (!this_chatroom.getMembers().isEmpty() && !this_user.getId().equals(this_chatroom.getMembers().get(0)))
            loadPartner(holder, this_chatroom.getMembers().get(0));
        else if (this_chatroom.getMembers().size() > 1)
            loadPartner(holder, this_chatroom.getMembers().get(1));
        else {
            holder.partner = this_user;
            holder.tv_name.setText(holder.partner.getName());
        }

        // Set time_stamp
        if (this_chatroom.getLast_message_timestamp() != null) {
            holder.tv_lastMsg.setText(Utils.millisToDateTime(this_chatroom.getLast_message_timestamp()));
            holder.tv_lastMsg.setVisibility(View.VISIBLE);
        } else holder.tv_lastMsg.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }


/* Firebase
***************************************************************************************************/

    // Get Partner user
    private void loadPartner(@NonNull LobbyViewHolder holder, String id) {
        mFirestore.document(User.getPath(id)).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                DocumentSnapshot doc = task.getResult();
                if ((boolean) doc.get("is_organization"))
                    holder.partner = task.getResult().toObject(Organization.class);
                else holder.partner = task.getResult().toObject(Student.class);

                if (holder.partner != null) {
                    holder.tv_name.setText(holder.partner.getName());
                    loadPartnerPic(holder, id);
                }
                else Log.e(TAG, "user was null!");

            } else Log.w(TAG, task.getException());
        });
    }

    // Get partner profile pic
    private void loadPartnerPic(@NonNull LobbyViewHolder holder, String id) {
        String previewImgPath = ImageUtils.getUserImagePreviewPath(id);
        try {
            db_storage.child(previewImgPath).getDownloadUrl().addOnCompleteListener(task -> {
                if(task.isSuccessful() && task.getResult() != null) Glide.with(context).load(task.getResult()).into(holder.iv_userImg);
                else Glide.with(context).load(R.drawable.ic_profile_selected).into(holder.iv_userImg);
            });
        } catch (Exception e){
            Log.w(TAG, "StorageException! No Preview Image for this user.");
        }
    }


/* View Holder subclass
***************************************************************************************************/

    static class LobbyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_name;
        ImageView iv_userImg;
        TextView tv_lastMsg;
        ConstraintLayout layout;
        User partner;


        public LobbyViewHolder(@NonNull View itemView, final OnChatroomClickListener listener) {
            super(itemView);

            // Initialize views
            tv_name = itemView.findViewById(R.id.item_chatroom_title);
            iv_userImg = itemView.findViewById(R.id.item_chatroom_image);
            tv_lastMsg = itemView.findViewById(R.id.item_chatroom_lastMsg);
            layout = itemView.findViewById(R.id.item_chatroom_layout);

            //Set Listeners
            layout.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onShortClick(position, partner);
                }
            });

            layout.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onLongClick(position, partner, v);
                    return true;
                }
                else return false;
            });
        }
    }


/* Item Click Interface (different methods for short and long(click and hold) clicks)
***************************************************************************************************/

    public interface OnChatroomClickListener {
        void onShortClick(int position, User partner);
        void onLongClick(int position, User partner, View v);
    }
}

