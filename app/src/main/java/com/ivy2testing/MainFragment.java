package com.ivy2testing;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


//https://google-developer-training.github.io/android-developer-advanced-course-practicals/unit-1-expand-the-user-experience/lesson-1-fragments/1-1-p-creating-a-fragment-with-a-ui/1-1-p-creating-a-fragment-with-a-ui.html

public class MainFragment extends Fragment {


    public MainFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    // returns inflated view
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // if we need information from the main activity, we can add it here
        // MainActivity current_activity = (MainActivity) getActivity();
        return view;
    }
    //return an instance
    public static MainFragment newInstance(){
        return new MainFragment();
    }
}
