package com.scv.slackgo.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.scv.slackgo.R;
import com.scv.slackgo.helpers.Constants;
import com.scv.slackgo.helpers.GsonUtils;
import com.scv.slackgo.helpers.Preferences;
import com.scv.slackgo.models.Location;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Transformer;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ayelen@scvsoft.com.
 */
public class LocationsListActivity extends MapActivity {

    ListView listView;
    List<Location> locationsList = new ArrayList<Location>();
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().destroyLoader(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationsList = Preferences.getLocationsList(this);
        setListView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setListView() {
        listView = (ListView) findViewById(R.id.list);

        listView.setAdapter(getAdapter());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Location locationClicked = locationsList.get(position);
                String locationJSON = GsonUtils.getJsonFromObject(locationClicked);
                String locationsListJSON = GsonUtils.getJsonFromObject(locationsList);

                Intent locationIntent = new Intent(getApplicationContext(), LocationActivity.class);
                locationIntent.putExtra(Constants.INTENT_LOCATION_CLICKED, locationJSON);
                locationIntent.putExtra(Constants.INTENT_LOCATION_LIST, locationsListJSON);
                startActivity(locationIntent);
            }
        });
    }

    private ArrayAdapter<String> getAdapter() {
        ArrayList<String> locations = setupLocations();
        return new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, locations);
    }

    private ArrayList<String> setupLocations() {
        ArrayList<String> locationListName = null;
        if (!Preferences.isLocationsListEmpty(this)) {
            locationListName = new ArrayList<>(CollectionUtils.collect(locationsList, new Transformer<Location, String>() {
                @Override
                public String transform(Location location) {
                    return location.getName();
                }
            }));
        }
        return locationListName;
    }

    public void addNewRegion(View view) {
        Intent locationIntent = new Intent(getApplicationContext(), LocationActivity.class);
        if (locationsList != null) {
            String locationsListJSON = GsonUtils.getJsonFromObject(locationsList);
            locationIntent.putExtra(Constants.INTENT_LOCATION_LIST, locationsListJSON);
        }
        startActivity(locationIntent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_locations_list;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        super.onMapReady(googleMap);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setCompassEnabled(true);

        IterableUtils.forEach(locationsList, new Closure<Location>() {
            @Override
            public void execute(Location loc) {
                setMarker(loc);
            }
        });

        if (locationsList == null) {
            //TODO change SCV to Default location anywhere.
            this.setMarker(Location.getSCVLocation());
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (String permisson : permissions) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
            break;
        }
    }

}
