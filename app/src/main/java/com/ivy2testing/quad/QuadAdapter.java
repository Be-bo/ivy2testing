package com.ivy2testing.quad;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.ImageUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Shanna Hollingworth
 * Overview: an adapter that takes in a list of user ids and constructs constructs a set of cards for non blacklisted users in the user's university
 * Used in: QuadFragment
 */
public class QuadAdapter extends RecyclerView.Adapter<QuadAdapter.QuadViewHolder> {

    // Shanna: Base

    private static final int BATCH_LIMIT = 10;
    private static final int NEW_BATCH_TOLERANCE = 4;
    private static final String TAG = "QuadAdapterTag";


    protected List<String> blacklist;
    private final ArrayList<User> users = new ArrayList<>();
    private final RecyclerView recycler;
    private final ProgressBar progress_bar;
    private final TextView empty_adapter_text;

    private Query default_query;
    private final StorageReference stor_ref = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore db_ref = FirebaseFirestore.getInstance();
    private DocumentSnapshot last_retrieved_user;

    private boolean loaded_all_users = false;
    private boolean load_in_progress = false;

    private final Context context;
    private final OnQuadClickListener quad_listener;





    public QuadAdapter(QuadAdapter.OnQuadClickListener quad_click_listener, Context con, TextView emptyAdapterText, User currentUser, RecyclerView rec, ProgressBar progressBar) {
        this.quad_listener = quad_click_listener;
        this.recycler = rec;
        this.progress_bar = progressBar;
        this.context = con;
        this.empty_adapter_text = emptyAdapterText;
        initBlacklist(currentUser);

        default_query = db_ref.collection("users")
                .whereNotIn("id", blacklist)
                .limit(BATCH_LIMIT);
        fetchUsers(default_query);
    }


    private void checkEmptyAdapter(){
        if(empty_adapter_text != null){
            if(getItemCount() < 1){
                empty_adapter_text.setVisibility(View.VISIBLE);
                Log.d(TAG, "Empty Adapter");
            }
            else empty_adapter_text.setVisibility(View.GONE);
        }
    }


    //Initializes the blacklist; this must be done every time the adapter in case user has been added/removed
    private void initBlacklist(User current_user) {
        blacklist = Stream.of(current_user.getBlocked_users(), current_user.getBlockers(), current_user.getMessaging_users()).flatMap(Collection::stream).collect(Collectors.toList());
        blacklist.add(current_user.getId());
    }


    public User getItem(int position){
        return users.get(position);
    }


    // Shanna: Static Pulling Methods (loading old users) - all the users that were created before this adapter was created
    //Maybe need to define user or student when added into list
    private void fetchUsers(Query query) { //fetch all users
        load_in_progress = true;
        query.get().addOnCompleteListener(querySnap -> {
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {
                if(!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {

                        DocumentSnapshot newUser = querySnap.getResult().getDocuments().get(i);

                        // Convert to org/student
                        if ((boolean)newUser.get("is_organization")) users.add(newUser.toObject(Organization.class));
                        else users.add(newUser.toObject(Student.class));

                        if (i >= querySnap.getResult().getDocuments().size() - 1) last_retrieved_user = newUser;
                    }

                    if (users.size() < 1) { //if the size is still 0 we need to check for the next batch (because if size 0 onBindViewHolder won't get called)
                        if (last_retrieved_user != null && !loaded_all_users)
                            fetchUsers(default_query.startAfter(last_retrieved_user)); //next batch has to be loaded from where the previous one left off
                    }
                    //Collections.shuffle(users); //randomize user list
                    notifyDataSetChanged();
                } else loaded_all_users = true;
            }
            stopLoading();
            checkEmptyAdapter();
            load_in_progress = false;
        });
    }

    // this gets triggered when the user comes back to the quad
    // In case blacklist was changed
    public void refreshAdapter(User usr) {
        // Reset values
        initBlacklist(usr);
        default_query = db_ref.collection("users")
                .whereNotIn("id", blacklist)
                .limit(BATCH_LIMIT);

        if (load_in_progress) return;   // no need to pull again if we're currently doing it

        users.clear();              // clear list of users
        fetchUsers(default_query);  // fetch users again
    }




    // Shanna: Override Methods


    @NonNull
    @Override
    public QuadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_card, parent, false);
        return new QuadViewHolder(view, quad_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull QuadViewHolder holder, final int position) {
        User current = users.get(position);

        holder.name_text.setText(current.getName());
        if (current instanceof Student) {
            //If user is a student turn on degree text visible and set text
            Student currStud = (Student) current;
            holder.degree_text.setVisibility((View.VISIBLE));
            holder.degree_text.setText(currStud.getDegree());
        } else {
            //else turn off degree text visibility
            holder.degree_text.setVisibility(View.INVISIBLE);
        }
        loadImage(holder, current);

        if (!load_in_progress && position >= (users.size() - NEW_BATCH_TOLERANCE)) { //new batch tolerance means within how many last items do we want to start loading the next batch (i.e. we have 20 items and tolerance 2 -> the next batch will start loading once the user scrolls to the position 18 or 19)
            if (last_retrieved_user != null && !loaded_all_users)
                fetchUsers(default_query.startAfter(last_retrieved_user)); //next batch has to be loaded from where the previous one left off
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }






    // Shanna: Other Methods

    private void loadImage(@NonNull QuadViewHolder holder, User currentUser) { // Load profile picture from storage
        if (currentUser == null) return;

        try {
            stor_ref.child(ImageUtils.getUserImagePath(currentUser.getId())).getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null)
                    Glide.with(context).load(task.getResult()).into(holder.user_profile_picture);
                else
                    Glide.with(context).load(R.drawable.empty_profile_image).into(holder.user_profile_picture);
            });
        } catch (Exception e) {
            Log.w(TAG, "StorageException! No Preview Image for this user.");
        }
    }

    private void stopLoading(){
        if(progress_bar.getVisibility() == View.VISIBLE) progress_bar.setVisibility(View.GONE);
        if(recycler.getVisibility() == View.INVISIBLE) recycler.setVisibility(View.VISIBLE);
    }


//Shanna: ViewHolder

//------------------------------------------------------------------------------------------------------------------------------------
    public static class QuadViewHolder extends RecyclerView.ViewHolder{

        public CardView layout;
        public ImageView chatButton;
        public ImageView user_profile_picture;
        public TextView name_text;
        public TextView degree_text;


        public QuadViewHolder(@NonNull View itemView, QuadAdapter.OnQuadClickListener listener) {
            super(itemView);
            user_profile_picture = itemView.findViewById(R.id.quad_studentProfilePic);
            name_text = itemView.findViewById(R.id.quad_studentName);
            degree_text = itemView.findViewById(R.id.quad_studentDegree);
            user_profile_picture = itemView.findViewById(R.id.quad_studentProfilePic);
            chatButton = itemView.findViewById(R.id.chatButton);
            layout = itemView.findViewById(R.id.cardView);

            // Chat button Click
            chatButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onChatClick(position, v);
                }
            });

            // Card Click
            layout.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onCardClick(position, v);
                } else {
                    Log.d(TAG, "null listener");
                }
            });

            name_text.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onCardClick(position, v);
                }
            });
        }
    }


/* Item Click Interface (different methods for short and long(click and hold) clicks)
***************************************************************************************************/

    public interface OnQuadClickListener {
        void onChatClick(int position, View v);
        void onCardClick(int position, View v);
    }
}