package com.scv.slackgo.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.scv.slackgo.R;
import com.scv.slackgo.activities.LocationDetailsActivity;
import com.scv.slackgo.helpers.Constants;
import com.scv.slackgo.helpers.GsonUtils;
import com.scv.slackgo.helpers.MapHelper;
import com.scv.slackgo.models.Location;
import com.scv.slackgo.models.LocationsStore;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;

import java.util.ArrayList;
import java.util.List;

import static com.scv.slackgo.R.id.channel_map;

/**
 * Created by scvsoft on 11/9/16.
 */

public class LocationsListMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    GoogleMap googleMap;
    LocationsStore locationsStore;
    List<Location> locationsList;

    @Override
    public void onResume() {
        super.onResume();
        locationsStore = LocationsStore.getInstance();
        locationsList = locationsStore.getList();
        initMapFragment();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter("centerMap"));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationsStore = LocationsStore.getInstance();
        locationsList = locationsStore.getList();
        initMapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_locations_list_map, container, false);
    }

    public static LocationsListMapFragment newInstance() {
        return new LocationsListMapFragment();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.googleMap.setOnMarkerClickListener(this);
        this.googleMap.setMaxZoomPreference(16);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = this.googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.map_style));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

        setMapValues();
    }

    public void setMapValues() {
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        locationsList = locationsStore.getList();

        if ((locationsList == null) || (locationsList.size() == 0)) {
            MapHelper.setMarker(new Location(getContext()), this.googleMap);
        } else {
            final List<LatLng> locationsLatLngList = new ArrayList<LatLng>();
            IterableUtils.forEach(locationsList, new Closure<Location>() {
                @Override
                public void execute(Location loc) {
                    MapHelper.setMarker(loc, googleMap);
                    LatLng locLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                    locationsLatLngList.add(locLatLng);
                }
            });
            MapHelper.centerLocation(getContext(), googleMap);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    //TODO repeated method. See if is necessary MapActivity, now can't be re-used because of fragment.
    public void initMapFragment() {
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(channel_map, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);
    }

    public void gotoLocation(String location) {
        String locationsListJSON = GsonUtils.getJsonFromObject(locationsList);
        Intent locationIntent = new Intent(getActivity(), LocationDetailsActivity.class);
        locationIntent.putExtra(Constants.INTENT_LOCATION_CLICKED, location);
        locationIntent.putExtra(Constants.INTENT_LOCATION_LIST, locationsListJSON);
        startActivity(locationIntent);
        getActivity().finish();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        this.gotoLocation(marker.getTitle());
        return true;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MapHelper.centerLocation(context, googleMap);
        }
    };

}
