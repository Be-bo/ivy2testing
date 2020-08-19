package com.ivy2testing.bubbletabs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.ivy2testing.entities.Post;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {





    // MARK: Base

    private List<Event> events;
    private TextView title;
    private RecyclerView recycler;
    private int type;
    private String campus_domain;
    private Context context;
    private EventClickListener event_listener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference stor = FirebaseStorage.getInstance().getReference();
    private Query query;

    public EventAdapter(Context con, int typ, String domain, EventClickListener listener, RecyclerView rec, TextView titl){
        this.events = new ArrayList<>();
        this.title = titl;
        this.recycler = rec;
        this.type = typ;
        this.context = con;
        this.campus_domain = domain;
        this.event_listener = listener;
        fetchEvents();
    }

    public interface EventClickListener {
        void onEventClick(int position, int viewId, int adapterType);
    }

    public Event getItem(int position){
        if(position < events.size()) return events.get(position);
        else return null;
    }

    public void refreshAdapter(){
        if(query != null){
            runQuery(query);
        }
    }











    // MARK: Database Related

    private void fetchEvents(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 24, 0);
        long todayMidnightMillis = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0); //set all of these to the start of the day
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeInMillis(System.currentTimeMillis() + Constant.MILLIS_IN_A_WEEK); //set to this day but exactly 1 week in the future
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()); //get the first day of the next week
        long endOfThisWeekMillis = cal.getTimeInMillis();

        switch (type){
            case Constant.EVENT_ADAPTER_FOR_YOU:
                //TODO: membership, said going, authored?
                break;
            case Constant.EVENT_ADAPTER_TODAY:
                query = db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("is_event", true).whereGreaterThan("start_millis", System.currentTimeMillis())
                        .whereLessThan("start_millis", todayMidnightMillis).orderBy("start_millis", Query.Direction.ASCENDING);
                runQuery(query);
                break;
            case Constant.EVENT_ADAPTER_THIS_WEEK:
                query = db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("is_event", true).whereGreaterThan("start_millis", todayMidnightMillis)
                        .whereLessThan("start_millis", endOfThisWeekMillis).orderBy("start_millis", Query.Direction.ASCENDING);
                runQuery(query);
                break;
            case Constant.EVENT_ADAPTER_UPCOMING:
                query = db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("is_event", true).whereGreaterThan("start_millis", endOfThisWeekMillis)
                        .orderBy("start_millis", Query.Direction.ASCENDING).limit(Constant.EVENT_ADAPTER_UPCOMING_LIMIT);
                runQuery(query);
                break;
        }
    }

    private void runQuery(Query query){
        query.get().addOnCompleteListener(querySnap -> {
           if(querySnap.isSuccessful() && querySnap.getResult() != null && !querySnap.getResult().isEmpty()){
               showElems();
               for(DocumentSnapshot docSnap: querySnap.getResult()){
                   Event event = docSnap.toObject(Event.class);
                   if(event != null && event.isIs_active() && !event.isIs_featured() && !eventAlreadyAdded(event.getId())) events.add(event);
               }
               notifyDataSetChanged();
           }else hideElems();
        });
    }

    private void hideElems(){
        title.setVisibility(View.GONE);
        recycler.setVisibility(View.GONE);
    }

    private void showElems(){
        title.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.VISIBLE);
    }

    private boolean eventAlreadyAdded(String eventId){
        for(Event current: events){
            if(current.getId().equals(eventId)) return true;
        }
        return false;
    }












    // MARK: Override Methods

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view, event_listener, type);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentEvent = events.get(position);
        holder.event_name.setText(currentEvent.getName());
        String authorPreviewImage = ImageUtils.getUserImagePreviewPath(currentEvent.getAuthor_id());
        stor.child(authorPreviewImage).getDownloadUrl().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null) Glide.with(context).load(task.getResult()).into(holder.author_image);
        });
        stor.child(currentEvent.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null) Glide.with(context).load(task.getResult()).into(holder.event_image);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
