package com.scv.slackgo.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.scv.slackgo.R;
import com.scv.slackgo.helpers.ChannelListHelper;
import com.scv.slackgo.helpers.Constants;
import com.scv.slackgo.helpers.ErrorUtils;
import com.scv.slackgo.helpers.GeofenceUtils;
import com.scv.slackgo.helpers.GsonUtils;
import com.scv.slackgo.models.Channel;
import com.scv.slackgo.models.Location;
import com.scv.slackgo.models.LocationsStore;
import com.scv.slackgo.services.GeofenceService;
import com.scv.slackgo.services.SlackApiService;
import com.scv.slackgo.services.listeners.ChannelsListListener;
import com.scv.slackgo.services.listeners.SlackTokenListener;
import com.scv.slackgo.services.listeners.TeamNameListener;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kado on 10/11/16.
 */

public class LocationActivity extends MapActivity {

    protected static final String TAG = "LocationActivity";
    protected List<Geofence> mGeofenceList;
    SlackApiService slackService;
    GeofenceService geofenceService;
    LocationsStore locationsStore;
    PlaceAutocompleteFragment autocompleteFragment;
    ListView channelsListView;
    private TextView locationRadiusValue;
    private TextView channelsTextView;
    private EditText locationName;
    private Button saveLocationButton;
    private Button delLocationButton;
    private Button addChannelsButton;
    private Location locationClicked;
    private Location editLocation;
    private List<Location> locationsList;
    private List<Channel> channelsList;
    private List<Channel> selectedChannels;
    private String toastMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeVariables();

        getLayoutResources();

        checkUserPermissions();

        setDetailValues();

        addCRUDButtonsListener();

        addSearchListener();

        addLocationNameEnterListener();
    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_location;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        super.onMapReady(googleMap);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        clearMap();
        this.setMarker(editLocation);

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                clearMap();
                editLocation.setLatitude(latLng.latitude);
                editLocation.setLongitude(latLng.longitude);
                setMarker(editLocation);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (String permisson : permissions) {
            switch (permisson) {
                case Manifest.permission.ACCESS_FINE_LOCATION: {
                    if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                Constants.RC_ASK_PERMISSIONS);
                    }
                    break;
                }
                case Manifest.permission.ACCESS_COARSE_LOCATION: {
                    if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                Constants.RC_ASK_PERMISSIONS);
                    }
                    break;
                }

            }
        }
    }

    private void initializeVariables() {

        slackService = new SlackApiService(this);
        locationsStore = LocationsStore.getInstance();

        channelsListView = (ListView) findViewById(R.id.channel_list);

        Intent myIntent = getIntent();
        String locationJSON = myIntent.getStringExtra(Constants.INTENT_LOCATION_CLICKED);
        String locationsListJSON = myIntent.getStringExtra(Constants.INTENT_LOCATION_LIST);

        locationClicked = GsonUtils.getObjectFromJson(locationJSON, Location.class);
        editLocation = (locationClicked == null) ? new Location(this) : locationClicked;

        locationsList = GsonUtils.getListFromJson(locationsListJSON, Location[].class);
        locationsList = locationsList == null ? new ArrayList<Location>() : locationsList;
        channelsList = new ArrayList<Channel>();
        channelsList = locationsStore.getChannelsList();

        mGeofenceList = GeofenceUtils.getGeofencesListFromLocations(locationsStore, locationsList);

        slackServiceActions();
    }

    private void getLayoutResources(){
        locationRadiusValue = (TextView) findViewById(R.id.location_radius_value);
        locationName = (EditText) findViewById(R.id.location_name);
        channelsTextView = (TextView) findViewById(R.id.selected_channels);
        saveLocationButton = (Button) findViewById(R.id.save_location_button);
        delLocationButton = (Button) findViewById(R.id.del_location_button);
        addChannelsButton = (Button) findViewById(R.id.add_channels);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
    }

    private void checkUserPermissions() {

        List<String> permission = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            permission.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!permission.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permission.toArray(new String[0]),
                    Constants.RC_ASK_PERMISSIONS);
        }
    }

    private void setDetailValues() {

        if (locationClicked != null) {
            locationName.setText(locationClicked.getName());
            locationRadiusValue.setText(String.valueOf(locationClicked.getRadius() * 10));

            selectedChannels = locationClicked.getChannels();

            List<String> channelsSelected = Lists.newArrayList(CollectionUtils.collect(selectedChannels, new Transformer<Channel, String>() {
                @Override
                public String transform(Channel channel) {
                    return channel.getName();
                }
            }));
            channelsTextView.setText(TextUtils.join(", ", channelsSelected));
            delLocationButton.setVisibility(View.VISIBLE);
        } else {
            locationRadiusValue.setText(String.valueOf(Constants.DEFAULT_RADIUS_METERS));
            delLocationButton.setVisibility(View.GONE);
        }
    }

    private void addCRUDButtonsListener() {
        delLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationsStore.deleteLocation(locationClicked);
                transitionToLocationActivity();
            }
        });

        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLocation.setName(locationName.getText().toString());

                List<Channel> channelsClicked = getSelectedChannels();
                editLocation.setChannels(channelsClicked);

                if (isValidLocation(editLocation)) {
                    saveLocation();
                } else {
                    ErrorUtils.toastError(LocationActivity.this, toastMsg, Toast.LENGTH_SHORT);
                }
            }
        });
        addChannelsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelListHelper.buildList(LocationActivity.this, channelsList, channelsListView);
            }

        });
    }

    private void addSearchListener() {
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                editLocation.setLatitude(place.getLatLng().latitude);
                editLocation.setLongitude(place.getLatLng().longitude);
                setMarker(editLocation);
            }

            @Override
            public void onError(Status status) {

                ErrorUtils.showErrorAlert(LocationActivity.this);
            }
        });
    }

    private void addLocationNameEnterListener() {
        locationName.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });
    }

    //Get slack info for channels and user team
    private void slackServiceActions() {
        String slackCode = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE).getString(Constants.SLACK_TOKEN, null);
        if (slackCode == null) {
            String code = getIntent().getData().getQueryParameters("code").get(0);
            String slackUrl = String.format(getString(R.string.slack_token_url),
                    getString(R.string.client_id), getString(R.string.client_secret), code, getString(R.string.redirect_oauth));

            slackService.getSlackToken(slackUrl, new SlackTokenListener() {
                @Override
                public void onResponse() {
                    setChannelsListFromService();
                    setLocationToTeam();
                }

                @Override
                public void onError(Context context) {
                    ErrorUtils.showErrorAlert(context);
                }
            });
        } else {
            setChannelsListFromService();
            if (locationClicked == null) {
                setLocationToTeam();
            }

        }
    }

    private void setChannelsListFromService() {
        slackService.getAvailableChannels(new ChannelsListListener() {
            @Override
            public void onResponse(List<Channel> channels) {
                channelsList = channels;
            }

            @Override
            public void onError(Context context) {
                ErrorUtils.showErrorAlert(context);
            }
        });
    }

    private void setLocationToTeam() {
        slackService.getTeam(new TeamNameListener() {
            @Override
            public void onResponse(String team) {
                if (team.equals(Constants.SLACK_SCV_TEAM)) {
                    editLocation = Location.getSCVLocation();
                    clearMap();
                    setMarker(editLocation);
                }
            }

            @Override
            public void onError(Context context) {
                ErrorUtils.showErrorAlert(context);
            }
        });
    }

    public List<Channel> getSelectedChannels() {
        return this.selectedChannels;
    }

    public void setSelectedChannels(List<Channel> selectedChannels) {
        this.selectedChannels = selectedChannels;
    }

    private void transitionToLocationActivity() {
        Intent locationsIntent = new Intent(getApplicationContext(), LocationsListActivity.class);
        startActivity(locationsIntent);
        finish();
    }


    private void saveLocation() {
        if (locationClicked == null) {
            locationsStore.addLocation(editLocation);
            updateGeofencesList(editLocation.getName());
            geofenceService = new GeofenceService(LocationActivity.this, mGeofenceList);
        } else {
            editLocation();
        }
        transitionToLocationActivity();
    }

    private void editLocation() {
        locationsList.remove(locationClicked);
        locationsList.add(editLocation);
        locationsStore.updateLocations(locationsList);
        updateGeofencesList(locationClicked.getName());
        geofenceService = new GeofenceService(LocationActivity.this, mGeofenceList);
    }


    private boolean isValidLocation(Location location) {
        boolean isValid = true;
        if (location.getName().isEmpty() || (location.getChannels().size() == 0)) {
            isValid = false;
            toastMsg = location.getName().isEmpty() ? getString(R.string.empty_location_name) : getString(R.string.no_channels_added);
        } else {
            if (locationsList != null) {
                for (Location locationInList : locationsList) {
                    if (location.getName().equals(locationInList.getName()) && !location.getName().equals(locationClicked.getName())) {
                        isValid = false;
                        toastMsg = getString(R.string.invalid_location_name);
                    }
                }
            }
        }
        return isValid;
    }

    private void updateGeofencesList(final String geofenceId) {
        Geofence newLocationGeofence = GeofenceUtils.geofenceBuilder(editLocation);
        CollectionUtils.filter(mGeofenceList, new Predicate<Geofence>() {
            @Override
            public boolean evaluate(Geofence object) {
                return !object.getRequestId().equals(geofenceId);
            }
        });
        mGeofenceList.add(newLocationGeofence);
    }
}