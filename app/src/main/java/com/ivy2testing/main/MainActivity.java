package com.ivy2testing.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.authentication.LoginActivity;
import com.ivy2testing.entities.User;
import com.ivy2testing.R;
import com.ivy2testing.bubbletabs.EventsFragment;
import com.ivy2testing.bubbletabs.CampusFragment;
import com.ivy2testing.home.CreatePostActivity;
import com.ivy2testing.terms.TermsActivity;
import com.ivy2testing.userProfile.NotificationCenterActivity;
import com.ivy2testing.userProfile.OrganizationProfileFragment;
import com.ivy2testing.userProfile.StudentProfileFragment;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.SpinnerAdapter;
import com.ivy2testing.util.Utils;

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

    private StorageReference stor = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private UserViewModel user_view_model;
    private User this_user;

    private SectionsPageAdapter tab_adapter = new SectionsPageAdapter(getSupportFragmentManager());
    private OrganizationProfileFragment org_fragment = new OrganizationProfileFragment();
    private StudentProfileFragment stud_fragment = new StudentProfileFragment();
    private NoSwipeViewPager tab_view_pager;
    private boolean login_setup = false;

    private CampusFragment campus_fragment;
    private EventsFragment event_fragment;










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
                    if(event_fragment != null) event_fragment.refreshAdapters();
                }
                break;
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

        main_toolbar.setNavigationIcon(R.drawable.ic_settings);
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
        auth.signOut();
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















    // MARK: Login Methods

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
                        Utils.setCampusUni(this_user.getUni_domain(), this); //by default for the logged in user we want to display their campus
                        setDrawerHeader();
                        if(!login_setup) setUpLoggedInInteraction();
                        endLoading();
                    }
                }
            });
        }else{
            setUpNoLoginInteraction();
            endLoading();
        }
    }














    //MARK: Setup Methods

    private void setUp(){
        setUpToolbar();
        setHandlers();
        attemptLogin();
    }

    private void setHandlers(){
        bottom_navigation = findViewById(R.id.activity_main_tab_bar_logged_in);
        loading_layout = findViewById(R.id.main_loadingScreen);
        function_button = findViewById(R.id.post_button);
        tab_view_pager = findViewById(R.id.tab_view_pager);
        function_button.setOnClickListener(view -> transToLogin());
    }

    private void setUpLoggedInInteraction() { //this method will set up all the interactive elements the user has access to when logged in, by default they're hidden (tab bar + post btn)
        bottom_navigation.getMenu().findItem(R.id.tab_bar_profile).setVisible(true);
        bottom_navigation.setSelectedItemId(R.id.tab_bar_home);
        function_button.setImageResource(R.drawable.ic_create);
        function_button.setOnClickListener(view -> transToCreatePost());

        if(event_fragment != null) event_fragment.refreshAdapters(); //if we're coming back from login events fragment already exists
        else{ //not coming back, starting the app already logged in
            event_fragment = new EventsFragment(this, this_user);
            tab_adapter.addFragment(event_fragment, "event");
        }
        if(campus_fragment != null) campus_fragment.refreshAdapter(); //same for the campus fragment
        else{
            campus_fragment = new CampusFragment(this, this_user);
            tab_adapter.addFragment(campus_fragment, "campus");
        }

        if (this_user.getIs_organization()) tab_adapter.addFragment(org_fragment, "organization");
        else tab_adapter.addFragment(stud_fragment, "student");

        tab_view_pager.setAdapter(tab_adapter);
        tab_view_pager.setOffscreenPageLimit(4);
        setUpBottomNavigationInteraction();
        login_setup = true;
    }

    private void setUpNoLoginInteraction(){ //this will only set up home and events fragments (user not signed in they don't have access to anything else)
        bottom_navigation.getMenu().findItem(R.id.tab_bar_profile).setVisible(false);
        bottom_navigation.setSelectedItemId(R.id.tab_bar_home);
        event_fragment = new EventsFragment(this, this_user);
        tab_adapter.addFragment(event_fragment, "event");
        campus_fragment = new CampusFragment(this, this_user);
        tab_adapter.addFragment(campus_fragment, "campus");
        tab_view_pager.setAdapter(tab_adapter);
        tab_view_pager.setOffscreenPageLimit(3);
        setUpBottomNavigationInteraction();
    }

    private void setUpBottomNavigationInteraction(){ //this method sets up menu button clicks for the current bottom navigation bar
        bottom_navigation.setOnNavigationItemSelectedListener((menuItem) -> {
            switch (menuItem.getItemId()) {
                case R.id.tab_bar_home:
                    setFunctionButton(R.id.tab_bar_home);
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("campus"));
                    return true;
                case R.id.tab_bar_events:
                    setFunctionButton(R.id.tab_bar_events);
                    tab_view_pager.setCurrentItem(tab_adapter.getPosition("event"));
                    return true;
                case R.id.tab_bar_profile:
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
    }

    private void setFunctionButton(int tabId){
        if(this_user != null) {
            switch (tabId) {
                case R.id.tab_bar_profile:
                    function_button.setImageResource(R.drawable.ic_bell);
                    function_button.setOnClickListener(view -> transToNotificationCenter());
                    break;
                default:
                    function_button.setImageResource(R.drawable.ic_create);
                    function_button.setOnClickListener(view -> transToCreatePost());
                    break;
            }
        }
    }























    // MARK: Other Methods

    private void transToCreatePost(){
        Intent intent = new Intent(getApplicationContext(), CreatePostActivity.class);
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
}
