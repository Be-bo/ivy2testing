package com.ivy2testing.bubbletabs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ExploreEventsAdapter extends RecyclerView.Adapter<ExploreEventsViewHolder> {





    // MARK: Base

    private static final int NEW_BATCH_TOLERANCE = 5;
    private List<Event> events = new ArrayList<>();
    private Context context;
    private AllEventsItemClickListener listener;
    private DocumentSnapshot last_retrieved_document;
    private boolean load_in_progress = false;
    private boolean loaded_all_events = false;
    private Query query;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference stor = FirebaseStorage.getInstance().getReference();

    public ExploreEventsAdapter(Context con, AllEventsItemClickListener listenr){
        context = con;
        listener = listenr;
        query = db.collection("universities").document(Utils.getCampusUni(context)).collection("posts")
                .whereEqualTo("is_event", true).whereGreaterThan("start_millis", System.currentTimeMillis())
                .orderBy("start_millis", Query.Direction.ASCENDING).limit(Constant.ALL_EVENTS_LOAD_LIMIT);
        fetchEventBatch();
    }

    private boolean eventAlreadyAdded(String eventId){
        for(Event current: events){
            if(current.getId().equals(eventId)) return true;
        }
        return false;
    }

    public Event getItem(int position){
        return events.get(position);
    }











    // MARK: Database

    private void fetchEventBatch(){
        load_in_progress = true;
        query.get().addOnCompleteListener(querySnap -> {
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {
                if (!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot newEvent = querySnap.getResult().getDocuments().get(i);
                        Event event = newEvent.toObject(Event.class);
                        if(event != null && event.isIs_active() && !eventAlreadyAdded(event.getId())) events.add(event); //add if not null and not featured

                        if (i >= querySnap.getResult().getDocuments().size() - 1) last_retrieved_document = newEvent;
                    }

                    if(events.size() < 1){ //if the size is still 0 we need to check for the next batch (because if size 0 onBindViewHolder won't get called)
                        if(last_retrieved_document != null && !loaded_all_events){
                            query = query.startAfter(last_retrieved_document);
                            fetchEventBatch(); //next batch has to be loaded from where the previous one left off
                        }
                    }
                    notifyDataSetChanged();
                }
//                else loadedAllPosts();
            }
            load_in_progress = false;
        });
    }

    public interface AllEventsItemClickListener{
        void onEventClick(int position);
    }








    // MARK: Override Methods

    @NonNull
    @Override
    public ExploreEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_view_all, parent, false);
        return new ExploreEventsViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreEventsViewHolder holder, int position) {
        Event currentEvent = events.get(position);
        holder.name.setText(currentEvent.getName());
        stor.child(ImageUtils.getPostPreviewPath(currentEvent.getId())).getDownloadUrl().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null) Glide.with(context).load(task.getResult()).into(holder.image);
        });

        if(!load_in_progress && position >= (events.size() - NEW_BATCH_TOLERANCE)){ //new batch tolerance means within how many last items do we want to start loading the next batch (i.e. we have 20 items and tolerance 2 -> the next batch will start loading once the user scrolls to the position 18 or 19)
            if(last_retrieved_document != null && !loaded_all_events){
                query = query.startAfter(last_retrieved_document);
                fetchEventBatch();
            }
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
