package com.ivy2testing.chat;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy2testing.R;
import com.ivy2testing.entities.Chatroom;
import com.ivy2testing.entities.Message;
import com.ivy2testing.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Main messaging area fragment responsible for obtaining and sending messages
 */
public class MessagingFragment extends Fragment  {
    private static final String TAG = "MessagingFragment";

    // Views
    private View root_view;
    private RecyclerView rv_messages;
    private EditText et_message;
    private ImageButton send_button;

    // RecyclerView
    RoomAdapter adapter;
    List<Message> messages = new ArrayList<>();
    private LinearLayoutManager layout_man;
    private static final int PAGE_LIMIT = 10;
    private DocumentSnapshot last_doc;                  // Snapshot of last message loaded
    private boolean message_list_updated;

    // Firebase
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    ListenerRegistration listener;      // Snapshot Listener for new messages only

    // Other Values
    private String chatroom_messages_address;
    private Chatroom this_chatroom;
    private User this_user;


    // Constructor
    public MessagingFragment(Chatroom chatroom, User this_user){
        this.this_chatroom = chatroom;
        this.this_user = this_user;
        if (chatroom != null)
            chatroom_messages_address = "conversations/" + this_chatroom.getId() + "/messages";
    }


    /* Overridden Methods
     ***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_messaging, container, false);

        // Initialization Methods
        if (this_user != null && !chatroom_messages_address.contains("null")){
            initViews();
            setListeners();
            initRecycler();
        } else Log.e(TAG, "A parcel was null!");

        return root_view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) listener.remove();      // Detach Listener
    }

    /* Initialization Methods
     ***************************************************************************************************/

    private void initViews() {
        rv_messages = root_view.findViewById(R.id.room_recyclerview);
        et_message = root_view.findViewById(R.id.room_writeMessage);
        send_button = root_view.findViewById(R.id.room_sendButton);
    }

    private void setListeners(){
        Activity activity = getActivity();
        if (activity == null) {
            Log.e(TAG, "Null Activity");
            return;
        }

        // SEND OnClicks: Send Message if pressed [ENTER]
        send_button.setOnClickListener(this::sendMessage);
        et_message.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.room_sendButton || id == EditorInfo.IME_NULL) {
                sendMessage(textView);
                return true;
            } else return false;
        });

        // Disable send button if no message to send
        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                send_button.setClickable(!et_message.getText().toString().trim().isEmpty());
                if (send_button.isClickable()) send_button.setColorFilter(activity.getColor(R.color.interaction));
                else send_button.setColorFilter(activity.getColor(R.color.grey));

                // Scroll to bottom while editing text ? TODO only when keyboard comes up?
                if (layout_man.findFirstCompletelyVisibleItemPosition() != 0) rv_messages.smoothScrollToPosition(0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initRecycler(){

        adapter = new RoomAdapter(messages, this_user.getId());
        layout_man = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,true);
        layout_man.setStackFromEnd(true);   // Always show bottom of recycler
        rv_messages.setLayoutManager(layout_man);
        rv_messages.setAdapter(adapter);

        getMessagesFromDB();

        // Scroll Listener used for pagination
        rv_messages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (message_list_updated) {
                    if (layout_man.findLastCompletelyVisibleItemPosition() > (messages.size() - 2 )){
                        message_list_updated = false;
                        Log.d(TAG, "Update Messages!!!");
                        getMessagesFromDB();
                    }
                }
            }
        });
    }

/* OnClick Methods
***************************************************************************************************/

    public void sendMessage(View view) {
        String text = et_message.getText().toString().trim();
        if (text.isEmpty()) return;

        messagePending(true);   // Disable sending message again
        Message newMessage = new Message(this_user.getId(), text);
        et_message.setText(null);
        sendMessageToDB(newMessage);
    }

/* Transition Methods
***************************************************************************************************/

// TODO: some loading mechanism?

    private void messagePending(boolean load){
        ProgressBar send_pending = root_view.findViewById(R.id.room_sendLoading);

        // Start loading
        if (load){
            send_button.setVisibility(View.INVISIBLE);
            send_button.setClickable(false);
            send_pending.setVisibility(View.VISIBLE);
        }
        // Stop loading
        else {
            send_button.setVisibility(View.VISIBLE);
            send_button.setClickable(true);
            send_pending.setVisibility(View.GONE);
        }
    }

    // Show a message if there are no messages to display
    private void displayMessages(boolean messagesExist){
        if (messagesExist) root_view.findViewById(R.id.room_noMessagesError).setVisibility(View.GONE);
        else root_view.findViewById(R.id.room_noMessagesError).setVisibility(View.VISIBLE);
    }


    /* Firebase related Methods
     ***************************************************************************************************/

    // implements pagination + snapshot listeners = [big mess...]
    private void getMessagesFromDB() {
        // Build Query
        Log.d(TAG, chatroom_messages_address);
        Query query = mFirestore.collection(chatroom_messages_address)
                .orderBy("time_stamp", Query.Direction.DESCENDING)
                .limit(PAGE_LIMIT);
        if (last_doc != null) query = query.startAfter(last_doc);

        // Get Request
        Query finalQuery = query;
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {

                QuerySnapshot query_doc = task.getResult();

                // Get messages and add to recycler
                for (QueryDocumentSnapshot doc : query_doc){
                    Message message = doc.toObject(Message.class);
                    message.setId(doc.getId());
                    messages.add(message);
                    adapter.notifyItemInserted(messages.size()-1);
                } Log.d(TAG, query_doc.size() + " messages were uploaded from database!");

                // Skip rest if there weren't any messages
                if (query_doc.isEmpty())
                    displayMessages(false);

                else { // Update pagination and add snapshot listener
                    DocumentSnapshot first = query_doc.getDocuments().get(0);
                    DocumentSnapshot last = query_doc.getDocuments().get(query_doc.size()-1);

                    // Only listen to new upcoming messages
                    if (last_doc == null) { // This is the first get Request
                        displayMessages(true);
                        listener = finalQuery.endBefore(first).addSnapshotListener(getListener());
                        rv_messages.scrollToPosition(0); // Scroll to bottom on first get
                    }

                    // Update pagination data
                    last_doc = last;             // Save last doc retrieved
                    message_list_updated = true;
                }
            } else Log.e(TAG, "Couldn't retrieve messages.", task.getException());
        });
    }

    // Get an event listener depending on if we want to add new messages
    private EventListener<QuerySnapshot> getListener() {
        return (queryDocumentSnapshots, e) -> {
            if (e != null) Log.w(TAG, "Error in attaching listener.", e);
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {

                    // Get Message
                    Message message = docChange.getDocument().toObject(Message.class);
                    message.setId(docChange.getDocument().getId());

                    // Update RecyclerView Adapter base on type of change
                    if (docChange.getType() == DocumentChange.Type.ADDED){
                        Log.d(TAG, "Document added.");
                        messages.add(0, message);
                        adapter.notifyItemInserted(0);
                        rv_messages.scrollToPosition(0);
                    }
                    else {
                        // Get Existing Message position
                        int position = messages.indexOf(message);
                        if (position < 0) {
                            Log.e(TAG, "Message not found in list! " + message.getId());
                            break;
                        }
                        // REMOVED ?
                        if (docChange.getType() == DocumentChange.Type.REMOVED){
                            Log.d(TAG, "Document removed.");
                            adapter.removeMessage(position);
                        }
                        else { // MODIFIED
                            Log.d(TAG, "Document modified.");
                            messages.get(position).setText(message.getText());
                            adapter.notifyItemChanged(position);
                        }
                    }
                }
            }
        };
    }


/* Message Options
***************************************************************************************************/

    // Send a single message to database
    private void sendMessageToDB(Message newMessage) {
        mFirestore.collection(chatroom_messages_address).add(newMessage).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) Log.e(TAG, "Couldn't send message!", task.getException());
            else if (listener == null) getMessagesFromDB();
            messagePending(false);
        });
    }
}
