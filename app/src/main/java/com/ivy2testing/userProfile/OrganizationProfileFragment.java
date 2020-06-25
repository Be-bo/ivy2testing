package com.ivy2testing.userProfile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.adapters.CircleImageAdapter;
import com.ivy2testing.util.adapters.SquareImageAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrganizationProfileFragment extends Fragment implements SquareImageAdapter.OnPostListener, CircleImageAdapter.OnPersonListener {


    // MARK: Variables and Constants

    private static final String TAG = "OrganizationProfileFragmentTag";
    private static final int POST_LIMIT = 6;

    private View rootView;
    private TextView edit_button;
    private TextView member_requests_button;
    private TextView see_all_posts_button;
    private TextView see_all_members_button;
    private CircleImageView profile_image;
    private TextView name_text;
    private TextView member_number_text;

    private SquareImageAdapter post_adapter;
    private CircleImageAdapter person_adapter;
    private RecyclerView post_recycler;
    private RecyclerView members_recycler;

    private StorageReference stor_ref = FirebaseStorage.getInstance().getReference();
    private User this_user;
    private UserViewModel this_user_viewmodel;
    private boolean is_set_up = false;
    public boolean isIs_set_up() {
        return is_set_up;
    }







    // MARK: Base Methods

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_organizationprofile, container, false);
        declareHandles();
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(post_adapter!=null) post_adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(is_set_up && post_adapter != null) post_adapter.startListening();
    }

    public void setUp(){
        is_set_up = true;
        setUpFragment();
    }

    private void declareHandles(){
        edit_button = rootView.findViewById(R.id.orgprofile_edit_button);
        member_requests_button = rootView.findViewById(R.id.orgprofile_members_button);
        see_all_posts_button = rootView.findViewById(R.id.orgprofile_post_see_all);
        see_all_members_button = rootView.findViewById(R.id.orgprofile_members_see_all);
        post_recycler = rootView.findViewById(R.id.orgprofile_post_recycler);
        members_recycler = rootView.findViewById(R.id.orgprofile_members_recycler);
        profile_image = rootView.findViewById(R.id.orgprofile_image);
        name_text = rootView.findViewById(R.id.orgprofile_name);
        member_number_text = rootView.findViewById(R.id.orgprofile_members);
    }









    // MARK: Setup Methods

    private void setUpFragment(){
        getUserProfile();
        if(this_user != null){
            populateUI();
            setListeners();
        }
    }

    private void populateUI(){
        name_text.setText(this_user.getName());
        String memberNumber = String.valueOf(((Organization)this_user).getMember_ids().size());
        String requestNumber = String.valueOf(((Organization)this_user).getRequest_ids().size());
        member_number_text.setText(getString(R.string.organization_member_number, memberNumber));
        member_requests_button.setText(getString(R.string.organization_request_number, requestNumber));
        String profPicPath = "userfiles/"+this_user.getId()+"/profileimage.jpg";
        stor_ref.child(profPicPath).getDownloadUrl().addOnCompleteListener(task -> {if(task.isSuccessful() && getContext() != null) Glide.with(getContext()).load(task.getResult()).into(profile_image);});
        setUpRecyclers();
    }

    private void setListeners(){
        see_all_members_button.setOnClickListener(view -> transToMembers());
        see_all_posts_button.setOnClickListener(view -> transToPosts());
        member_requests_button.setOnClickListener(view -> transToRequests());
        edit_button.setOnClickListener(view -> transToEdit());
    }

    private void setUpRecyclers(){
        post_adapter = new SquareImageAdapter(this_user.getId(), this_user.getUni_domain(), 9, getContext(), this);
        post_recycler.setLayoutManager(new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false));
        post_recycler.setAdapter(post_adapter);

        person_adapter = new CircleImageAdapter(((Organization)this_user).getMember_ids(), this_user.getUni_domain(), getContext(), this);
        members_recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        members_recycler.setAdapter(person_adapter);
    }

    @Override
    public void onPostClick(int position) {
        Intent intent = new Intent(getContext(), ViewPostOrEventActivity.class);
        intent.putExtra("viewer_id", this_user.getId());
        intent.putExtra("post", post_adapter.getItem(position));
        startActivity(intent);
    }

    @Override
    public void onPersonClicked(int position) {
        //TODO
    }

    private void setUpMembers(){

    }

    private void getUserProfile(){
        if (getActivity() != null) {
            this_user_viewmodel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            this_user = this_user_viewmodel.getThis_user().getValue();
            this_user_viewmodel.getThis_user().observe(getActivity(), (User updatedProfile) -> { //listen to realtime user profile changes afterwards
                this_user = updatedProfile;
                //TODO: must work in terms of profile edits
//                populateUI();
            });
        }
    }









    // MARK: Transition Methods

    private void transToMembers(){
        Intent intent = new Intent(getContext(), MembersActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("isEditable", true);
        intent.putExtra("isRequests", false);
        startActivity(intent);
    }

    private void transToRequests(){
        Intent intent = new Intent(getContext(), MembersActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("isEditable", true);
        intent.putExtra("isRequests", true);
        startActivity(intent);
    }

    private void transToPosts(){

    }

    private void transToEdit(){
        Intent intent = new Intent(getContext(), EditOrganizationProfileActivity.class);
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Constant.EDIT_ORGANIZATION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.EDIT_ORGANIZATION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) populateUI(); //changes were made...
        } else
            Log.w(TAG, "Don't know how to handle the request code, \"" + requestCode + "\" yet!");
    }
}
