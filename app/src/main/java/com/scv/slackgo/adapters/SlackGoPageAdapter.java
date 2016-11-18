package com.scv.slackgo.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.scv.slackgo.fragments.LocationsListFragment;
import com.scv.slackgo.fragments.LocationsListMapFragment;

/**
 * Created by scvsoft on 11/17/16.
 */

public class SlackGoPageAdapter extends FragmentPagerAdapter {

    public SlackGoPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int pos) {
        switch (pos) {
            case 0:
                return LocationsListMapFragment.newInstance();
            case 1:
                return LocationsListFragment.newInstance();
            default:
                return LocationsListMapFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
