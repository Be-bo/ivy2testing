package com.ivy2testing.userProfile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.SeeAllPostsActivity;
import com.ivy2testing.home.SeeAllUsersActivity;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.adapters.CircleImageAdapter;
import com.ivy2testing.util.adapters.SquareImageAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrganizationProfileActivity extends AppCompatActivity implements SquareImageAdapter.OnPostListener, CircleImageAdapter.OnPersonListener{




    // MARK: Variables and Constants

    private static final String TAG = "OrganizationProfileActivityTag";
    private static final int POST_LIMIT = 6;
    private static final int MEMBER_LIMIT = 5;

    private Button join_button;
    private TextView see_all_posts_button;
    private TextView see_all_members_button;
    private TextView member_status_text;
    private CircleImageView profile_image;
    private TextView name_text;
    private TextView member_number_text;
    private TextView post_title;
    private TextView member_title;
    private View post_divider;
    private View member_divider;

    private StorageReference stor_ref = FirebaseStorage.getInstance().getReference();
    private FirebaseFirestore db_ref = FirebaseFirestore.getInstance();

    private RecyclerView post_recycler;
    private RecyclerView members_recycler;
    private SquareImageAdapter post_adapter;
    private CircleImageAdapter person_adapter;

    private User this_user;
    private String org_to_display_uni;
    private String org_to_display_id;
    private Organization org_to_display;








    // MARK: Override Methods

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_profile);
        declareHandles();
        getIncomingData();
        setUpActivity();
    }

    @Override
    public void onPostClick(int position) {
        Intent intent = new Intent(this, ViewPostOrEventActivity.class);
        intent.putExtra("viewer_id", org_to_display_id);
        intent.putExtra("post", post_adapter.getItem(position));
        startActivity(intent);
    }

    @Override
    public void onPersonClicked(int position) {
        Intent intent = new Intent(this, StudentProfileActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("student_to_display_id", person_adapter.getItem(position));
        intent.putExtra("student_to_display_uni", org_to_display_uni);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(post_adapter!=null) post_adapter.stopListening();
    }











    // MARK: Setup Functions

    private void declareHandles(){
        join_button = findViewById(R.id.activity_orgprofile_join_button);
        see_all_posts_button = findViewById(R.id.activity_orgprofile_post_see_all);
        see_all_members_button = findViewById(R.id.activity_orgprofile_members_see_all);
        post_recycler = findViewById(R.id.activity_orgprofile_post_recycler);
        members_recycler = findViewById(R.id.activity_orgprofile_members_recycler);
        profile_image = findViewById(R.id.activity_orgprofile_image);
        name_text = findViewById(R.id.activity_orgprofile_name);
        member_number_text = findViewById(R.id.activity_orgprofile_members);
        member_status_text = findViewById(R.id.activity_orgprofile_member_status);
        post_title = findViewById(R.id.activity_orgprofile_posts_header);
        post_divider = findViewById(R.id.activity_orgprofile_divider1);
        member_title = findViewById(R.id.activity_orgprofile_members_header);
        member_divider = findViewById(R.id.activity_orgprofile_divider2);
    }

    private void getIncomingData(){
        this_user = getIntent().getParcelableExtra("this_user");
        org_to_display_id = getIntent().getStringExtra("org_to_display_id");
        org_to_display_uni = getIntent().getStringExtra("org_to_display_uni");
        //TODO: this crashes whatever happens before
        if(this_user == null || (org_to_display_id == null && org_to_display_uni == null)) finish();
    }

    private void setUpActivity(){
        db_ref.collection("universities").document(org_to_display_uni).collection("users").document(org_to_display_id).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null && task.getResult().exists()){
                org_to_display = task.getResult().toObject(Organization.class);
                if(org_to_display != null) populateUI();
            }else{
                Toast.makeText(this, "Couldn't load organization data. :-(", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateUI(){
        setTitle("Profile");
        if(this_user.getUni_domain().equals(org_to_display_uni)) join_button.setEnabled(true);
        if(org_to_display.getMember_ids().contains(this_user.getId())){
            member_status_text.setText(getString(R.string.you_are_a_member));
            member_status_text.setVisibility(View.VISIBLE);
            join_button.setText(R.string.leave_org);
            join_button.setOnClickListener(view -> leaveOrg());
        }
        else if(org_to_display.getRequest_ids().contains(this_user.getId())){
            member_status_text.setText(getString(R.string.request_pending));
            member_status_text.setVisibility(View.VISIBLE);
            join_button.setText(R.string.cancel);
            join_button.setOnClickListener(view -> cancelRequest());
        }
        else{
            member_status_text.setVisibility(View.GONE);
            join_button.setText(R.string.join);
            join_button.setOnClickListener(view -> requestMembership());
        }
        if(this_user.getIs_organization()) join_button.setVisibility(View.GONE);
        name_text.setText(org_to_display.getName());
        String memberNumber = String.valueOf(org_to_display.getMember_ids().size());
        member_number_text.setText(getString(R.string.organization_member_number, memberNumber));
        String profPicPath = "userfiles/"+org_to_display_id+"/profileimage.jpg";
        stor_ref.child(profPicPath).getDownloadUrl().addOnCompleteListener(task -> { if(task.getResult() != null)Glide.with(this).load(task.getResult()).into(profile_image);});
        setUpRecyclers();
        setUpMembers();
        see_all_members_button.setOnClickListener(view -> transToMembers());
        see_all_posts_button.setOnClickListener(view -> transToPosts());
    }

    private void setUpRecyclers(){
        List<View> allViews = new ArrayList<>();
        allViews.add(post_recycler);
        allViews.add(see_all_posts_button);
        allViews.add(post_divider);
        allViews.add(post_title);
        post_adapter = new SquareImageAdapter(org_to_display_id, org_to_display_uni, POST_LIMIT, this, this, allViews);
        post_recycler.setLayoutManager(new GridLayoutManager(this, Constant.PROFILE_POST_GRID_ROW_COUNT, GridLayoutManager.VERTICAL, false){
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = getWidth() / Constant.PROFILE_POST_GRID_ROW_COUNT;
                return true;
            }
        });
        post_recycler.setAdapter(post_adapter);
    }

    private void setUpMembers(){
        if(org_to_display.getMember_ids().size() > 0){
            member_title.setVisibility(View.VISIBLE);
            members_recycler.setVisibility(View.VISIBLE);
            see_all_members_button.setVisibility(View.VISIBLE);
            member_divider.setVisibility(View.VISIBLE);
            if(org_to_display.getMember_ids().size() < MEMBER_LIMIT) person_adapter = new CircleImageAdapter(org_to_display.getMember_ids(), org_to_display_uni, this, this);
            else person_adapter = new CircleImageAdapter(org_to_display.getMember_ids().subList(0, MEMBER_LIMIT), org_to_display_uni, this, this);
            members_recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL,false){
                @Override
                public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                    lp.width = getWidth() / Constant.PROFILE_MEMBER_LIMIT;
                    return true;
                }
            });
            members_recycler.setAdapter(person_adapter);
        }else{
            member_title.setVisibility(View.GONE);
            members_recycler.setVisibility(View.GONE);
            see_all_members_button.setVisibility(View.GONE);
            member_divider.setVisibility(View.GONE);
        }
    }









    // MARK: Interaction Functions

    private void requestMembership(){
        db_ref.collection("universities").document(org_to_display.getUni_domain()).collection("users").document(org_to_display_id).update("request_ids",FieldValue.arrayUnion(this_user.getId())).addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               join_button.setVisibility(View.GONE);
               member_status_text.setText(getString(R.string.request_pending));
               member_status_text.setVisibility(View.VISIBLE);
           }else{
               Toast.makeText(this, "Failed to send request. :-(", Toast.LENGTH_LONG).show();
           }
        });
    }

    private void leaveOrg(){
        //TODO
    }

    private void cancelRequest(){
        //TODO
    }

    private void transToMembers(){
        Intent intent = new Intent(this, SeeAllUsersActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("title", org_to_display.getName()+"'s Members");
        intent.putExtra("uni_domain", org_to_display_uni);
        intent.putExtra("user_ids", new ArrayList<>(org_to_display.getMember_ids()));
        startActivity(intent);
    }

    private void transToPosts(){
        Intent intent = new Intent(this, SeeAllPostsActivity.class);
        intent.putExtra("title", org_to_display.getName()+"'s Posts");
        intent.putExtra("this_user", this_user);
        intent.putExtra("uni_domain", org_to_display_uni);
        HashMap<String, Object> query_map = new HashMap<String, Object>() {{ put("author_id", org_to_display_id); }};
        intent.putExtra("query_map", query_map);
        startActivity(intent);
    }
}
