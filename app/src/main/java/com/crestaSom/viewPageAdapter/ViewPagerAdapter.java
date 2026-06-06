package com.crestaSom.viewPageAdapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 1/3/2017.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    List<Fragment> fragments=new ArrayList<>();
    List<String> tabTitles=new ArrayList<>();
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    public  void addFragments(Fragment fragment,String tabTitle){
        fragments.add(fragment);
        tabTitles.add(tabTitle);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }
}
