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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.entities.User;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.util.Constant;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrganizationProfileFragment extends Fragment {


    // MARK: Variables and Constants

    private static final String TAG = "OrganizationProfileFragmentTag";
    private View rootView;
    private TextView edit_button;
    private TextView member_requests_button;
    private TextView see_all_posts_button;
    private TextView see_all_members_button;
    private RecyclerView post_recycler;
    private RecyclerView members_recycler;
    private CircleImageView profile_image;
    private TextView name_text;
    private TextView member_number_text;
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();
    private User this_user;
    private UserViewModel this_user_viewmodel;





    // MARK: Base Methods

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_organizationprofile, container, false);
        declareHandles();
        setUpFragment();
        return rootView;
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
        base_storage_ref.child(profPicPath).getDownloadUrl().addOnCompleteListener(task -> {if(task.isSuccessful() && getContext() != null) Glide.with(getContext()).load(task.getResult()).into(profile_image);});
        setUpRecyclerViews();
    }

    private void setListeners(){
        see_all_members_button.setOnClickListener(view -> transToMembers());
        see_all_posts_button.setOnClickListener(view -> transToPosts());
        member_requests_button.setOnClickListener(view -> transToRequests());
        edit_button.setOnClickListener(view -> transToEdit());
    }

    private void setUpRecyclerViews(){
        //TODO
    }

    private void getUserProfile(){
        if (getActivity() != null) {
            this_user_viewmodel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            this_user = this_user_viewmodel.getThis_user().getValue();
            this_user_viewmodel.getThis_user().observe(getActivity(), (User updatedProfile) -> { //listen to realtime user profile changes afterwards
                this_user = updatedProfile;
                populateUI();
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
