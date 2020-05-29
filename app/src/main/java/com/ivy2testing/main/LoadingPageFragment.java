package com.ivy2testing.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ivy2testing.R;

public class LoadingPageFragment extends Fragment {

    // Constants
    private final static String TAG = "UserProfileFragment";

    // Views
    private Context mContext;
    private ImageView img;


    // Constructor
    public LoadingPageFragment(Context c){
        this.mContext = c;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_loading, container, false);

        img = rootView.findViewById(R.id.loading_img);
        startAnimation();

        return rootView;
    }

    // Animation
    private void startAnimation() {
       Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.spinning_logo);
       //animation.setInterpolator(android.R.anim.linear_interpolator);
       animation.setRepeatCount(Animation.INFINITE);
       img.startAnimation(animation);
    }


}
