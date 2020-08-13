package com.ivy2testing.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.BubbleTabAdapter;
import com.ivy2testing.home.CreatePost;
import com.ivy2testing.R;
import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.bubbletabs.EventsFragment;
import com.ivy2testing.bubbletabs.CampusFragment;
import com.ivy2testing.notifications.NotificationHandler;
import com.ivy2testing.terms.TermsActivity;
import com.ivy2testing.userProfile.NotificationCenterActivity;
import com.ivy2testing.userProfile.OrganizationProfileFragment;
import com.ivy2testing.userProfile.StudentProfileFragment;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.SpinnerAdapter;
import com.ivy2testing.util.Utils;

import java.util.ArrayList;

import static com.ivy2testing.util.StaticDomainList.available_domain_list;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {




    // MARK: Variables and Constants

    private final static String TAG = "MainActivityTag";
    private NavigationView drawer_nav_view;
    private DrawerLayout drawer;
    private BottomNavigationView bottom_navigation;
    private FrameLayout loading_layout;
    private ImageButton function_button;
    private TextView ham_menu_uni_text;
    private ImageView ham_memu_uni_image;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference stor = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private UserViewModel user_view_model;
    private User this_user;

    private SectionsPageAdapter tab_adapter = new SectionsPageAdapter(getSupportFragmentManager());
    private ChatFragment chat_fragment = new ChatFragment();
    private OrganizationProfileFragment org_fragment = new OrganizationProfileFragment();
    private StudentProfileFragment stud_fragment = new StudentProfileFragment();
    private NoSwipeViewPager tab_view_pager;
    private boolean login_setup = false;

    private final ArrayList<String> bubble_arraylist = new ArrayList<String>();
    private RecyclerView bubble_recycler_view;
    private RecyclerView.Adapter bubble_adapter;
    private RecyclerView.LayoutManager bubble_layout_manager;
    private CampusFragment campus_fragment = null;
    private EventsFragment event_fragment = null;
//    private PostsFragment post_fragment = null;
    private String selected_bubble = "campus";
    private boolean home_setup = false;







    // MARK: Override Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUp();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.LOGIN_REQUEST_CODE:
                attemptLogin();
                break;
            case Constant.CREATE_POST_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    campus_fragment.refreshAdapter();
                    if(event_fragment != null) event_fragment.refreshAdapter();
                    //TODO: don't forget to notify any other fragments
                }
        }
    }
















    // MARK: Menu & Navbar Methods

    private void setUpToolbar() {
        Toolbar main_toolbar = findViewById(R.id.main_toolbar_id);
        drawer_nav_view = findViewById(R.id.main_nav_view);
        setSupportActionBar(main_toolbar);
        drawer = findViewById(R.id.main_layout_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, main_toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawer_nav_view.setNavigationItemSelectedListener(this);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(null);
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.interaction));

        View hView =  drawer_nav_view.getHeaderView(0);
        ham_memu_uni_image = hView.findViewById(R.id.hamburger_menu_imageview);
        ham_menu_uni_text = hView.findViewById(R.id.hamburger_menu_bottomtext);
        setDrawerHeader();
    }

    private void setDrawerHeader(){
        ham_menu_uni_text.setText(Utils.getCampusUni(this));
        stor.child("unilogos/"+Utils.getCampusUni(this)+".png").getDownloadUrl().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null) Glide.with(this).load(task.getResult()).into(ham_memu_uni_image);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.drawer_contact_us_item:
                transToContactUs();
                break;
            case R.id.drawer_terms_item:
                transToTerms();
                break;
            case R.id.drawer_sign_out_item:
                if(login_setup) signOut();
                else Toast.makeText(this, "You are not logged in...", Toast.LENGTH_LONG).show();
                break;
            case R.id.drawer_change_campus_item:
                openUniChangeDialog();
                break;
        }
        return false;
    }

    private void transToContactUs(){
        Intent intent = new Intent(this, ContactUsActivity.class);
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }

    private void transToTerms(){
        Intent intent = new Intent(this, TermsActivity.class);
        startActivity(intent);
    }

    private void signOut(){
        auth.addAuthStateListener(firebaseAuth -> {
            if(auth.getCurrentUser() == null){
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("signed_out", true);
                startActivity(intent);
                finish();
            }
        });
        db.collection("users").document(this_user.getId()).update("messaging_token", "none").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Utils.deleteThis_user();
                Utils.clearNotif_list();
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                assert notificationManager != null;
                notificationManager.cancelAll();
                auth.signOut();
            }
        });

        // Just in case? // TODO
      //  Utils.deleteThis_user();
      //  auth.signOut();
    }

    private void openUniChangeDialog() {
        final Dialog infoDialog = new Dialog(this);
        infoDialog.setContentView(R.layout.dialog_change_uni);
        Button okButton = infoDialog.findViewById(R.id.uni_change_dialog_button);
        Spinner uniSpinner = infoDialog.findViewById(R.id.uni_change_dialog_spinner);

        SpinnerAdapter uniAdapter = new SpinnerAdapter(this, available_domain_list);
        uniAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        uniSpinner.setAdapter(uniAdapter);
        uniSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0) okButton.setEnabled(true);
                else okButton.setEnabled(false);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        okButton.setOnClickListener(view -> {
            changeCampus(uniAdapter.getItem(uniSpinner.getSelectedItemPosition()));
            infoDialog.dismiss();
        });
        ColorDrawable transparentColor = new ColorDrawable(Color.TRANSPARENT);
        if (infoDialog.getWindow() != null)
            infoDialog.getWindow().setBackgroundDrawable(transparentColor);
        infoDialog.setCancelable(true);
        infoDialog.show();
    }

    private void changeCampus(String newUni){
        Utils.setCampusUni(newUni, this);
        setDrawerHeader();
        campus_fragment.changeUni();
        if(event_fragment != null) event_fragment.changeUni();
    }








    //MARK: Setup Methods

    private void setUp(){
        setUpToolbar();
        setHandlers();
        attemptLogin();
    }

    private void setHandlers(){
        bottom_navigation = findViewById(R.id.main_tab_bar);
        bottom_navigation.setSelectedItemId(R.id.tab_bar_home);
        loading_layout = findViewById(R.id.main_loadingScreen);
        function_button = findViewById(R.id.post_button);
        tab_view_pager = findViewById(R.id.tab_view_pager);
        function_button.setOnClickListener(view -> transToLogin());
    }

    private void setUpLoggedInInteraction() { //this method will set up all the interactive elements the user has access to when logged in, by default they're hidden (tab bar + post btn)
        function_button.setImageResource(R.drawable.ic_create);
        function_button.setOnClickListener(view -> transToCreatePost());
        bottom_navigation.setVisibility(View.VISIBLE);

        tab_adapter.addFragment(chat_fragment, "chat");
        if (this_user.getIs_organization()) tab_adapter.addFragment(org_fragment, "organization");
        else tab_adapter.addFragment(stud_fragment, "student");

        if(!home_setup) homeSetup();

        bottom_navigation.setOnNavigationItemSelectedListener((menuItem) -> {
            switch (menuItem.getItemId()) {
                case R.id.tab_bar_chat:
                    bubble_recycler_view.setVisibility(View.GONE);
                    setFunctionButton(R.id.tab_bar_chat);
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("chat"));
                    return true;
                case R.id.tab_bar_home:
                    bubble_recycler_view.setVisibility(View.VISIBLE);
                    setFunctionButton(R.id.tab_bar_home);
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition(selected_bubble));
                    return true;
                case R.id.tab_bar_profile:
                    bubble_recycler_view.setVisibility(View.GONE);
                    setFunctionButton(R.id.tab_bar_profile);
                    if (this_user.getIs_organization()) {
                        if (!org_fragment.isIs_set_up()) org_fragment.setUp();
                        tab_view_pager.setCurrentItem(tab_adapter.getPosition("organization"));
                    } else {
                        if (!stud_fragment.isIs_set_up()) stud_fragment.setUpProfile();
                        tab_view_pager.setCurrentItem(tab_adapter.getPosition("student"));
                    }
                    return true;
            }
            return false;
        });
        tab_view_pager.setCurrentItem(tab_adapter.getPosition("campus"));
        login_setup = true;
    }

    private void homeSetup(){
        //TODO: Not ideal, all the bubbles essentially act as separate tabs -> limit 6 so that we can keep everything in mem, will have to be reworked in the future:
        //TODO: i.e. killing old bubbles when we reach the limit we decide on, instead of killing starting from 0th index (which will start killing actual tabs)
        bubbleBarSetup();
        campus_fragment = new CampusFragment(this, this_user);
        tab_adapter.addFragment(campus_fragment, "campus");
        tab_view_pager.setAdapter(tab_adapter);
        tab_view_pager.setOffscreenPageLimit(6);
        home_setup = true;
    }

    private void setFunctionButton(int tabId){
        if(this_user != null) {
            switch (tabId) {
                case R.id.tab_bar_profile:
                    function_button.setImageResource(R.drawable.ic_bell);
                    function_button.setOnClickListener(view -> transToNotificationCenter());
                    break;
                case R.id.tab_bar_home:
                    function_button.setImageResource(R.drawable.ic_create);
                    function_button.setOnClickListener(view -> transToCreatePost());
                    break;
                case R.id.tab_bar_chat:
                    function_button.setImageResource(R.drawable.ic_create);
                    function_button.setOnClickListener(view -> transToCreatePost());
                    break;
            }
        }
    }












    // MARK: Base Methods

    private void attemptLogin(){
        startLoading();
        user_view_model = new ViewModelProvider(this).get(UserViewModel.class);
        if(auth.getCurrentUser() != null && auth.getUid() != null){
            user_view_model.startListening(auth.getUid());
            user_view_model.getThis_user().observe(this, (User updatedUser) -> {
                if(updatedUser != null){
                    this_user = updatedUser;
                    if(this_user.getIs_banned()){
                        Toast.makeText(this, "Your account has been banned.", Toast.LENGTH_LONG).show();
                        signOut();
                    }else{
                        initFCM();
                        Utils.setCampusUni(this_user.getUni_domain(), this); //by default for the logged in user we want to display their campus
                        setDrawerHeader();
                        if(!login_setup) setUpLoggedInInteraction();
                        endLoading();
                    }
                }
            });
        }else{
            if(!home_setup) homeSetup();
            endLoading();
        }
    }

    private void bubbleBarSetup(){
        bubble_arraylist.add("Campus");
        bubble_arraylist.add("Events");
//        bubble_arraylist.add("Posts");
        bubble_recycler_view = findViewById(R.id.bubble_sample_rv);
        bubble_recycler_view.setHasFixedSize(true);
        bubble_layout_manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bubble_adapter = new BubbleTabAdapter(bubble_arraylist, position -> {
            switch (position){
                case 0:
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("campus"));
                    selected_bubble = "campus";
                    break;
                case 1:
                    if(event_fragment == null){
                        event_fragment = new EventsFragment(this, this_user);
                        tab_adapter.addFragment(event_fragment, "event");
                    }
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("event"));
                    selected_bubble = "event";
                    break;
//                case 2:
//                    if(post_fragment == null){
//                        post_fragment = new PostsFragment(this);
//                        post_fragment.setThisUser(this_user);
//                        tab_adapter.addFragment(post_fragment, "post");
//                    }
//                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("post"));
//                    selected_bubble = "post";
//                    break;
            }
        });
        bubble_recycler_view.setLayoutManager(bubble_layout_manager);
        bubble_recycler_view.setAdapter(bubble_adapter);
    }











    // MARK: Other Methods

    private void transToCreatePost(){
        Intent intent = new Intent(getApplicationContext(), CreatePost.class);
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Constant.CREATE_POST_REQUEST_CODE);
    }

    private void transToNotificationCenter(){
        Intent intent = new Intent(this, NotificationCenterActivity.class);
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }

    private void transToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, Constant.LOGIN_REQUEST_CODE);
    }

    private void startLoading(){
        loading_layout.setVisibility(View.VISIBLE);      // Bring up view to cover entire screen
        getSupportFragmentManager().beginTransaction().replace(R.id.main_loadingScreen, new LoadingPageFragment(this)).commit();      // Populate View with loading page layout
    }

    private void endLoading(){ // Fade out loading page animation
        loading_layout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));  // Start fade out animation
        loading_layout.setVisibility(View.GONE);
    }



    private void initFCM(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w("TAG", "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                String token = task.getResult().getToken();
                Utils.setThis_user(this_user);
                if(!token.equals(this_user.getMessaging_token())){
                    Utils.sendRegistrationToServer(token);
                    this_user.setMessaging_token(token);
                }
            }
        });
        //sendRegistrationToServer(token);
    }

}
