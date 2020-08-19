package com.ivy2testing.bubbletabs;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.home.ViewPostOrEventActivity;
import com.ivy2testing.util.Utils;

public class ExploreAllEventsActivity extends AppCompatActivity implements ExploreEventsAdapter.AllEventsItemClickListener {

    private RecyclerView recycler;
    private ExploreEventsAdapter adapter;
    private User this_user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_events);
        setTitle(Utils.getCampusUni(this)+" Events");
        setUp();
    }

    private void setUp(){
        this_user = getIntent().getParcelableExtra("this_user");
        recycler = findViewById(R.id.activity_view_all_events_recycler);
        adapter = new ExploreEventsAdapter(this, this);
        recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recycler.setAdapter(adapter);
    }

    @Override
    public void onEventClick(int position) {
        viewEvent(adapter.getItem(position));
    }

    private void viewEvent(Post event) { // Transition to a post/event
        Intent intent = new Intent(this, ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post", event);
        startActivity(intent);
    }
}
