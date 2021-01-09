package com.ivy2testing.chat;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.ivy2testing.R;
import com.ivy2testing.entities.Chatroom;
import com.ivy2testing.util.Utils;

public class LobbyAdapter extends RecyclerView.Adapter<LobbyAdapter.LobbyViewHolder> {

    // Attributes
    private String this_username;
    private SortedList<Chatroom> chatrooms;
    OnChatroomClickListener selection_listener;

    // Constructor
    public LobbyAdapter(String this_username, OnChatroomClickListener listener) {
        this.this_username = this_username;
        this.selection_listener = listener;
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

        // Set Chat Title TODO
        if (!this_chatroom.getMembers().isEmpty() && !this_username.equals(this_chatroom.getMembers().get(0)))
            holder.tv_name.setText(this_chatroom.getMembers().get(0));
        else if (this_chatroom.getMembers().size() > 1)
            holder.tv_name.setText(this_chatroom.getMembers().get(1));
        else holder.tv_name.setText(R.string.chatroom);

        // Set time_stamp TODO
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

    //TODO get partner

    // TODO set listener on last message


/* View Holder subclass
***************************************************************************************************/

    static class LobbyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_name;
        TextView tv_lastMsg;
        ConstraintLayout layout;


        public LobbyViewHolder(@NonNull View itemView, final OnChatroomClickListener listener) {
            super(itemView);

            // Initialize views
            tv_name = itemView.findViewById(R.id.item_chatroom_title);
            tv_lastMsg = itemView.findViewById(R.id.item_chatroom_lastMsg);
            layout = itemView.findViewById(R.id.item_chatroom_layout);

            //Set Listeners
            layout.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onShortClick(position);
                }
            });

            layout.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onLongClick(position, v);
                    return true;
                }
                else return false;
            });
        }
    }


/* Item Click Interface (different methods for short and long(click and hold) clicks)
***************************************************************************************************/

    public interface OnChatroomClickListener {
        void onShortClick(int position);
        void onLongClick(int position, View v);
    }
}

