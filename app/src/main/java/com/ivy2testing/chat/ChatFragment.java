package com.ivy2testing.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.ivy2testing.R;
import com.ivy2testing.entities.Chatroom;
import com.ivy2testing.entities.Message;
import com.ivy2testing.entities.User;
import com.ivy2testing.userProfile.OrganizationProfileActivity;
import com.ivy2testing.userProfile.StudentProfileActivity;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.Utils;
import com.ivy2testing.util.adapters.WrapContentLinearLayoutManager;

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
    private TextView tv_no_chat;
    private LobbyAdapter adapter;
    private ExtendedSortedList<Chatroom> chatrooms;
    private Chatroom selectedRoom;

    // Firebase
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private final ArrayList<ListenerRegistration> list_regs = new ArrayList<>();

    // Other Values
    private User this_user;

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
            tv_no_chat = root_view.findViewById(R.id.lobby_noChatrooms);
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
            list_reg.remove();  // No need to listen if you're not there
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Chatroom was deleted
        if (resultCode == RESULT_OK && requestCode == Constant.CHATROOM_REQUEST) {
            if (selectedRoom != null) removeChatroom(selectedRoom);
        }
    }

    /* Initialization Methods
***************************************************************************************************/

    private void initRecycler() {
        adapter = new LobbyAdapter(this_user, this, context);
        chatrooms = getChatroomSortedList();
        adapter.setChatrooms(chatrooms);
        RecyclerView.LayoutManager manager = new WrapContentLinearLayoutManager(context);
        rv_chat_rooms.setLayoutManager(manager);
        rv_chat_rooms.setAdapter(adapter);
    }


/* OnClick Methods
***************************************************************************************************/

    // Go to chatroom
    @Override
    public void onShortClick(int position, User partner) {
        Intent intent;
        intent = new Intent(context, ChatroomActivity.class);
        selectedRoom = chatrooms.get(position);

        intent.putExtra("chatroom", selectedRoom);
        intent.putExtra("this_user", this_user);
        intent.putExtra("partner", partner);
        startActivityForResult(intent, Constant.CHATROOM_REQUEST);
    }


    @Override
    public void onLongClick(int position, User partner, View v) {

        PopupMenu popup = new PopupMenu(context, v);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.roomNavOptions_profile)
                viewUserProfile(partner);
            else if (item.getItemId() == R.id.roomNavOptions_delete)
                deleteChatRoom(partner, chatrooms.get(position));
            else return false;
            return true;
        });
        popup.inflate(R.menu.chatroom_nav_options);
        popup.setGravity(Gravity.END);

        // Adjust view displays
        Menu menu = popup.getMenu();
        Utils.colorMenuItem(menu.findItem(R.id.roomNavOptions_delete), context.getColor(R.color.red));

        popup.show();
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
                                tv_no_chat.setVisibility(View.GONE);

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
                            else if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty())
                                tv_no_chat.setVisibility(View.VISIBLE);
                        }));
    }

    // Sets a listener to latest message in a chatroom so time_stamp gets updated real time
    // Then, it adds the updated chatroom to the sorted list
    private void addNewChatroom(Chatroom chatroom) {
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
                            int position = findChatroomIndex(chatroom);
                            if (position >= 0) {
                                chatrooms.updateItemAt(position, chatroom);
                                adapter.notifyDataSetChanged();
                            } else adapter.notifyItemInserted(chatrooms.add(chatroom));
                        }));
    }

    private void updateChatroom(Chatroom chatroom) {

        // Get Existing Message position
        int position = findChatroomIndex(chatroom);
        if (position >= 0) {
            chatrooms.updateItemAt(position, chatroom);
            adapter.notifyDataSetChanged();
        }
        else Log.e(TAG, "Chatroom not found in list! " + chatroom.getId());
    }

    private void removeChatroom(Chatroom chatroom) {
        // Get Existing Message position
        int position = findChatroomIndex(chatroom);
        if (position >= 0) {
            chatrooms.removeItemAt(position);
            adapter.notifyItemRemoved(position);

            // Need full refresh of this fragment to update views
            onStop();
            onStart();

        }
        else Log.e(TAG, "Chatroom not found in list! " + chatroom.getId());
    }

    // Util function for the above functions
    private int findChatroomIndex(Chatroom chatroom) {
        int position = chatrooms.indexOf(chatroom);
        if (position < 0) position = chatrooms.findIndexById(chatroom); // Try finding by ID if can't find it
        return position;
    }



    /* Chatroom Option Methods (also used in ChatroomActivity)
     ***************************************************************************************************/

    // View User Profile
    private void viewUserProfile(User partner) {
        Intent intent;
        if (partner.getIs_organization()) {
            Log.d(TAG, "Starting OrganizationProfile Activity for organization " + partner.getId());
            intent = new Intent(context, OrganizationProfileActivity.class);
            intent.putExtra("org_to_display_id",  partner.getId());
            intent.putExtra("org_to_display_uni", partner.getUni_domain());
        } else {
            Log.d(TAG, "Starting StudentProfile Activity for student " +  partner.getId());
            intent = new Intent(context, StudentProfileActivity.class);
            intent.putExtra("student_to_display",  partner);
        }
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }

    // Confirmation dialog
    private void deleteChatRoom(User partner, Chatroom room) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.deleteRoom_title))
                .setMessage(getString(R.string.deleteRoom_message))
                .setPositiveButton("Confirm", (dialog, which) -> deleteChatRoomFromDB(partner, room))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    // Delete Chatroom completely and return to Lobby
    private void deleteChatRoomFromDB(User partner, Chatroom room) {

        // Remove from messaging list
        mFirestore.document(User.getPath(this_user.getId()))
                .update("messaging_users", FieldValue.arrayRemove(partner.getId()));

        // Remove user from chatroom members. if empty list -> delete document
        DocumentReference chatroomDoc = mFirestore.collection("conversations").document(room.getId());
        chatroomDoc.update("members", FieldValue.arrayRemove(this_user.getId()))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatroomDoc.get().addOnCompleteListener( task1 -> {
                            if (task1.isSuccessful() && task1.getResult() != null) {

                                // Make sure it's removed from adapter
                                removeChatroom(room);

                                // Delete if no members left, else do nothing
                                Chatroom updatedRoom = task1.getResult().toObject(Chatroom.class);
                                if (updatedRoom != null && updatedRoom.getMembers().isEmpty()) {
                                    chatroomDoc.delete().addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            Toast.makeText(context, getString(R.string.deleteRoom), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, getString(R.string.error_deleteRoom), Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, getString(R.string.error_deleteRoom), task2.getException());
                                        }
                                    });
                                } else {
                                    // Send a user left message
                                    Message msg = new Message(this_user.getId(), this_user.getName() + " left the Conversation.");
                                    chatroomDoc.collection("messages").document(msg.getId()).set(msg);
                                    Toast.makeText(context, getString(R.string.deleteRoom), Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(context, getString(R.string.error_deleteRoom), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Couldn't retrieve updated chatroom", task1.getException());
                            }
                        });
                    } else {
                        Toast.makeText(context, getString(R.string.error_deleteRoom), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Couldn't remove member from chatroom.", task.getException());
                    }
                });
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
