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
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.ImageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Shanna Hollingworth
 * Overview: an adapter that takes in a list of student ids and constructs constructs a set of cards for non blacklisted students in the user's university
 * Used in: QuadFragment
 */
public class QuadAdapter extends RecyclerView.Adapter<QuadAdapter.QuadViewHolder> {

    // Shanna: Base

    private static final int NEW_BATCH_TOLERANCE = 4;
    private QuadAdapter.QuadClickListener quad_click_listener;
    private static final String TAG = "QuadAdapterTag";


    private String uni_domain;
    private User current_user;
    protected List<String> blacklist;
    private ArrayList<Student> students = new ArrayList<>();
    private long creation_millis;
    private RecyclerView recycler;
    private ProgressBar progress_bar;

    private Query query;
    private FirebaseFirestore db_ref = FirebaseFirestore.getInstance();
    private StorageReference stor_ref = FirebaseStorage.getInstance().getReference();
    private int pull_limit;
    DocumentSnapshot last_retrieved_student;
    private QuadClickListener quad_listener;
    private TextView empty_adapter_text;
    private boolean loaded_all_students = false;
    private boolean load_in_progress = false;

    private Context context;

    public QuadAdapter(QuadAdapter.QuadClickListener quad_click_listener, int limit, String uniDomain, Context con, TextView emptyAdapterText, User currentUser, RecyclerView rec, ProgressBar progressBar) {
        this.quad_click_listener = quad_click_listener;
        this.recycler = rec;
        this.pull_limit = limit;
        this.progress_bar = progressBar;
        this.current_user = currentUser;
        this.uni_domain = uniDomain;
        this.context = con;
        this.empty_adapter_text = emptyAdapterText;
        this.creation_millis = System.currentTimeMillis();
        blacklist = current_user.getBlacklist();
        blacklist.add(current_user.getId());
        query = db_ref.collection("users").whereEqualTo("uni_domain", uni_domain).whereEqualTo("is_club", false)
                .whereEqualTo("is_organization", false).whereNotIn("id", blacklist);
        Log.d("Current User:", currentUser.getId());
        fetchStudents();
    }

    private void checkEmptyAdapter(){
        if(empty_adapter_text != null){
            if(getItemCount() < 1){
                empty_adapter_text.setVisibility(View.VISIBLE);
                Log.d("Quad Adapter", "Empty Adapter");
            }
            else empty_adapter_text.setVisibility(View.GONE);
        }
    }


    public interface QuadClickListener {
        void onQuadClick(int position, int clicked_id);
    }

    // Shanna: Static Pulling Methods (loading old students) - all the students that were created before this adapter was created

    private void fetchStudents() { //fetch all students
        load_in_progress = true;
        query.get().addOnCompleteListener(querySnap -> {
            if (querySnap.isSuccessful() && querySnap.getResult() != null) {
                Log.d("Quad Adapter", "Query successful");
                if(!querySnap.getResult().isEmpty()) {
                    for (int i = 0; i < querySnap.getResult().getDocuments().size(); i++) {
                        Log.d("Quad Adapter", "added student");
                        DocumentSnapshot newStudent = querySnap.getResult().getDocuments().get(i);
                        Student student = newStudent.toObject(Student.class);
                        students.add(student);

                        if (i >= querySnap.getResult().getDocuments().size() - 1) last_retrieved_student = newStudent;
                    }

                    if (students.size() < 1) { //if the size is still 0 we need to check for the next batch (because if size 0 onBindViewHolder won't get called)
                        if (last_retrieved_student != null && !loaded_all_students) {
                            query = query.startAfter(last_retrieved_student);
                            fetchStudents(); //next batch has to be loaded from where the previous one left off
                        }
                    }
                    Collections.shuffle(students); //randomize user list
                    notifyDataSetChanged();
                } else {
                    loaded_all_students = true;
                }
            }
            stopLoading();
            checkEmptyAdapter();
            load_in_progress = false;
        });
    }

    public void refreshAdapter() { //this gets triggered when the user comes back to the quad, we check if new users have been added in the meantime or if new users have been blocked and add/remove them (have to be added to the beginning of the list, completely independent of how we're loading the rest)
        //Has a weird bug where when this is called the other profile pics change what the heckkkk
        load_in_progress = true;
        blacklist = current_user.getBlacklist();
        blacklist.add(current_user.getId());
        Log.d(TAG,"refresh called");
        db_ref.collection("users").whereEqualTo("uni_domain", uni_domain).whereEqualTo("is_club", false)
                .whereEqualTo("is_organization", false).whereNotIn("id", blacklist).get().addOnCompleteListener(querySnapTask -> {
            if(querySnapTask.isSuccessful() && querySnapTask.getResult() != null && !querySnapTask.getResult().isEmpty()){
                for(DocumentSnapshot docSnap: querySnapTask.getResult()){
                    Student student = docSnap.toObject(Student.class);
                    if (student != null && !studentAlreadyAdded(student.getId())) students.add(0, student);
                }
                for(Student currStud : students) //Check if new users have been added to blacklist and remove them from students
                {
                    if(blacklist.contains(currStud.getId())) {
                        students.remove(currStud);
                    }
                }
                Collections.shuffle(students); //randomize user list
                notifyDataSetChanged();
                load_in_progress = false;
            }
            checkEmptyAdapter();
        });
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
        Student current = students.get(position);

        holder.name_text.setText(current.getName());
        holder.degree_text.setText(current.getDegree());
        loadImage(holder, students.get(position));

        if (!load_in_progress && position >= (students.size() - NEW_BATCH_TOLERANCE)) { //new batch tolerance means within how many last items do we want to start loading the next batch (i.e. we have 20 items and tolerance 2 -> the next batch will start loading once the user scrolls to the position 18 or 19)
            if (last_retrieved_student != null && !loaded_all_students) {
                query = query.startAfter(last_retrieved_student);
                fetchStudents(); //next batch has to be loaded from where the previous one left off
            }
        }
    }

    @Override
    public int getItemCount() {
        return students.size();
    }






    // Shanna: Other Methods

    private void loadImage(@NonNull QuadViewHolder holder, Student currentStudent) { // Load profile picture from storage
        if (currentStudent == null) return;

        stor_ref.child(ImageUtils.getUserImagePath(currentStudent.getId())).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                       Glide.with(context).load(task.getResult()).into(holder.student_profile_picture);
                });
    }

    private boolean studentAlreadyAdded(String id) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equals(id)) return true;
        }
        return false;
    }

    private void stopLoading(){
        if(progress_bar.getVisibility() == View.VISIBLE) progress_bar.setVisibility(View.GONE);
        if(recycler.getVisibility() == View.INVISIBLE) recycler.setVisibility(View.VISIBLE);
    }


//Shanna: ViewHolder

//------------------------------------------------------------------------------------------------------------------------------------
    public static class QuadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        QuadAdapter.QuadClickListener quad_click_listener;
        public CardView layout;
        public ImageView chatButton;
        public ImageView student_profile_picture;
        public TextView name_text;
        public TextView degree_text;


        public QuadViewHolder(@NonNull View itemView, QuadAdapter.QuadClickListener quad_click_listener) {
            super(itemView);
            student_profile_picture = itemView.findViewById(R.id.quad_studentProfilePic);
            name_text = itemView.findViewById(R.id.quad_studentName);
            degree_text = itemView.findViewById(R.id.quad_studentDegree);
            student_profile_picture = itemView.findViewById(R.id.quad_studentProfilePic);
            chatButton = itemView.findViewById(R.id.chatButton);

            this.quad_click_listener = quad_click_listener;
            chatButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            quad_click_listener.onQuadClick(getAdapterPosition(), v.getId());
        }
    }
}