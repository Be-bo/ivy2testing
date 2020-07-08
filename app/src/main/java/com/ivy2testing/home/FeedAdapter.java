package com.ivy2testing.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {



    // MARK: Variables

    private static final String TAG = "FeedAdapterTag";

    private static final int BATCH_LIMIT = 15;
    private static final int NEW_BATCH_TOLERANCE = 4;
    private ArrayList<Post> post_array_list;
    private FeedClickListener feed_click_listener;
    private int adapter_type;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();

    private DocumentSnapshot last_retrieved_document;
    private boolean loaded_all_posts = false;
    private boolean load_in_progress = false;
    private Query default_query;
    private String campus_domain;
    private String seeall_author_id = "";

    private Context context;
    private long last_pull_millis = 0;
    private TextView empty_adapter_text;
    private TextView reached_bottom_text;










    //MARK: Base

    public FeedAdapter(FeedClickListener feed_click_listener, int type, String campusDomain, String seeallAuthorId, Context con, TextView emptyAdapterText, TextView reachedBottomText) {
        this.post_array_list = new ArrayList<>();
        this.feed_click_listener = feed_click_listener;
        this.adapter_type = type;
        this.campus_domain = campusDomain;
        this.seeall_author_id = seeallAuthorId;
        this.context = con;
        this.last_pull_millis = System.currentTimeMillis();
        this.empty_adapter_text = emptyAdapterText;
        this.reached_bottom_text = reachedBottomText;

        switch (adapter_type){ //decide which type of adapter we're using based on its purpose
            case Constant.FEED_ADAPTER_CAMPUS:
                default_query = db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("main_feed_visible", true).orderBy("creation_millis", Query.Direction.DESCENDING).limit(BATCH_LIMIT);
                fetchMixedBatch(default_query);
                break;
            case Constant.FEED_ADAPTER_EVENTS:
                default_query = db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("main_feed_visible", true).whereEqualTo("is_event", true)
                        .whereGreaterThan("start_millis", System.currentTimeMillis()).orderBy("start_millis", Query.Direction.ASCENDING).limit(BATCH_LIMIT);
                fetchEventBatch(default_query);
                break;
            case Constant.FEED_ADAPTER_SEEALL:
                default_query = db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("author_id", seeall_author_id).orderBy("creation_millis", Query.Direction.DESCENDING).limit(BATCH_LIMIT);
                fetchMixedBatch(default_query);
                break;
        }
    }

    public interface FeedClickListener {
        void onFeedClick(int position, int clicked_id);
    }













    //MARK: Override

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_feed_post, parent, false);
        return new FeedViewHolder(v, feed_click_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Post thisPost = post_array_list.get(position);
        
        if(thisPost.getIs_event()){
            Event thisEvent = (Event) thisPost;
            holder.feed_title.setText(thisEvent.getName());
            holder.feed_title.setVisibility(View.VISIBLE);
            holder.feed_text.setVisibility(View.GONE);
        }else{
            holder.feed_text.setText(thisPost.getText());
            holder.feed_text.setVisibility(View.VISIBLE);
            holder.feed_title.setVisibility(View.GONE);
        }

        if (!thisPost.getPinned_id().equals("")){
            holder.feed_pinned_name.setVisibility(View.VISIBLE);
            holder.pin_icon.setVisibility(View.VISIBLE);
            holder.feed_pinned_name.setText(thisPost.getPinned_name());
        }else{
            holder.feed_pinned_name.setVisibility(View.GONE);
            holder.pin_icon.setVisibility(View.GONE);
        }

        if (thisPost.getVisual().contains("/")) {
            holder.feed_image_view.setVisibility(View.VISIBLE);
            getPicFromDB(holder, thisPost);
        } else holder.feed_image_view.setVisibility(View.GONE);

        if (thisPost instanceof Event){
            holder.banner.setVisibility(View.VISIBLE);
            holder.banner_text.setVisibility(View.VISIBLE);
        }else{
            holder.banner.setVisibility(View.GONE);
            holder.banner_text.setVisibility(View.GONE);
        }

        holder.feed_text.setText(thisPost.getText());
        holder.feed_author.setText(thisPost.getAuthor_name());
        String previewImgPath = "userfiles/"+thisPost.getAuthor_id()+"/previewimage.jpg";
        db_storage.child(previewImgPath).getDownloadUrl().addOnCompleteListener(task -> {Glide.with(context).load(task.getResult()).into(holder.author_preview_image);});

        if(!load_in_progress && position >= (post_array_list.size() - NEW_BATCH_TOLERANCE)){ //new batch tolerance means within how many last items do we want to start loading the next batch (i.e. we have 20 items and tolerance 2 -> the next batch will start loading once the user scrolls to the position 18 or 19)
            if(last_retrieved_document != null && !loaded_all_posts){
                switch(adapter_type){
                    case Constant.FEED_ADAPTER_CAMPUS:
                        fetchMixedBatch(default_query.startAfter(last_retrieved_document)); //next batch has to be loaded from where the previous one left off
                        break;
                    case Constant.FEED_ADAPTER_EVENTS:
                        fetchEventBatch(default_query.startAfter(last_retrieved_document)); //next batch has to be loaded from where the previous one left off
                        break;
                    case Constant.FEED_ADAPTER_SEEALL:
                        fetchMixedBatch(default_query.startAfter(last_retrieved_document)); //next batch has to be loaded from where the previous one left off
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return post_array_list.size();
    }












    //MARK: Database

    private void fetchMixedBatch(Query query){ //fetch all posts (events and standard posts) combined based on the input query
        load_in_progress = true;
        query.get().addOnCompleteListener(querySnap -> {
            if(querySnap.isSuccessful() && querySnap.getResult() != null) {
                if(!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot newPost = querySnap.getResult().getDocuments().get(i);

                        if(newPost.get("is_event") instanceof  Boolean && (Boolean) newPost.get("is_event")){ //it's an event
                            Event event = newPost.toObject(Event.class);
                            if(event != null) post_array_list.add(event);
                        }else{ //it ain't
                            Post post = newPost.toObject(Post.class);
                            if(post != null) post_array_list.add(post);
                        }

                        if (i >= querySnap.getResult().getDocuments().size() - 1) last_retrieved_document = newPost;
                    }

                    if(post_array_list.size() < 1){ //if the size is still 0 we need to check for the next batch (because if size 0 onBindViewHolder won't get called)
                        if(last_retrieved_document != null && !loaded_all_posts){
                            fetchMixedBatch(default_query.startAfter(last_retrieved_document)); //next batch has to be loaded from where the previous one left off
                        }
                    }
                    notifyDataSetChanged();
                }else loadedAllPosts(); //no more data to load
            }
            checkEmptyAdapter();
            load_in_progress = false;
        });
    }

    private void fetchEventBatch(Query query){ //fetch only events for the given query
        load_in_progress = true;
        query.get().addOnCompleteListener(querySnap -> {
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {
                if (!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot newEvent = querySnap.getResult().getDocuments().get(i);
                        Event event = newEvent.toObject(Event.class);
                        if(event != null && !event.isIs_featured() && event.isIs_active()) post_array_list.add(event); //add if not null and not featured

                        if (i >= querySnap.getResult().getDocuments().size() - 1) last_retrieved_document = newEvent;
                    }

                    if(post_array_list.size() < 1){ //if the size is still 0 we need to check for the next batch (because if size 0 onBindViewHolder won't get called)
                        if(last_retrieved_document != null && !loaded_all_posts){
                            fetchEventBatch(default_query.startAfter(last_retrieved_document)); //next batch has to be loaded from where the previous one left off
                        }
                    }
                    notifyDataSetChanged();
                }
                else loadedAllPosts();
            }
            checkEmptyAdapter();
            load_in_progress = false;
        });
    }












    // MARK: Refreshing

    public void refreshPosts() { //load everything the user has missed since they last pulled
        switch (adapter_type) {
            case Constant.FEED_ADAPTER_CAMPUS:
                refreshMixed(db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("main_feed_visible", true)
                        .whereGreaterThan("creation_millis", last_pull_millis).orderBy("creation_millis", Query.Direction.ASCENDING));
                break;
            case Constant.FEED_ADAPTER_EVENTS:
                refreshEvents(db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("is_event",true)
                        .whereEqualTo("main_feed_visible", true).whereGreaterThan("creation_millis", last_pull_millis).orderBy("creation_millis", Query.Direction.ASCENDING));
                break;
            case Constant.FEED_ADAPTER_SEEALL:
                //TODO
                break;
        }
    }

    private void refreshMixed(Query query){
        load_in_progress = true; //just in case
        query.get().addOnCompleteListener(querySnap ->{
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {
                if (!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot newPost = querySnap.getResult().getDocuments().get(i);//add all the missing posts to the top of the arraylist

                        if(newPost.get("is_event") instanceof Boolean && (Boolean) newPost.get("is_event")){
                            Event ev = newPost.toObject(Event.class);
                            if(ev != null && !post_array_list.contains(ev)) post_array_list.add(0, ev);
                        }else{
                            Post pst = newPost.toObject(Post.class);
                            if(pst != null && !post_array_list.contains(pst)) post_array_list.add(0, pst);
                        }
                    }
                    notifyDataSetChanged();
                }
            }
            load_in_progress = false;
        });
    }

    private void refreshEvents(Query query){
        load_in_progress = true; //just in case
        query.get().addOnCompleteListener(querySnap ->{
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {
                if (!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot newEvent = querySnap.getResult().getDocuments().get(i);
                        Event event = newEvent.toObject(Event.class);
                        if(event!= null && !event.isIs_featured() && event.isIs_active() && !post_array_list.contains(event)) post_array_list.add(0, event); //add all the missing events to the top of the arraylist
                    }
                    notifyDataSetChanged();
                }
            }
            load_in_progress = false;
        });
    }















    //MARK: Other

    public void getPicFromDB(FeedViewHolder holder, Post post) {
        String visualPath = post.getVisual();
        db_storage.child(visualPath).getDownloadUrl().addOnCompleteListener(task -> {Glide.with(context).load(task.getResult()).into(holder.feed_image_view);});
    }

    public ArrayList<Post> getPost_array_list() {
        return post_array_list;
    }

    private void checkEmptyAdapter(){
        if(empty_adapter_text != null){
            if(getItemCount() < 1) empty_adapter_text.setVisibility(View.VISIBLE);
            else empty_adapter_text.setVisibility(View.GONE);
        }
    }

    public void loadedAllPosts(){
        loaded_all_posts = true;
        if(getItemCount() > 0) reached_bottom_text.setVisibility(View.VISIBLE);
    }
















    //MARK: ViewHolder

//------------------------------------------------------------------------------------------------------------------------------------
    public static class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        FeedClickListener feed_click_listener;

        public ImageView banner;
        public TextView banner_text;
        public ImageView feed_image_view;
        public TextView feed_title;
        public TextView feed_text;
        public TextView feed_author;
        public CircleImageView author_preview_image;
        public TextView feed_pinned_name;
        public TextView full_text_button;
        public ImageView pin_icon;


        public FeedViewHolder(@NonNull View itemView, FeedClickListener feed_click_listener) {
            super(itemView);
            banner = itemView.findViewById(R.id.item_post_banner);
            banner_text = itemView.findViewById(R.id.item_post_banner_text);
            feed_image_view = itemView.findViewById(R.id.item_feed_image);
            feed_title = itemView.findViewById(R.id.item_feed_event_title);
            feed_text = itemView.findViewById(R.id.item_feed_text);
            feed_author = itemView.findViewById(R.id.item_feed_posted_by_text);
            author_preview_image = itemView.findViewById(R.id.item_feed_author_preview_image);
            feed_pinned_name = itemView.findViewById(R.id.item_feed_pinned_text);
            full_text_button = itemView.findViewById(R.id.item_feed_full_text_button);
            pin_icon = itemView.findViewById(R.id.item_feed_pin_icon);


            this.feed_click_listener = feed_click_listener;
            full_text_button.setOnClickListener(this);
            feed_author.setOnClickListener(this);
            feed_pinned_name.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            feed_click_listener.onFeedClick(getAdapterPosition(), v.getId());
        }
    }
}
