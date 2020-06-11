package com.ivy2testing.userProfile;

import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.util.OnSelectionListener;
import com.ivy2testing.entities.Student;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: 3rd party Student Profile view Activity.
 *          Takes in a user "address" in Firestore as intent extras.
 *          Otherwise similar to StudentProfileFragment.
 * Notes: Recycler items currently hard-coded, Needs Update from fragment version
 */
public class StudentProfileActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "StudentProfileActivity";

    // Views
    private ImageView mProfileImg;
    private TextView mName;
    private TextView mDegree;
    private RecyclerView mRecyclerView;

    // FireBase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private String this_uni_domain;
    private String this_user_id;
    private Student student;
    private ImageAdapter adapter;


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_studentprofile);

        // Initialization Methods
        getIntentExtras();
        declareViews();
        getStudentInfo();
    }


/* Initialization Methods
***************************************************************************************************/

    private void declareViews(){
        mProfileImg = findViewById(R.id.studentProfile_circleImg);
        mName = findViewById(R.id.studentProfile_name);
        mDegree = findViewById(R.id.studentProfile_degree);
        mRecyclerView = findViewById(R.id.studentProfile_posts);

        // Action bar
        setActionBar((Toolbar) findViewById(R.id.editStudent_toolBar));
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        else Log.e(TAG, "no actionbar");
    }

    private void setupViews(){
        loadImage();
        mName.setText(student.getName());
        mDegree.setText(student.getDegree());
    }

    // TODO set up with Post objects later
    private void setUpRecycler(){

        // Get list of image ids
        List<Uri> imageIds = new ArrayList<>();

        // set LayoutManager and Adapter
        //adapter = new ImageAdapter(imageIds);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(adapter);
    }

    // Set up onClick Listeners
    private void setListeners(){
        findViewById(R.id.studentProfile_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();}
        });
        findViewById(R.id.studentProfile_seeAll).setOnClickListener(new View.OnClickListener() {
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

    // Add Friend TODO
    private void addFriend(){}

    // See all posts TODO
    private void seeAllPosts(){}

    // A post in recycler was selected  TODO
    private void selectPost() {
        Toast.makeText(this,"I was clicked here! ", Toast.LENGTH_SHORT).show();
    }

/* Firebase Related Methods
***************************************************************************************************/

    private void getStudentInfo(){
        db.collection("universities").document(this_uni_domain).collection("users").document(this_user_id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                    else student.setId(this_user_id);

                    // Set fields with current values and initiate listeners
                    setupViews();
                    setUpRecycler();
                    setListeners();
                }
                else Log.e(TAG,"getStudentInfo: unsuccessful!");
            }
        });
    }

    // Load student profile picture
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void loadImage(){
        // Make sure student has a profile image already
        if (student.getProfile_picture() != null){
            base_storage_ref.child(student.getProfile_picture()).getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri path = task.getResult();
                                Picasso.get().load(path).into(mProfileImg);
                            }
                            else {
                                Log.w(TAG, task.getException());
                                student.setProfile_picture(""); // image doesn't exist
                            }
                        }
                    });
        }
    }

/* Transition Methods
***************************************************************************************************/

    // Get student address in firebase
    private void getIntentExtras(){
        if (getIntent() != null){

            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            this_user_id = getIntent().getStringExtra("this_user_id");

            if (this_uni_domain == null || this_user_id == null){
                Log.e(TAG,"Student Address is null!");
                finish(); // Not tested yet TODO
            }
        }
    }
}