package com.ivy2testing.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.home.CreatePost;
import com.ivy2testing.R;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.entities.Student;
import com.ivy2testing.home.HomeFragment;
import com.ivy2testing.userProfile.StudentProfileFragment;
import com.ivy2testing.util.FragCommunicator;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements FragCommunicator {

    // Constants
    private final static String TAG = "MainActivity";

    // Hamburger menu
    private DrawerLayout drawer;
    private Toolbar main_toolbar;

    // Testing buttons
    private Button mainLoginButton;
    private Button mainTestButton;


    private BottomNavigationView mainTabBar;
    private FrameLayout mFrameLayout;
    private FrameLayout loadingLayout;


    private Button post_button;

    private Button uni_button;
    private Button current_button;

    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private String this_uni_domain;
    private String this_user_id;
    private boolean is_organization = false;
    private Student mStudent;       //TODO need abstract class User?
    private Uri profileImgUri;


    // On create
    /* ************************************************************************************************** */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // toolbar is an <include> in xml, referencing the file main_toolbar.xml
        main_toolbar = findViewById(R.id.main_toolbar_id);

        // setting support action bar builds the toolbar (required for a hamburger menu)
        setSupportActionBar(main_toolbar);

        // activity_main is a drawer layout/ the drawer drawn off screen
        drawer = findViewById(R.id.main_layout_drawer);

        // toggle is the hamburger menu itself
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, main_toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // this is required to nullify title, title is set to empty character in xml too but still writes
        getSupportActionBar().setTitle(null);

        // set color of hamburger menu
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.interaction));

        setHandlers();
        setListeners();
        chooseDisplay();


        // this method is kept so people can swap fragments when need be
        // fragmentHandler();

        buildMainFeed();
    }

    /* ************************************************************************************************** */

    // these methods close the drawer before calling another back press
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            //resetMainBubble();
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof StudentProfileFragment) {
            StudentProfileFragment profileFragment = (StudentProfileFragment) fragment;
            profileFragment.setCommunicator(this);
        }
    }

    // Enable bottom Navigation for a logged-in user
    private void setLoggedInDisplay(){
        mainLoginButton.setVisibility(View.GONE);
        mainTestButton.setVisibility(View.GONE);
        mainTabBar.setVisibility(View.VISIBLE);
     //  mFrameLayout.setVisibility(View.VISIBLE);
        mainTabBar.setSelectedItemId(R.id.tab_bar_home);
        endLoading();
    }

    // Disable bottom Navigation for a logged-in user
    private void setLoggedOutDisplay(){
        mainLoginButton.setVisibility(View.VISIBLE);
        mainTestButton.setVisibility(View.VISIBLE);
        mainTabBar.setVisibility(View.GONE);
//        mFrameLayout.setVisibility(View.GONE);
        endLoading();
    }

    private void setNavigationListener() {
        // Test bottom navigation
        mainTabBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()){
                    case R.id.tab_bar_chat:
                        selectedFragment = new ChatFragment();
                        break;
                    case R.id.tab_bar_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.tab_bar_profile:
                        if (is_organization) Log.w(TAG, "Organization Profile View under construction.");
                        else selectedFragment = new StudentProfileFragment(MainActivity.this, mStudent, profileImgUri);
                        break;
                }
                if (selectedFragment!= null)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragmentContainer, selectedFragment).commit();

                return true;
            }
        });
    }

    /* ************************************************************************************************** */
    // grabbing views and setting current_button to handle clicks on bubbles
    private void setHandlers(){
        mainTabBar = findViewById(R.id.main_tab_bar);
        loadingLayout = findViewById(R.id.main_loadingScreen);
        post_button = findViewById(R.id.post_button);
        uni_button = findViewById(R.id.btn_1);
        mainLoginButton = findViewById(R.id.main_loginButton);
        mainTestButton = findViewById(R.id.main_testButton);

        current_button = uni_button;
    }

    /* ************************************************************************************************** */
    // setting listeners on the post icon, and disables current button

    // Go to login screen
    public void mainLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // TEST For testing purposes only!
    public void mainTest(View view) {
        Intent intent = new Intent(this, com.ivy2testing.main.MainActivity.class);
        intent.putExtra("this_uni_domain","ucalgary.ca");
        intent.putExtra("this_user_id", "testID");
        intent.putExtra("isStudent", true);
        finish();
        startActivity(intent);
    }

    private void setListeners(){
        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreatePost.class);
                startActivity(intent);
            }
        });
        current_button.setEnabled(false);
    }
    /* Transition Methods
     ***************************************************************************************************/

    // Get intent extras and see if logged in
    private void chooseDisplay(){

        startLoading();     // Loading Animation overlay

        // Get intent extras
        if (getIntent() != null) {
            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            this_user_id = getIntent().getStringExtra("this_user_id");
        }

        if (this_uni_domain == null || this_user_id == null){
            Log.w(TAG,"Not signed in yet!");
            setLoggedOutDisplay();
        }
        else getUserInfo();
    }
    // Set loading page animation
    private void startLoading(){
        loadingLayout.setVisibility(View.VISIBLE);      // Bring up view to cover entire screen
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_loadingScreen, new LoadingPageFragment(this)).commit();      // Populate View with loading page layout
    }

    // Fade out loading page animation
    private void endLoading(){
        loadingLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));  // Start fade out animation
        loadingLayout.setVisibility(View.GONE);
    }


    /* ************************************************************************************************** */
    // resets main "Uni" button to be selected/ deselects others,
    // this method is can be called when returning from other activities or back presses

    private void resetMainBubble(){
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

    public void toggleEnabled(View view){
        current_button.setEnabled(true);
        view.setEnabled(false);
        current_button = (Button) view;
      //  Toast.makeText(this, "" + current_button.getText(), Toast.LENGTH_SHORT).show();

    }

    /* Firebase Methods
     ***************************************************************************************************/

    // Get all student info and return student class
    public void getUserInfo(){
        String address = "universities/" + this_uni_domain + "/users/" + this_user_id;
        if (address.contains("null")){
            Log.e(TAG, "User Address has null values.");
            return;
        }

        db_reference.document(address).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc == null){
                        Log.e(TAG, "Document doesn't exist");
                        return;
                    }

                    // what TODO if field doesn't exist?
                    if (doc.get("is_organization") == null){
                        Log.e(TAG, "getUserInfo: 'is_organization' field doesn't exist");
                        mStudent = doc.toObject(Student.class);
                        if (mStudent == null) Log.e(TAG, "Student object obtained from database is null!");
                        else mStudent.setId(this_user_id);
                        // Continue to rest of App
                        setNavigationListener();
                        setLoggedInDisplay();
                        return;
                    }

                    // Student or Organization?
                    is_organization = (boolean) doc.get("is_organization");
                    if (is_organization){
                        //TODO is organization
                        Log.d(TAG, "User is an organization!");
                        // Continue to rest of App
                        setNavigationListener();
                        setLoggedInDisplay();
                    }
                    else {
                        mStudent = doc.toObject(Student.class);
                        if (mStudent == null) Log.e(TAG, "Student object obtained from database is null!");
                        else mStudent.setId(this_user_id);
                        getStudentPic();
                    }
                }
                else Log.e(TAG,"getUserInfo: unsuccessful!");
            }
        });
    }

    // load picture from firebase storage
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void getStudentPic() {

        // Make sure student has a profile image already
        if (mStudent.getProfile_picture() != null){
            db_storage.child(mStudent.getProfile_picture()).getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                profileImgUri = task.getResult();
                            }
                            else {
                                Log.w(TAG, task.getException());
                                mStudent.setProfile_picture(""); // image doesn't exist
                            }
                            // Continue to rest of App
                            setNavigationListener();
                            setLoggedInDisplay();
                        }
                    });
        }
        else {
            // Continue to rest of App
            setNavigationListener();
            setLoggedInDisplay();
        }
    }

    /* Interface Methods
     ***************************************************************************************************/

    // Frag Communicator
    @Override
    public Object message(Object obj) {
        if (obj instanceof Student) {
            mStudent = (Student) obj;   // Update student user
            Log.d(TAG, "Student " + mStudent.getName() + " got updated!");
        }
        return null;
    }







    /* ************************************************************************************************** */
    // this function navigates to the collection containing posts, checks if an item is a post. It then converts the
    // item to a hash map and creates a post in the fragment container view

    private void buildMainFeed(){
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

    private void postCreator( Map<String, Object> post ){
        // layout inflater puts views inside other views
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.sample_post,null);

        // fill in any details dynamically here
        final ImageView sample_image =  v.findViewById(R.id.sample_image);
        //TODO do posts have titles?
        TextView tv =  v.findViewById(R.id.sample_name);
        TextView description =  v.findViewById(R.id.sample_description);
        TextView author_name =  v.findViewById(R.id.sample_author_nam);
        description.setText((String) post.get("text"));
        author_name.setText( (String) post.get("author_name") );
        String visual_path =(String) post.get("visual");
        // if the visual path contains a / it will be a link to storage location
        if (visual_path.contains("/") ){
            // max download size = 1.5mb
            db_storage.child(visual_path).getBytes(1500000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                    // bytes is an byte [] returned from storage,
                    // set the image to be visible
                    sample_image.setImageBitmap(BitmapFactory.decodeByteArray(bytes , 0, bytes.length));
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
        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.main_fragment_linear_layout);
        insertPoint.addView(v,0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    /* ************************************************************************************************** */
    // this handler will allow another fragment to be placed in the fragment container
    // the container should be set to a frame layout to allow fragments on top of eachother
    // it is set to a scrollview atm, but we don't call fragments yet

    private void fragmentHandler() {
        MainFragment mf = MainFragment.newInstance();
        //Get the Fragment Manager and start a transaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // add the fragment
        // this code here allows info from other fragments to be added to the back stack
        // pressing back will reload previous fragments, I don't think we need this for ivy, + how far back should they stack?
        fragmentTransaction.add(R.id.main_fragmentContainer, mf).addToBackStack(null).commit();

        closeFragment();

    }

    // Close format will close the previous fragment
    private void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Check to see if a fragment is already showing.
        MainFragment mf = (MainFragment) fragmentManager.findFragmentById(R.id.main_fragmentContainer);
        if (mf != null) {
            //create and commit the transaction to remove the fragment
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // i believe replace allows the previous fragment to be saved in memory, while swapping views..
            //fragmentTransaction.replace(R.id.fragment_container, sf);

            //close
            fragmentTransaction.remove(mf).commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }
}
