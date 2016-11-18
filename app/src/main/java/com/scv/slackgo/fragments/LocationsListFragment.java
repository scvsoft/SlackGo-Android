package com.scv.slackgo.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.scv.slackgo.R;
import com.scv.slackgo.activities.LocationDetailsActivity;
import com.scv.slackgo.adapters.AdapterLocation;
import com.scv.slackgo.customs.CustomViewPager;
import com.scv.slackgo.helpers.Constants;
import com.scv.slackgo.helpers.GsonUtils;
import com.scv.slackgo.models.Location;
import com.scv.slackgo.models.LocationsStore;

import java.util.List;

/**
 * Created by scvsoft on 11/9/16.
 */

public class LocationsListFragment extends Fragment {

    SwipeMenuListView listView;
    LocationsStore locationsStore;
    List<Location> locationsList;

    @Override
    public void onResume() {
        super.onResume();
        CustomViewPager vp = (CustomViewPager) getActivity().findViewById(R.id.locations_view_pager);
        vp.setPagingEnabled(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (SwipeMenuListView) view.findViewById(R.id.list);
        locationsStore = LocationsStore.getInstance();
        locationsList = locationsStore.getList();
        setListView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_locations_list, container, false);
    }

    public static LocationsListFragment newInstance() {
        return new LocationsListFragment();
    }

    private void setListView() {

        if (locationsList.size() > 0) {

            //final AdapterLocation adapterLocation = new AdapterLocation(getActivity(), 0, locationsList);
            final AdapterLocation adapterLocation = new AdapterLocation(getActivity(), locationsList);
            listView.setAdapter(adapterLocation);
            // step 1. create a MenuCreator
            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(
                            getActivity());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                            0x3F, 0x25)));
                    // set item width
                    deleteItem.setWidth(dp2px(90));
                    // set a icon
                    deleteItem.setIcon(R.drawable.ic_delete);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                }
            };
            // set creator
            listView.setMenuCreator(creator);


            listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    Location location = locationsList.get(position);
                    switch (index) {
                        case 0:
                            locationsList.remove(location);
                            locationsStore.deleteLocation(location);
                            adapterLocation.notifyDataSetChanged();
                            break;
                    }
                    return false;
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Location locationClicked = locationsList.get(position);
                    String locationsListJSON = GsonUtils.getJsonFromObject(locationsList);

                    Intent locationIntent = new Intent(getActivity(), LocationDetailsActivity.class);
                    locationIntent.putExtra(Constants.INTENT_LOCATION_CLICKED, locationClicked.getName());
                    locationIntent.putExtra(Constants.INTENT_LOCATION_LIST, locationsListJSON);
                    startActivity(locationIntent);
                    getActivity().finish();
                }
            });

        } else {
            listView.setAdapter(null);

        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
