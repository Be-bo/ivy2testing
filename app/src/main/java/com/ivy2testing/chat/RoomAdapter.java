package com.ivy2testing.chat;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.Message;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.Utils;

import java.util.List;


public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.MessageViewHolder> {

    // Firebase
    private static final String TAG = "RoomAdapter";
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();


    // Attributes
    private User this_user;
    private User partner;
    private List<Message> messages;
    private Context context;



    public RoomAdapter(List<Message> messages, User this_user, User partner) {
        this.messages = messages;
        this.this_user = this_user;
        this.partner = partner;
    }


    /* Overridden Methods
     ***************************************************************************************************/

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message this_message = messages.get(position);
        holder.tv_message.setText(this_message.getText());
        holder.tv_timestamp.setText(Utils.millisToDateTime(this_message.getTime_stamp()));

        if (this_user.getId().equals(this_message.getAuthor())) {
            holder.tv_author.setText(this_user.getName());
            setChatRowAppearance(true, holder);
        } else {
            setChatRowAppearance(false, holder);
            if (partner.getId().equals(this_message.getAuthor()))
                holder.tv_author.setText(partner.getName());
            else  // A partner who has deleted this chatroom
                getPartner(this_message.getAuthor(), holder);
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }


    /* Other Methods
     ***************************************************************************************************/

    private void setChatRowAppearance(boolean isMe, MessageViewHolder holder){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.tv_author.getLayoutParams();

        if (isMe){
            params.gravity = Gravity.END;
            holder.tv_author.setTextColor(context.getColor(R.color.interaction));
            holder.tv_message.setBackground(ContextCompat.getDrawable(context, R.drawable.bubble1));
        }
        else {
            params.gravity = Gravity.START;
            holder.tv_author.setTextColor(context.getColor(R.color.grey));
            holder.tv_message.setBackground(ContextCompat.getDrawable(context,R.drawable.bubble2));
        }

        holder.tv_author.setLayoutParams(params);
        holder.tv_message.setLayoutParams(params);
        holder.tv_timestamp.setLayoutParams(params);
    }

    public void removeMessage(int position){
        messages.remove(position);
        notifyItemRemoved(position);
    }


    private void getPartner(String author, MessageViewHolder holder) {
        mFirestore.document(User.getPath(author)).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                DocumentSnapshot doc = task.getResult();
                if ((boolean) doc.get("is_organization"))
                    partner = task.getResult().toObject(Organization.class);
                else partner = task.getResult().toObject(Student.class);

                if (partner != null) holder.tv_author.setText(partner.getName());
                else Log.e(TAG, "user was null!");

            } else Log.w(TAG, task.getException());
        });
    }



    /* View Holder subclass
     ***************************************************************************************************/

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView tv_author;
        TextView tv_message;
        TextView tv_timestamp;
        LinearLayout layout;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            tv_author = itemView.findViewById(R.id.item_chatmessage_username);
            tv_message = itemView.findViewById(R.id.item_chatmessage_text);
            tv_timestamp = itemView.findViewById(R.id.item_chatmessage_timestamp);
            layout = itemView.findViewById(R.id.item_chatmessage_layout);

            //Set Listeners
            layout.setOnClickListener(v -> {
                if (tv_timestamp.getVisibility() == View.GONE) tv_timestamp.setVisibility(View.VISIBLE);
                else tv_timestamp.setVisibility(View.GONE);
            });
        }
    }
}
