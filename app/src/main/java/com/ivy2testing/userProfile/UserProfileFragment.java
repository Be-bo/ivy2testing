package com.ivy2testing.userProfile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ivy2testing.MainActivity;
import com.ivy2testing.R;
import com.ivy2testing.authentication.LoginActivity;

public class UserProfileFragment extends Fragment {

    // Constants
    private final static String TAG = "UserProfileFragment";

    // Views

    // FireBase


    public UserProfileFragment(){
        // Required Empty public constructor
    }


/* Override Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_userprofile, container, false);
    }

/* Initialization Methods
***************************************************************************************************/

/* OnClick Methods
***************************************************************************************************/

/* Firebase related Methods
***************************************************************************************************/

/* Transition Methods
***************************************************************************************************/

/* UI related Methods
***************************************************************************************************/

/* Utility Methods
***************************************************************************************************/

    private void toastError(String msg){
        Log.w(TAG, msg);
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
