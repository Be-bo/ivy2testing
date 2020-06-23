package com.ivy2testing.main;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert Fiker on 2/16/2018.
 */

public class SectionsPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments = new ArrayList<>();
    private List<String> fragmentTitles = new ArrayList<>();

    SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    void addFragment(Fragment fragment, String title){
        fragments.add(fragment);
        fragmentTitles.add(title);
        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }

    public int getPosition(String title){
        if(fragmentTitles.contains(title)) return fragmentTitles.indexOf(title);
        else return 0;
    }

    @Override
    public Fragment getItem( int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
