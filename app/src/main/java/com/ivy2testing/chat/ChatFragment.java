package com.ivy2testing.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.ivy2testing.R;
import com.ivy2testing.entities.Chatroom;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.Utils;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Chat Lobby -> View list of chatrooms
 */
public class ChatFragment extends Fragment implements LobbyAdapter.OnChatroomClickListener{
    private static final String TAG = "ChatFragment";

    // Views
    private final Context context;

    // RecyclerView
    private RecyclerView rv_chat_rooms;
    private LobbyAdapter adapter;
    private ExtendedSortedList<Chatroom> chatrooms;

    // Firebase
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private final ArrayList<ListenerRegistration> list_regs = new ArrayList<>();

    // Other Values
    private User this_user;
    private int selected_chatroom_index = -1;

    // Constructor
    public ChatFragment(Context con, User thisUser) {
        context = con;
        if(thisUser != null) this_user = thisUser;
    }


/* Overridden Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root_view = inflater.inflate(R.layout.fragment_chat, container, false);

        if (this_user != null) {
            rv_chat_rooms = root_view.findViewById(R.id.lobby_recyclerview);
            initRecycler();
        }
        else Log.e(TAG, "User parcel was null!");

        return root_view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setChatroomListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        for (ListenerRegistration list_reg : list_regs)
            list_reg.remove();  // No need to listen if you're not there //TODO remove?
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // Coming back from ChatroomActivity
            if (requestCode == Constant.CHATROOM_REQUEST) {
                selected_chatroom_index = -1;
            /*// Reorder Chatroom lists
            Util.reorderItem(chatrooms, selected_chatroom_index, 0);
            selected_chatroom_index = -1;

            // Update Chatroom and adapter
            adapter.notifyDataSetChanged();*/
            }


            // Go to chatroom after it is ADDED by listener
            if (requestCode ==Constant.NEWCHATROOM_REQUEST)
                selected_chatroom_index = chatrooms.size();
        }
    }


/* Initialization Methods
***************************************************************************************************/

    private void initRecycler() {
        adapter = new LobbyAdapter(this_user, this);
        chatrooms = getChatroomSortedList();
        adapter.setChatrooms(chatrooms);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
        rv_chat_rooms.setLayoutManager(manager);
        rv_chat_rooms.setAdapter(adapter);
    }


/* OnClick Methods
***************************************************************************************************/

    // Go to chatroom
    @Override
    public void onShortClick(int position, User partner) {
        if (position < 0) return;
        selected_chatroom_index = position;
        Intent intent;
        intent = new Intent(context, ChatroomActivity.class);

        intent.putExtra("chatroom", chatrooms.get(position));
        intent.putExtra("this_user", this_user);
        intent.putExtra("partner", partner);
        startActivityForResult(intent, Constant.CHATROOM_REQUEST);
    }


    // TODO: use popup window instead?
    //  Remove altogether? Or change options...
    @Override
    public void onLongClick(int position, View v) {
        /*
        if (!(chatrooms.get(position).getIs_groupChat())) return;
        GroupChat selected_room = (GroupChat) chatrooms.get(position);

        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.roomOptions_changeTitle:
                    changeRoomTitle(selected_room);
                    return true;

                case R.id.roomOptions_delete:
                    deleteChatroom(selected_room.getId());
                    return true;

                case R.id.roomOptions_leave:
                    leaveChatroom(selected_room);
                    return true;
            }
            return false;
        });
        popup.inflate(R.menu.chatroom_options);
        popup.setGravity(Gravity.END);

        // Adjust view displays
        Menu menu = popup.getMenu();
        Util.colorMenuItem(menu.findItem(R.id.roomOptions_delete), getColor(R.color.red));
        if (!this_user.getUsername().equals(selected_room.getHost()))   // Only host can delete room
            menu.findItem(R.id.roomOptions_delete).setVisible(false);

        popup.show();*/
    }


    /* Transition Methods
     ***************************************************************************************************/

    // TODO: some loading mechanism?
    public void refreshAdapter(){
        //TODO
    }


    /* Firebase related Methods
     ***************************************************************************************************/

    private void setChatroomListener(){
        list_regs.add(
                mFirestore.collection("conversations")
                        .whereArrayContains("members", this_user.getId())
                        .addSnapshotListener((queryDocumentSnapshots, e) -> {
                            if (e != null) Log.w(TAG, "Error in attaching listener.", e);
                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {

                                    // Get Chatroom object from results
                                    Chatroom chatroom = docChange.getDocument().toObject(Chatroom.class);
                                    chatroom.setId(docChange.getDocument().getId());

                                    // Update RecyclerView Adapter base on type of change
                                    if (docChange.getType() == DocumentChange.Type.ADDED) addNewChatroom(chatroom);
                                    else if (docChange.getType() == DocumentChange.Type.REMOVED) removeChatroom(chatroom);
                                    else updateChatroom(chatroom); // MODIFIED
                                } Log.d(TAG, queryDocumentSnapshots.size() + " rooms uploaded!");
                            }
                        }));
    }

    // Sets a listener to latest message in a chatroom so time_stamp gets updated real time
    // Then, it adds the updated chatroom to the sorted list
    private void addNewChatroom(Chatroom chatroom) {
        Log.d(TAG, "Document added: " + chatroom.getId());
        String address = "conversations/" + chatroom.getId() + "/messages";

        list_regs.add(
                mFirestore.collection(address)
                        .orderBy("time_stamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .addSnapshotListener((queryDocumentSnapshots, e) -> {
                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                DocumentChange docChange = queryDocumentSnapshots.getDocumentChanges().get(0);
                                Long time_stamp = (Long) docChange.getDocument().get("time_stamp");

                                // Set chatroom time_stamp locally
                                if (time_stamp != null) chatroom.setLast_message_timestamp(time_stamp);
                            }
                            // Update chatroom if already exists
                            int position = chatrooms.indexOf(chatroom);
                            if (position < 0) position = chatrooms.findIndexById(chatroom); // Try finding by ID if can't find it
                            if (position >= 0) chatrooms.updateItemAt(position, chatroom);
                            else chatrooms.add(chatroom);
                            Log.d(TAG, chatroom.getId() + " ADDED CHATROOM POSITION: " + position);
                        }));
    }

    //TODO needs more testing
    private void updateChatroom(Chatroom chatroom) {
        Log.d(TAG, "Document modified: " + chatroom.getId());

        // Get Existing Message position
        int position = chatrooms.indexOf(chatroom);
        if (position < 0) position = chatrooms.findIndexById(chatroom); // Try finding by ID if can't find it
        if (position >= 0) chatrooms.updateItemAt(position, chatroom);
        else Log.e(TAG, "Chatroom not found in list! " + chatroom.getId());
    }

    // TODO needs testing
    private void removeChatroom(Chatroom chatroom) {
        Log.d(TAG, "Document removed: " + chatroom.getId());
        chatrooms.remove(chatroom);
    }




    /* Chatroom Option Methods (also used in ChatroomActivity)
     ***************************************************************************************************/

    // Delete room entirely
    private void deleteChatroom(String chatroom_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.deleteRoom_message))
                .setPositiveButton("Confirm", (dialog, which) ->
                        mFirestore.collection("conversations").document(chatroom_id)
                                .delete().addOnCompleteListener(Utils.getSimpleOnCompleteListener(
                                context,
                                getString(R.string.deleteRoom),
                                getString(R.string.error_deleteRoom))))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }



    /* Utility Methods
     ***************************************************************************************************/

    public ExtendedSortedList<Chatroom> getChatroomSortedList(){
        return new ExtendedSortedList<>(Chatroom.class, new SortedList.Callback<Chatroom>() {
            @Override
            public void onInserted(int position, int count) {
                adapter.notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                adapter.notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                adapter.notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                adapter.notifyItemRangeChanged(position, count);
            }

            @Override
            public int compare(Chatroom o1, Chatroom o2) {
                int result = -1;
                if (areItemsTheSame(o1, o2)) result = 0;
                else if (o1.getLast_message_timestamp() != null && o2.getLast_message_timestamp() != null)
                        result = o1.getLast_message_timestamp().compareTo(o2.getLast_message_timestamp());

                Log.d(TAG, "o1: " + o1.getId() + ", o2: " + o2.getId() + ". RESULT: " + result);

                return result;
            }

            @Override
            public boolean areContentsTheSame(Chatroom oldItem, Chatroom newItem) {
                boolean same_timestamp = true;
                if (newItem.getLast_message_timestamp() != null)
                    same_timestamp = newItem.getLast_message_timestamp().equals(oldItem.getLast_message_timestamp());

                return same_timestamp;
            }

            @Override
            public boolean areItemsTheSame(Chatroom item1, Chatroom item2) {
                if (item1 == null && item2 == null) return true;
                else if (item1 == null || item2 == null) return false;
                else return item1.getId().equals(item2.getId());
            }
        });
    }

}
