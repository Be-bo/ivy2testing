package com.ivy2testing.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

import com.ivy2testing.chat.ChatFragment;
import com.ivy2testing.entities.User;
import com.ivy2testing.eventstab.EventsFragment;
import com.ivy2testing.hometab.HomeFragment;

/**
 * New way to create Fragments.
 * Use a fragment factory when using custom fragment constructors
 * Will cause crash otherwise...
 */
public class MyFragmentFactory extends FragmentFactory {


    private final Context con;
    private final User this_user;

    public MyFragmentFactory(Context con, User this_user) {
        this.con = con;
        this.this_user = this_user;
    }

    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        Class <? extends Fragment> clazz = loadFragmentClass(classLoader, className);

        if (clazz == HomeFragment.class) {
            return new HomeFragment(con, this_user);
        }
        else if (clazz == EventsFragment.class) {
            return new EventsFragment(con, this_user);
        }
        else if (clazz == ChatFragment.class) {
            return new ChatFragment(con, this_user);
        }
        else {
            return super.instantiate(classLoader, className);
        }
    }
}
