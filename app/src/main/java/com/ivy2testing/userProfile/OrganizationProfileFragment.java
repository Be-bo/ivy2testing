package com.ivy2testing.userProfile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.ivy2testing.main.SeeAllPostsActivity;
import com.ivy2testing.main.SeeAllUsersActivity;
import com.ivy2testing.hometab.ViewPostOrEventActivity;
import com.ivy2testing.main.UserViewModel;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.adapters.CircleUserAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrganizationProfileFragment extends Fragment implements ProfilePostAdapter.OnPostListener, CircleUserAdapter.OnPersonListener {


    // MARK: Variables and Constants

    private static final String TAG = "OrganizationProfileFragmentTag";

    private View rootView;
    private TextView edit_button;
    private TextView member_requests_button;
    private TextView see_all_members_button;
    private CircleImageView profile_image;
    private TextView name_text;
    private TextView member_number_text;
    private TextView member_title;
    private TextView post_title;
    private View post_divider;
    private View member_divider;
    private TextView no_posts_text;
    private ProgressBar progress_bar;

    private ProfilePostAdapter post_adapter;
    private CircleUserAdapter person_adapter;
    private RecyclerView post_recycler;
    private RecyclerView members_recycler;

    private final StorageReference stor_ref = FirebaseStorage.getInstance().getReference();
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
    }

    @Override
    public void onStart() {
        super.onStart();
        if(is_set_up && post_adapter != null){
            post_adapter.refreshAdapter();
            setInteractionListeners();
        }
    }

    public void setUp(){
        is_set_up = true;
        setUpFragment();
    }

    private void declareHandles(){
        edit_button = rootView.findViewById(R.id.orgprofile_edit_button);
        member_requests_button = rootView.findViewById(R.id.orgprofile_members_button);
        see_all_members_button = rootView.findViewById(R.id.orgprofile_members_see_all);
        post_recycler = rootView.findViewById(R.id.orgprofile_post_recycler);
        members_recycler = rootView.findViewById(R.id.orgprofile_members_recycler);
        profile_image = rootView.findViewById(R.id.orgprofile_image);
        name_text = rootView.findViewById(R.id.orgprofile_name);
        member_number_text = rootView.findViewById(R.id.orgprofile_members);
        member_title = rootView.findViewById(R.id.orgprofile_members_header);
        post_title = rootView.findViewById(R.id.orgprofile_posts_header);
        post_divider = rootView.findViewById(R.id.orgprofile_divider1);
        member_divider = rootView.findViewById(R.id.orgprofile_divider2);
        no_posts_text = rootView.findViewById(R.id.orgprofile_no_posts);
        progress_bar = rootView.findViewById(R.id.orgprofile_progress_bar);
    }









    // MARK: Setup Methods

    private void setUpFragment(){
        getUserProfile();
        if(this_user != null){
            populateUI();
            setUpMembers();
            setUpPosts();
            setInteractionListeners();
        }
    }

    private void populateUI(){
        name_text.setText(this_user.getName());
        String memberNumber = String.valueOf(((Organization)this_user).getMember_ids().size());
        String requestNumber = String.valueOf(((Organization)this_user).getRequest_ids().size());
        if(memberNumber.equals("1")) member_number_text.setText(getString(R.string.one_member));
        else member_number_text.setText(getString(R.string.organization_member_number, memberNumber));
        member_requests_button.setText(getString(R.string.organization_request_number, requestNumber));
        String profPicPath = ImageUtils.getUserImagePath(this_user.getId());
        try {
            stor_ref.child(profPicPath).getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful() && getContext() != null)
                    Glide.with(getContext()).load(task.getResult()).into(profile_image);
            });
        } catch (Exception e) {
            Log.w(TAG, "StorageException! No Preview Image for this user.");
        }
    }

    private void setInteractionListeners(){
        if(getContext()!=null){
            if(((Organization)this_user).getRequest_ids().size()<1) member_requests_button.setTextColor(ContextCompat.getColor(getContext(), R.color.light_grey));
            else{
                member_requests_button.setTextColor(ContextCompat.getColor(getContext(), R.color.interaction));
                member_requests_button.setOnClickListener(view -> transToRequests());
            }
        }
        see_all_members_button.setOnClickListener(view -> transToMembers());
        edit_button.setOnClickListener(view -> transToEdit());
    }

    private void setUpPosts(){
        List<View> allViews = new ArrayList<>();
        allViews.add(post_recycler);
        allViews.add(post_divider);
        allViews.add(post_title);
        post_adapter = new ProfilePostAdapter(this_user.getId(), this_user.getUni_domain(), Constant.PROFILE_POST_LIMIT_ORG, getContext(), this, allViews, no_posts_text, post_recycler, progress_bar);
        post_recycler.setLayoutManager(new GridLayoutManager(getContext(), Constant.PROFILE_POST_GRID_ROW_COUNT, GridLayoutManager.VERTICAL, false){
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = getWidth() / Constant.PROFILE_POST_GRID_ROW_COUNT;
                return true;
            }
        });
        post_recycler.setAdapter(post_adapter);
    }

    private void setUpMembers(){
        if(((Organization)this_user).getMember_ids().size() > 0){
            member_title.setVisibility(View.VISIBLE);
            members_recycler.setVisibility(View.VISIBLE);
            see_all_members_button.setVisibility(View.VISIBLE);
            member_divider.setVisibility(View.VISIBLE);
            if(((Organization)this_user).getMember_ids().size() < Constant.PEOPLE_PREVIEW_LIMIT){ //if less members than how many we're displaying in profile preview -> hide see all button and give full list
                person_adapter = new CircleUserAdapter(((Organization)this_user).getMember_ids(), getContext(), this);
                see_all_members_button.setVisibility(View.GONE);
            }else{ //otherwise show see all button and only feed the adapter a sublist (we don't need to load all members when we're only displaying 5 or so)
                person_adapter = new CircleUserAdapter(((Organization)this_user).getMember_ids().subList(0, Constant.PEOPLE_PREVIEW_LIMIT), getContext(), this);
                see_all_members_button.setVisibility(View.VISIBLE);
            }
            members_recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false){
                @Override
                public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                    lp.width = getWidth() / Constant.PEOPLE_PREVIEW_LIMIT;
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

    @Override
    public void onPostClick(int position) {
        Intent intent = new Intent(getContext(), ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post_id", post_adapter.getItem(position).getId());
        intent.putExtra("post_uni", post_adapter.getItem(position).getUni_domain());
        intent.putExtra("author_id", post_adapter.getItem(position).getAuthor_id());
        startActivity(intent);
    }

    @Override
    public void onPersonClicked(int position) {
        Intent intent = new Intent(getContext(), StudentProfileActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("student_to_display_id", person_adapter.getItem(position));
        intent.putExtra("student_to_display_uni", this_user.getUni_domain());
        startActivity(intent);
    }

    private void getUserProfile(){
        if (getActivity() != null) {
            this_user_viewmodel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            this_user = this_user_viewmodel.getThis_user().getValue();
            this_user_viewmodel.getThis_user().observe(getActivity(), (User updatedProfile) -> { //listen to realtime user profile changes afterwards
                if(updatedProfile != null){
                    this_user = updatedProfile;
                    populateUI();
                    setInteractionListeners();
                    setUpMembers();
                }
            });
        }
    }









    // MARK: Transition Methods

    private void transToMembers(){
        Intent intent = new Intent(getContext(), SeeAllUsersActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("title", "Your Members");
        intent.putExtra("uni_domain", this_user.getUni_domain());
        intent.putExtra("user_ids", new ArrayList<>(((Organization)this_user).getMember_ids()));
        startActivity(intent);
    }

    private void transToRequests(){
        Intent intent = new Intent(getContext(), SeeAllUsersActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("title","Your Member Requests");
        intent.putExtra("uni_domain", this_user.getUni_domain());
        intent.putExtra("user_ids", new ArrayList<>(((Organization)this_user).getRequest_ids()));
        intent.putExtra("shows_member_requests", true);
        startActivity(intent);
    }

    private void transToPosts(){
        Intent intent = new Intent(getContext(), SeeAllPostsActivity.class);
        intent.putExtra("title", "Your Posts");
        intent.putExtra("this_user", this_user);
        intent.putExtra("uni_domain", this_user.getUni_domain());
        HashMap<String, Object> query_map = new HashMap<String, Object>() {{ put("author_id", this_user.getId()); }};
        intent.putExtra("query_map", query_map);
        startActivity(intent);
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
