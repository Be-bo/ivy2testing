package com.ivy2testing.userProfile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.util.adapters.SquareImageAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

class OrganizationProfileActivity extends AppCompatActivity {

    // MARK: Variables and Constants

    private static final String TAG = "OrganizationProfileFragmentTag";
    private static final int POST_LIMIT = 6;
    private View rootView;
    private Button join_button;
    private TextView see_all_posts_button;
    private TextView see_all_members_button;
    private RecyclerView post_recycler;
    private RecyclerView members_recycler;
    private CircleImageView profile_image;
    private TextView name_text;
    private TextView member_number_text;
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();
    private FirebaseFirestore base_database_reference = FirebaseFirestore.getInstance();
    private User this_user;
    private SquareImageAdapter post_adapter;
    private String org_to_display_uni;
    private String org_to_display_id;
    private Organization org_to_display;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_profile);
        declareHandles();
        getIncomingData();
        setUpActivity();
    }

    private void getIncomingData(){
        this_user = getIntent().getParcelableExtra("this_user");
        org_to_display_id = getIntent().getStringExtra("org_to_display_id");
        org_to_display_uni = getIntent().getStringExtra("org_to_display_uni");
        if(this_user == null || org_to_display_id == null && org_to_display_uni == null) finish();
    }

    private void setUpActivity(){
        base_database_reference.collection("universities").document(org_to_display_uni).collection("users").document(org_to_display_id).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                org_to_display = task.getResult().toObject(Organization.class);
                populateUI();
            }else{
                Toast.makeText(this, "Couldn't load organization data. :-(", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void declareHandles(){
        join_button = rootView.findViewById(R.id.activity_orgprofile_join_button);
        see_all_posts_button = rootView.findViewById(R.id.activity_orgprofile_post_see_all);
        see_all_members_button = rootView.findViewById(R.id.activity_orgprofile_members_see_all);
        post_recycler = rootView.findViewById(R.id.activity_orgprofile_post_recycler);
        members_recycler = rootView.findViewById(R.id.activity_orgprofile_members_recycler);
        profile_image = rootView.findViewById(R.id.activity_orgprofile_image);
        name_text = rootView.findViewById(R.id.activity_orgprofile_name);
        member_number_text = rootView.findViewById(R.id.activity_orgprofile_members);
    }

    private void populateUI(){
        if(this_user.getUni_domain().equals(org_to_display_uni)) join_button.setEnabled(true);
        name_text.setText(org_to_display.getName());
        String memberNumber = String.valueOf(org_to_display.getMember_ids().size());
        member_number_text.setText(getString(R.string.organization_member_number, memberNumber));
        String profPicPath = "userfiles/"+org_to_display_id+"/profileimage.jpg";
        base_storage_ref.child(profPicPath).getDownloadUrl().addOnCompleteListener(task -> { if(task.getResult() != null)Glide.with(this).load(task.getResult()).into(profile_image);});
        join_button.setOnClickListener(view -> requestMembership());
        see_all_members_button.setOnClickListener(view -> transToMembers());
        see_all_posts_button.setOnClickListener(view -> transToPosts());
    }

    private void requestMembership(){
        //TODO
    }

    private void transToMembers(){
        //TODO
    }

    private void transToPosts(){
        //TODO
    }


}
