package com.ivy2testing.userProfile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrganizationProfileFragment extends Fragment {


    // MARK: Variables and Constants

    private View rootView;

    private TextView editButton;
    private TextView memberRequestsButton;
    private TextView seeAllPostsButton;
    private TextView seeAllMembersButton;

    private RecyclerView postRecyclerView;
    private RecyclerView membersRecyclerView;
    private CircleImageView profileImage;

    private TextView nameText;
    private TextView memberNumberText;

    private Organization thisOrganization;





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
        editButton = rootView.findViewById(R.id.orgprofile_edit_button);
        memberRequestsButton = rootView.findViewById(R.id.orgprofile_members_button);
        seeAllPostsButton = rootView.findViewById(R.id.orgprofile_post_see_all);
        seeAllMembersButton = rootView.findViewById(R.id.orgprofile_members_see_all);
        postRecyclerView = rootView.findViewById(R.id.orgprofile_post_recycler);
        membersRecyclerView = rootView.findViewById(R.id.orgprofile_members_recycler);
        profileImage = rootView.findViewById(R.id.orgprofile_image);
        nameText = rootView.findViewById(R.id.orgprofile_name);
        memberNumberText = rootView.findViewById(R.id.orgprofile_members);
    }






    // MARK: Set Up Methods

    private void setUpFragment(){
        //TODO: get user view model
        if(thisOrganization != null){
            nameText.setText(thisOrganization.getName());
            memberNumberText.setText(getString(R.string.organization_member_number, thisOrganization.getMember_ids().size()));
            memberRequestsButton.setText(getString(R.string.organization_request_number, thisOrganization.getRequest_ids().size()));
            profileImage.setOnClickListener(view -> changeProfPic());
            seeAllMembersButton.setOnClickListener(view -> transToMembers());
            seeAllPostsButton.setOnClickListener(view -> transToPosts());
            memberRequestsButton.setOnClickListener(view -> transToRequests());
            setUpRecyclerViews();
        }
    }

    private void setUpRecyclerViews(){
        //TODO: make a universal ppl recyclerview (same as post and event displaying)
        //TODO: wait for Zahra's post displaying
    }





    // MARK: Interaction Methods

    private void changeProfPic(){
        //TODO: woah, carriage return
    }

    private void transToMembers(){
        Intent intent = new Intent(getContext(), MembersActivity.class);
        intent.putExtra("organization", thisOrganization);
        intent.putExtra("isEditable", true);
        intent.putExtra("isRequests", false);
        startActivity(intent);
    }

    private void transToRequests(){
        Intent intent = new Intent(getContext(), MembersActivity.class);
        intent.putExtra("organization", thisOrganization);
        intent.putExtra("isEditable", true);
        intent.putExtra("isRequests", true);
        startActivity(intent);
    }

    private void transToPosts(){
        //TODO: Terry Davis
    }
}
