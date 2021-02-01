package com.ivy2testing.hometab;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Post;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.Utils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {



    //MARK: Base

    private static final String TAG = "FeedAdapterTag";

    private static final int BATCH_LIMIT = 15;
    private static final int NEW_BATCH_TOLERANCE = 4;
    private ArrayList<Post> post_array_list;
    private FeedClickListener feed_click_listener;

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
    private ProgressBar progress_bar;
    private RecyclerView recycler;

    public FeedAdapter(FeedClickListener feed_click_listener, String campusDomain, String seeallAuthorId, Context con, TextView emptyAdapterText, TextView reachedBottomText, RecyclerView rec, ProgressBar progressBar) {
        this.post_array_list = new ArrayList<>();
        this.feed_click_listener = feed_click_listener;
        this.campus_domain = campusDomain;
        this.seeall_author_id = seeallAuthorId;
        this.context = con;
        this.last_pull_millis = System.currentTimeMillis();
        this.empty_adapter_text = emptyAdapterText;
        this.reached_bottom_text = reachedBottomText;
        this.progress_bar = progressBar;
        this.recycler = rec;

        default_query = db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("is_event", false).orderBy("creation_millis", Query.Direction.DESCENDING).limit(BATCH_LIMIT);
        fetchPostBatch(default_query);
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

        holder.feed_text.setText(thisPost.getText());
        holder.time_text.setText(Utils.getHumanTimeFromMillis(thisPost.getCreation_millis()));

        if (!thisPost.getPinned_id().equals("")){
            holder.feed_pinned_name.setVisibility(View.VISIBLE);
            holder.pin_icon.setVisibility(View.VISIBLE);
            holder.feed_pinned_name.setText(thisPost.getPinned_name());
        }else{
            holder.feed_pinned_name.setVisibility(View.GONE);
            holder.pin_icon.setVisibility(View.GONE);
        }

        if (thisPost.getVisual() != null && thisPost.getVisual().contains("/")) {
            holder.feed_image_view.setVisibility(View.VISIBLE);
            getPicFromDB(holder, thisPost);
        } else holder.feed_image_view.setVisibility(View.GONE);

        holder.feed_text.setText(thisPost.getText());
        String previewImgPath = ImageUtils.getUserImagePreviewPath(thisPost.getAuthor_id());

        try {
            db_storage.child(previewImgPath).getDownloadUrl().addOnCompleteListener(task -> {
                if(task.isSuccessful() && task.getResult() != null) Glide.with(context).load(task.getResult()).into(holder.author_preview_image);
                else Glide.with(context).load(R.drawable.ic_profile_selected).into(holder.author_preview_image);
            });
        } catch (Exception e){
            Log.w(TAG, "StorageException! No Preview Image for this user.");
        }


        if(!load_in_progress && position >= (post_array_list.size() - NEW_BATCH_TOLERANCE)){ //new batch tolerance means within how many last items do we want to start loading the next batch (i.e. we have 20 items and tolerance 2 -> the next batch will start loading once the user scrolls to the position 18 or 19)
            if(last_retrieved_document != null && !loaded_all_posts){
                fetchPostBatch(default_query.startAfter(last_retrieved_document)); //next batch has to be loaded from where the previous one left off
            }
        }
    }

    @Override
    public int getItemCount() {
        return post_array_list.size();
    }













    //MARK: Database

    private void fetchPostBatch(Query query){ //fetch all posts (events and standard posts) combined based on the input query
        load_in_progress = true;
        query.get().addOnCompleteListener(querySnap -> {
            if(querySnap.isSuccessful() && querySnap.getResult() != null) {
                if(!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot newPost = querySnap.getResult().getDocuments().get(i);
                        Post post = newPost.toObject(Post.class);
                        if(post != null && !postAlreadyAdded(post.getId())) post_array_list.add(post);
                        if (i >= querySnap.getResult().getDocuments().size() - 1) last_retrieved_document = newPost;
                    }

                    if(post_array_list.size() < 1){ //if the size is still 0 we need to check for the next batch (because if size 0 onBindViewHolder won't get called)
                        if(last_retrieved_document != null && !loaded_all_posts){
                            fetchPostBatch(default_query.startAfter(last_retrieved_document)); //next batch has to be loaded from where the previous one left off
                        }
                    }
                    notifyDataSetChanged();
                }else loadedAllPosts(); //no more data to load
            }
            stopLoading();
            checkEmptyAdapter();
            load_in_progress = false;
        });
    }

    private boolean postAlreadyAdded(String postId){
        for(Post current: post_array_list){
            if(current.getId().equals(postId)) return true;
        }
        return false;
    }

    public void refreshPosts() { //load everything the user has missed since they last pulled
        refreshAdapter(db.collection("universities").document(campus_domain).collection("posts").whereEqualTo("is_event", false)
                .whereGreaterThan("creation_millis", last_pull_millis).orderBy("creation_millis", Query.Direction.ASCENDING));
    }

    private void refreshAdapter(Query query){
        load_in_progress = true; //just in case
        query.get().addOnCompleteListener(querySnap ->{
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {
                if (!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot newPost = querySnap.getResult().getDocuments().get(i);//add all the missing posts to the top of the arraylist
                        Post pst = newPost.toObject(Post.class);
                        if(pst != null && !postAlreadyAdded(pst.getId())){
                            post_array_list.add(0, pst);
                        }
                    }
                    notifyDataSetChanged();
                }
            }
            checkEmptyAdapter();
            load_in_progress = false;
        });
    }














    //MARK: Other

    public void getPicFromDB(FeedViewHolder holder, Post post) {
        String visualPath = post.getVisual();
        if(visualPath != null && visualPath.contains("/")){
            RequestOptions myOptions = new RequestOptions().override(300, 300);
            db_storage.child(visualPath).getDownloadUrl().addOnCompleteListener(task -> {if(task.isSuccessful() && task.getResult() != null)Glide.with(context).applyDefaultRequestOptions(myOptions).load(task.getResult()).into(holder.feed_image_view);
            else Glide.with(context).load(R.drawable.ivy_logo).into(holder.feed_image_view);});
        }
    }

    public ArrayList<Post> getPost_array_list() {
        return post_array_list;
    }

    private void checkEmptyAdapter(){
        if(empty_adapter_text != null){
            if(getItemCount() < 1){
                empty_adapter_text.setVisibility(View.VISIBLE);
                reached_bottom_text.setVisibility(View.GONE);
            }
            else empty_adapter_text.setVisibility(View.GONE);
        }
    }

    public void loadedAllPosts(){
        loaded_all_posts = true;
        if(getItemCount() > 0) reached_bottom_text.setVisibility(View.VISIBLE);
    }

    private void stopLoading(){
        if(progress_bar.getVisibility() == View.VISIBLE) progress_bar.setVisibility(View.GONE);
        if(recycler.getVisibility() == View.INVISIBLE) recycler.setVisibility(View.VISIBLE);
    }















    //MARK: ViewHolder

//------------------------------------------------------------------------------------------------------------------------------------
    public static class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        FeedClickListener feed_click_listener;
        public CardView layout;
        public ImageView feed_image_view;
        public TextView feed_text;
        public CircleImageView author_preview_image;
        public TextView feed_pinned_name;
        public ImageView pin_icon;
        public TextView time_text;


        public FeedViewHolder(@NonNull View itemView, FeedClickListener feed_click_listener) {
            super(itemView);
            feed_image_view = itemView.findViewById(R.id.item_feed_image);
            feed_text = itemView.findViewById(R.id.item_feed_text);
            author_preview_image = itemView.findViewById(R.id.item_feed_author_preview_image);
            feed_pinned_name = itemView.findViewById(R.id.item_feed_pinned_text);
            pin_icon = itemView.findViewById(R.id.item_feed_pin_icon);
            time_text = itemView.findViewById(R.id.item_feed_time);

            this.feed_click_listener = feed_click_listener;
            feed_pinned_name.setOnClickListener(this);
            pin_icon.setOnClickListener(this);
            feed_image_view.setOnClickListener(this);
            feed_text.setOnClickListener(this);
            author_preview_image.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            feed_click_listener.onFeedClick(getAdapterPosition(), v.getId());
        }
    }
}
