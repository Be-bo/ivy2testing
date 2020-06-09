package com.ivy2testing.userProfile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.util.FragCommunicator;
import com.ivy2testing.util.OnSelectionListener;
import com.ivy2testing.R;
import com.ivy2testing.entities.Student;
import com.ivy2testing.util.Constant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: Student Profile view fragment
 * Notes: Recycler items currently hard-coded
 */
public class StudentProfileFragment extends Fragment {

    // Constants
    private final static String TAG = "StudentProfileFragment";

    // Parent activity
    private FragCommunicator mCommunicator; // For communications to activity
    private Context mContext;

    // Views
    private ImageView mProfileImg;
    private TextView mName;
    private TextView mDegree;
    private RecyclerView mRecyclerView;

    // Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Student student;
    private ImageAdapter adapter;
    private Uri profileImgUri;


    // Constructor
    public StudentProfileFragment(Context context, Student student, Uri profileImgUri) {
        mContext = context;
        this.student = student;
        this.profileImgUri = profileImgUri;
    }

    // Setter for communicator
    public void setCommunicator(FragCommunicator communicator) {
        mCommunicator = communicator;
    }


    /* Override Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_studentprofile, container, false);

        // Initialization Methods
        declareViews(rootView);
        setupViews();
        setUpRecycler();
        setListeners(rootView);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Came back from edit student activity (Change to a switch statement if more request codes)
        if (requestCode == Constant.EDIT_STUDENT_REQUEST_CODE) {
            Log.d(TAG, "Coming back from EditStudent!");
            if (resultCode == Activity.RESULT_OK && data != null) {
                boolean updated = data.getBooleanExtra("updated", false);
                if (updated) reloadStudent();
            }
        } else
            Log.w(TAG, "Don't know how to handle the request code, \"" + requestCode + "\" yet!");
    }

/* Initialization Methods
***************************************************************************************************/

    private void declareViews(View v){
        mProfileImg = v.findViewById(R.id.studentProfile_circleImg);
        mName = v.findViewById(R.id.studentProfile_name);
        mDegree = v.findViewById(R.id.studentProfile_degree);
        mRecyclerView = v.findViewById(R.id.studentProfile_posts);
    }

    private void setupViews(){
        if (student == null) return;
        mName.setText(student.getName());
        mDegree.setText(student.getDegree());
        if (profileImgUri!= null) Picasso.get().load(profileImgUri).into(mProfileImg);
    }

    // TODO set up with Post objects later
    private void setUpRecycler(){

        // Get list of image ids
        List<Integer> imageIds = new ArrayList<>();
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);
        imageIds.add(R.drawable.test_flower);

        // set LayoutManager and Adapter
        adapter = new ImageAdapter(imageIds);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(adapter);
    }

    // Set up onClick Listeners
    private void setListeners(View v){
        v.findViewById(R.id.studentProfile_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {editProfile();}
        });
        v.findViewById(R.id.studentProfile_seeAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {seeAllPosts();}
        });
        adapter.setOnSelectionListener(new OnSelectionListener() {
            @Override
            public void onSelectionClick(int position) {selectPost();}
        });
    }

/* OnClick Methods
***************************************************************************************************/

    // Edit profile
    private void editProfile(){
        Intent intent = new Intent(getActivity(), EditStudentProfileActivity.class);
        Log.d(TAG, "Starting EditProfile Activity for student: " + student.getId());
        intent.putExtra("student", student);

        // onActivityResult in MainActivity gets called!
        if (getActivity() != null)
            startActivityForResult(intent, Constant.EDIT_STUDENT_REQUEST_CODE);
        else
            Log.e(TAG, "getActivity() was null when calling EditProfile.");
    }

    // See all posts TODO
    private void seeAllPosts(){}

    // A post in recycler was selected  TODO
    private void selectPost() {
        Toast.makeText(mContext,"I was clicked here! ", Toast.LENGTH_SHORT).show();
    }


/* Firebase Methods
***************************************************************************************************/

    // Reload student profile
    private void reloadStudent() {
        if (student == null) return;

        final String this_user_id = student.getId();
        String address = "universities/" + student.getUni_domain() + "/users/" + student.getId();
        if (address.contains("null")){
            Log.e(TAG, "Student Address has null values.");
            return;
        }

        db.document(address).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc == null){
                        Log.e(TAG, "Document doesn't exist");
                        return;
                    }
                    student = doc.toObject(Student.class);
                    if (student == null) Log.e(TAG, "Student object obtained from database is null!");
                    else {
                        student.setId(this_user_id);    // Set student ID
                        mCommunicator.message(student); // Tell MainActivity to use new student
                        getStudentPic();                // Upload pic and update views
                    }
                }
                else Log.e(TAG,"getUserInfo: unsuccessful!");
            }
        });
    }

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getStudentPic() {
        if (student == null) return;

        // Make sure student has a profile image already
        if (student.getProfile_picture() != null){
            base_storage_ref.child(student.getProfile_picture()).getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                profileImgUri = task.getResult();
                            }
                            else {
                                Log.w(TAG, task.getException());
                                student.setProfile_picture(""); // image doesn't exist
                            }

                            // Reload views
                            setupViews();
                            setUpRecycler();
                        }
                    });
        } else {
            // Reload views
            setupViews();
            setUpRecycler();
        }
    }
}
