package com.scv.slackgo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;

import com.scv.slackgo.R;
import com.scv.slackgo.adapters.SlackGoPageAdapter;
import com.scv.slackgo.customs.CustomViewPager;
import com.scv.slackgo.models.Location;
import com.scv.slackgo.models.LocationsStore;

import java.util.List;


/**
 * Created by ayelen@scvsoft.com.
 */

public class LocationsListActivity extends FragmentActivity {

    CustomViewPager pager;
    LocationsStore locationsStore;
    List<Location> locationsList;
    ImageButton buttonMap;
    ImageButton buttonList;

    @Override
    protected void onResume() {
        super.onResume();
        locationsStore = LocationsStore.getInstance();
        locationsList = locationsStore.getList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_list);

        pager = (CustomViewPager) findViewById(R.id.locations_view_pager);
        final SlackGoPageAdapter adapter = new SlackGoPageAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setPagingEnabled(true);

        buttonList = (ImageButton) findViewById(R.id.goto_locations_list);
        buttonMap = (ImageButton) findViewById(R.id.goto_locations_map);

        buttonMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pager.setCurrentItem(0);
                buttonMap.setImageResource(R.drawable.ic_map_selected);
                buttonList.setImageResource(R.drawable.ic_list);
            }
        });

        buttonList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pager.setCurrentItem(1);
                adapter.notifyDataSetChanged();
                buttonMap.setImageResource(R.drawable.ic_map);
                buttonList.setImageResource(R.drawable.ic_list_selected);
            }
        });

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if ((position==0) && (locationsList.size() != locationsStore.getList().size())){
                    //TODO check if can use setMapValues
                    getSupportFragmentManager().getFragments().get(position).onResume();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {}

    public void addNewRegion(View view) {
        Intent locationIntent = new Intent(getApplicationContext(), LocationMapActivity.class);
        startActivity(locationIntent);
        finish();
    }
}