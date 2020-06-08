package com.ivy2testing;

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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private Toolbar main_toolbar;

    private Button post_button;

    private Button uni_button;
    private Button current_button;

    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();


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

    /* ************************************************************************************************** */
    // grabbing views and setting current_button to handle clicks on bubbles
    private void setHandlers(){
        post_button = findViewById(R.id.post_button);
        uni_button = findViewById(R.id.btn_1);
        current_button = uni_button;
    }

    /* ************************************************************************************************** */
    // setting listeners on the post icon, and disables current button
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
        fragmentTransaction.add(R.id.main_fragment_container, mf).addToBackStack(null).commit();

        closeFragment();

    }

    // Close format will close the previous fragment
    private void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Check to see if a fragment is already showing.
        MainFragment mf = (MainFragment) fragmentManager.findFragmentById(R.id.main_fragment_container);
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
