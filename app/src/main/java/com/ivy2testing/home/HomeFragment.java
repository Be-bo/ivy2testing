package com.ivy2testing.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.entities.Student;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.util.Constant;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    //Constants
    private static final String TAG = "HomeFragment";

    // Parent activity
    private Context mContext;
    private View rootView;

    // Testing buttons
    private Button mainLoginButton;
    private Button mainTestButton;

    private Button uni_button;
    private Button current_button;

    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();

    private Student student;
    private boolean is_organization = false;
    private UserViewModel this_user_viewmodel;


    // Constructor
    public HomeFragment(Context con) {
        mContext = con;
    }




    // MARK: Get User Data This Way - always stays update and doesn't require passing anything because ViewModel is connected to the Activity that manages the fragment
    private void getUserProfile(View rootView){
        if (getActivity() != null) {
            this_user_viewmodel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            student = this_user_viewmodel.getThisStudent().getValue(); //grab the initial data
            // TODO: only start doing processes that depend on user profile here:
            if(student != null){
                // TODO: populate UI
                // TODO: set up listeners
                // TODO: etc.
                // NOTE: everything depends on the user profile data, only execute stuff dependent on it once you 100% have it
            }
            this_user_viewmodel.getThisStudent().observe(getActivity(), (Student updatedProfile) -> { //listen to realtime user profile changes afterwards
                if (updatedProfile != null) student = updatedProfile;
                // TODO: if stuff needs to be updated whenever the user profile receives an update, DO SO HERE
            });
        }
    }
    // MARK: ------------------------------------------------------------------------------------------------------------------------------------------------------------




    /* Overridden Methods
     ***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialization
        uni_button = rootView.findViewById(R.id.btn_1);
        mainLoginButton = rootView.findViewById(R.id.main_loginButton);
        mainTestButton = rootView.findViewById(R.id.main_testButton);

        mainLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainLogin();
            }
        });
        mainTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainTest();
            }
        });

        chooseDisplay(); // Login?

        buildMainFeed();
        current_button = uni_button;
        current_button.setEnabled(false);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Came back from Login activity (Change to a switch statement if more request codes)
        if (requestCode == Constant.LOGIN_REQUEST_CODE) {
            Log.d(TAG, "Coming back from LoginActivity!");
            if (resultCode == Activity.RESULT_OK && data != null) {
                Map<Object, Object> map = new HashMap<>();
                map.put("this_user_id", data.getStringExtra("this_user_id"));
                map.put("this_uni_domain", data.getStringExtra("this_uni_domain"));
            }
        } else
            Log.w(TAG, "Don't know how to handle the request code, \"" + requestCode + "\" yet!");
    }

    /* OnClick Methods
     ***************************************************************************************************/

    // Go to login screen
    public void mainLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        Log.d(TAG, "Launching LoginActivity");

        // onActivityResult in MainActivity gets called!
        if (getActivity() != null)
            startActivityForResult(intent, Constant.LOGIN_REQUEST_CODE);
        else
            Log.e(TAG, "getActivity() was null when calling LoginActivity.");
    }

    // TEST For testing purposes only!
    public void mainTest() {
        Map<Object, Object> map = new HashMap<>();
        map.put("this_user_id", "testID");
        map.put("this_uni_domain", "ucalgary.ca");
    }

    /* Transition Methods
     ***************************************************************************************************/

    // See if logged in (check if parent Activity gave us an actual student)
    private void chooseDisplay() {

        if (student == null) {
            Log.w(TAG, "Not signed in yet!");
            setLoggedOutDisplay();
        } else setLoggedInDisplay();
    }

    // Enable bottom Navigation for a logged-in user
    private void setLoggedInDisplay() {
        mainLoginButton.setVisibility(View.GONE);
        mainTestButton.setVisibility(View.GONE);
    }

    // Disable bottom Navigation for a logged-in user
    private void setLoggedOutDisplay() {
        mainLoginButton.setVisibility(View.VISIBLE);
        mainTestButton.setVisibility(View.VISIBLE);
    }

    /* ************************************************************************************************** */
    // resets main "Uni" button to be selected/ deselects others,
    // this method is can be called when returning from other activities or back presses

    private void resetMainBubble() {
        current_button.setEnabled(true);
        uni_button.setEnabled(false);
        current_button = uni_button;
    }

    /* ************************************************************************************************** */
    // the buttons are set to enabled to be clickable and disabled  when selected so they can't be constantly clicked
    // they can be set to selected and unselected but it depends on what function they perform when clicked
    // I was struggling to get the buttons to stay clicked so i threw together this enabled solution, and it works,
    // but we can change the states... for now this works

    // all bubbles are also set with an onClick function so they call this function, and locally, whichever button
    // triggers the function, is saved locally and the view is updated. current_button = most recently clicked button, the last button will be re-enabled
    // functions can be placed here to perform a function based off the text/ information in a button i.e a search with current_button.getText

    public void toggleEnabled(View view) {
        current_button.setEnabled(true);
        view.setEnabled(false);
        current_button = (Button) view;
        //  Toast.makeText(this, "" + current_button.getText(), Toast.LENGTH_SHORT).show();

    }

    /* ************************************************************************************************** */
    // this function navigates to the collection containing posts, checks if an item is a post. It then converts the
    // item to a hash map and creates a post in the fragment container view

    private void buildMainFeed() {
        //DocumentReference sampler = db_reference.collection("universities").document("ucalgary.ca").collection("posts").document("5c2fbbf4-c06b-406c-92e9-71321f046d43");
        db_reference.collection("universities").document("ucalgary.ca").collection("posts")
                .whereEqualTo("is_event", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Toast.makeText(MainActivity.this, document.getId() + " => " + document.getData(), Toast.LENGTH_SHORT).show();
                                Map<String, Object> is_post = document.getData();
                                postCreator(is_post);
                            }
                        } else {
                            //  Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /* ************************************************************************************************** */
    //This function takes a hash map and fills a sample post with its information
    //https://stackoverflow.com/questions/6216547/android-dynamically-add-views-into-view

    private void postCreator(Map<String, Object> post) {
        // layout inflater puts views inside other views
        LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.sample_post, null);

        // fill in any details dynamically here
        final ImageView sample_image = v.findViewById(R.id.sample_image);
        //TODO do posts have titles?
        TextView tv = v.findViewById(R.id.sample_name);
        TextView description = v.findViewById(R.id.sample_description);
        TextView author_name = v.findViewById(R.id.sample_author_nam);
        description.setText((String) post.get("text"));
        author_name.setText((String) post.get("author_name"));
        String visual_path = (String) post.get("visual");
        // if the visual path contains a / it will be a link to storage location
        if (visual_path.contains("/")) {
            // max download size = 1.5mb
            db_storage.child(visual_path).getBytes(1500000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                    // bytes is an byte [] returned from storage,
                    // set the image to be visible
                    sample_image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    sample_image.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO error handler
                }
            });
        }


        // insert into main view
        // currently inserts newest post last... needs better queries
        ViewGroup insertPoint = rootView.findViewById(R.id.main_fragment_linear_layout);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}