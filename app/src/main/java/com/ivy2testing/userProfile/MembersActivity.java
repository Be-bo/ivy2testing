package com.ivy2testing.userProfile;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;

class MembersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MembersAdapter membersAdapter;
    private Organization organization;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        declareHandles();
        setUp();
    }

    private void declareHandles(){
        recyclerView = findViewById(R.id.activity_members_recycler);
    }

    private void setUp(){
        setTitle("Members");
        organization = getIntent().getParcelableExtra("organization");
        boolean isEditable = getIntent().getBooleanExtra("isEditable", false);
        if(organization != null){
            setTitle(organization.getName()+" Members");
            membersAdapter = new MembersAdapter(this, organization, false);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, isEditable));
            recyclerView.setAdapter(membersAdapter);
        }
    }
}
