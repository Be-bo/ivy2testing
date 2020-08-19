package com.ivy2testing.util.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zahra Ghavasieh, Robert's iPad
 * Overview: an adapter that takes in a list of post ids and constructs square images per post
 * Used in: StudentProfile.Posts
 */
public class SquarePostAdapter extends RecyclerView.Adapter<SquareImgHolder> {







    // MARK: Base

    private static final int NEW_BATCH_TOLERANCE = 4;
    private static final String TAG = "SquareImageAdapterTag";
    private String author_id;
    private String uni_domain;
    private int pull_limit = 0;
    private ArrayList<Post> posts = new ArrayList<>();
    private List<View> all_layout_elements;
    private long creation_millis;
    private TextView empty_adapter_text;
    private RecyclerView recycler;
    private ProgressBar progress_bar;

    private Query query;
    private FirebaseFirestore db_ref = FirebaseFirestore.getInstance();
    private StorageReference stor_ref = FirebaseStorage.getInstance().getReference();
    private OnPostListener post_listener;
    private boolean loaded_all_posts = false;
    private boolean load_in_progress = false;
    private DocumentSnapshot last_retrieved_document;

    private Context context;
    private ListenerRegistration list_reg;

    public SquarePostAdapter(String id, String uniDomain, int limit, Context mrContext, OnPostListener listener, List<View> allElems, TextView emptyAdapterText, RecyclerView rec, ProgressBar progressBar) {
        this.recycler = rec;
        this.progress_bar = progressBar;
        this.uni_domain = uniDomain;
        this.author_id = id;
        this.context = mrContext;
        this.post_listener = listener;
        this.pull_limit = limit;
        this.all_layout_elements = allElems;
        this.empty_adapter_text = emptyAdapterText;
        this.creation_millis = System.currentTimeMillis();

        query = db_ref.collection("universities/" + uni_domain + "/posts").whereEqualTo("author_id", author_id).orderBy("creation_millis", Query.Direction.DESCENDING).limit(limit);
        fetchPostBatch();
    }

    public interface OnPostListener { // Listener Interface
        void onPostClick(int position);
    }

    public Post getItem(int position) {
        return posts.get(position);
    }

    private void checkEmptyAdapter() {
        if (empty_adapter_text != null) {
            if (getItemCount() < 1) {
                hideLayout();
                empty_adapter_text.setVisibility(View.VISIBLE);
            } else {
                empty_adapter_text.setVisibility(View.GONE);
                showLayout();
            }
        }
    }











    // MARK: Static Pulling Methods (loading old posts) - all the posts that were created before this adapter was created

    private void fetchPostBatch() { //fetch all posts (events and standard posts) combined based on the input query
        load_in_progress = true;
        query.get().addOnCompleteListener(querySnap -> {
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {

                if(!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        DocumentSnapshot newPost = querySnap.getResult().getDocuments().get(i);

                        if (newPost.get("is_event") instanceof Boolean && (Boolean) newPost.get("is_event")) {
                            Event event = newPost.toObject(Event.class);
                            if (event != null && !postAlreadyAdded(event.getId())) posts.add(event);
                        } else {
                            Post post = newPost.toObject(Post.class);
                            if (post != null && !postAlreadyAdded(post.getId())) posts.add(post);
                        }

                        if (i >= querySnap.getResult().getDocuments().size() - 1) last_retrieved_document = newPost;
                    }

                    if (posts.size() < 1) { //if the size is still 0 we need to check for the next batch (because if size 0 onBindViewHolder won't get called)
                        if (last_retrieved_document != null && !loaded_all_posts) {
                            query = query.startAfter(last_retrieved_document);
                            fetchPostBatch(); //next batch has to be loaded from where the previous one left off
                        }
                    }
                    notifyDataSetChanged();
                }else{
                    loaded_all_posts = true;
                }
            }
            stopLoading();
            checkEmptyAdapter();
            load_in_progress = false;
        });
    }

    public void refreshAdapter() { //this gets triggered when the user comes back to the profile, we check if new posts have been added in the meantime and add them (have to be added to the beginning of the list, completely independent of how we're loading the rest)
        load_in_progress = true;
        db_ref.collection("universities/" + uni_domain + "/posts").whereEqualTo("author_id", author_id).whereGreaterThan("creation_millis", creation_millis).orderBy("creation_millis", Query.Direction.ASCENDING).get().addOnCompleteListener(querySnapTask -> {
            if(querySnapTask.isSuccessful() && querySnapTask.getResult() != null && !querySnapTask.getResult().isEmpty()){
                for(DocumentSnapshot docSnap: querySnapTask.getResult()){
                    if (docSnap.get("is_event") instanceof Boolean && (Boolean) docSnap.get("is_event")) {
                        Event event = docSnap.toObject(Event.class);
                        if (event != null && !postAlreadyAdded(event.getId())) posts.add(0, event);
                    } else {
                        Post post = docSnap.toObject(Post.class);
                        if (post != null && !postAlreadyAdded(post.getId())) posts.add(0, post);
                    }
                }
                notifyDataSetChanged();
                load_in_progress = false;
            }
            checkEmptyAdapter();
        });
    }













    // MARK: Override Methods

    @NonNull
    @Override
    public SquareImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_grid, parent, false);
        return new SquareImgHolder(view, post_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SquareImgHolder holder, final int position) {
        Post current = posts.get(position);

        if (posts.get(position) instanceof Event) { // Banner
            holder.banner.setVisibility(View.VISIBLE);
            holder.banner_text.setVisibility(View.VISIBLE);
        } else {
            holder.banner.setVisibility(View.GONE);
            holder.banner_text.setVisibility(View.GONE);
        }

        if (current.getVisual() == null || current.getVisual().equals("nothing")) {
            holder.image_view.setVisibility(View.GONE);
            holder.info_text.setVisibility(View.VISIBLE);
            if (current.getIs_event()) holder.info_text.setText(((Event) current).getName());
            else holder.info_text.setText(current.getText());
        } else {
            holder.info_text.setVisibility(View.GONE);
            holder.image_view.setVisibility(View.VISIBLE);
            loadImage(holder, posts.get(position));
        }

        if (!load_in_progress && position >= (posts.size() - NEW_BATCH_TOLERANCE)) { //new batch tolerance means within how many last items do we want to start loading the next batch (i.e. we have 20 items and tolerance 2 -> the next batch will start loading once the user scrolls to the position 18 or 19)
            if (last_retrieved_document != null && !loaded_all_posts) {
                query = query.startAfter(last_retrieved_document);
                fetchPostBatch(); //next batch has to be loaded from where the previous one left off
            }
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }













    // MARK: Other Methods

    private void loadImage(@NonNull SquareImgHolder holder, Post currentPost) { // Load visual from storage
        if (currentPost.getVisual() != null && !currentPost.getVisual().equals("nothing") && !currentPost.getVisual().equals(""))
            stor_ref.child(currentPost.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null)
                    Glide.with(context).load(task.getResult()).into(holder.image_view);
            });
    }

    private void hideLayout() {
        for (View view : all_layout_elements) {
            if (view != null) view.setVisibility(View.GONE);
        }
    }

    private void showLayout() {
        for (View view : all_layout_elements) {
            if (view != null) view.setVisibility(View.VISIBLE);
        }
    }

    private boolean postAlreadyAdded(String id) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(id)) return true;
        }
        return false;
    }

    private void stopLoading(){
        if(progress_bar.getVisibility() == View.VISIBLE) progress_bar.setVisibility(View.GONE);
        if(recycler.getVisibility() == View.INVISIBLE) recycler.setVisibility(View.VISIBLE);
    }
}
