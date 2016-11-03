package com.scv.slackgo.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by kado on 10/11/16.
 */

public class LocationActivity extends MapActivity implements Observer {

    protected static final String TAG = "LocationActivity";


    private SeekBar locationSeekBar;
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
    SlackApiService slackService;
    GeofenceService geofenceService;
    LocationsStore locationsStore;
    PlaceAutocompleteFragment autocompleteFragment;
    ListView channelsListView;

    protected List<Geofence> mGeofenceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeVariables();

        checkUserPermissions();

        setDetailValues();

        addLocationBarListener();

        addCRUDButtonsListener();

        addSearchListener();

        addLocationNameEnterListener();

        String slackCode = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE).getString(Constants.SLACK_TOKEN, null);

        if (slackCode == null) {
            String code = getIntent().getData().getQueryParameters("code").get(0);
            slackService.getSlackToken(String.format(getString(R.string.slack_token_url),
                    getString(R.string.client_id), getString(R.string.client_secret), code, getString(R.string.redirect_oauth)));
        } else {
            slackService.getAvailableChannels();
        }

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

        this.googleMap.clear();
        if (locationClicked != null) {
            this.setMarker(locationClicked);
        } else {
            Location officeLocation = Location.getSCVLocation();
            this.setMarker(officeLocation);
        }


        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();
                editLocation.setLatitude(latLng.latitude);
                editLocation.setLongitude(latLng.longitude);
                setMarker(editLocation);
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data != null) {
            List<Channel> dataAsstrings = new ArrayList<Channel>(((ArrayList<Object>) data).size());
            for (Object object : (ArrayList<Object>) data) {
                dataAsstrings.add((Channel) object);
            }
            String[] dataArr = new String[dataAsstrings.size()];
            locationsStore.addChannels(dataAsstrings);
            channelsList = dataAsstrings;

        } else {
            slackService.getAvailableChannels();
        }
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
        } else {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            android.location.Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                editLocation.setLatitude(Constants.DEFAULT_LAT);
                editLocation.setLongitude(Constants.DEFAULT_LONG);
            } else {
                editLocation.setLongitude(location.getLongitude());
                editLocation.setLatitude(location.getLatitude());
            }
        }
    }

    private void initializeVariables() {

        editLocation = new Location(this);
        slackService = new SlackApiService(this);
        locationsStore = LocationsStore.getInstance();

        channelsListView = (ListView) findViewById(R.id.channel_list);

        Intent myIntent = getIntent();
        String locationJSON = myIntent.getStringExtra(Constants.INTENT_LOCATION_CLICKED);
        String locationsListJSON = myIntent.getStringExtra(Constants.INTENT_LOCATION_LIST);

        locationClicked = GsonUtils.getObjectFromJson(locationJSON, Location.class);

        locationsList = GsonUtils.getListFromJson(locationsListJSON, Location[].class);
        locationsList = locationsList == null ? new ArrayList<Location>() : locationsList;

        if (channelsList == null) {
            channelsList = locationsStore.getChannelsList();
        }

        //Get resources
        locationSeekBar = (SeekBar) findViewById(R.id.location_radius_seek_bar);
        locationRadiusValue = (TextView) findViewById(R.id.location_radius_value);
        locationName = (EditText) findViewById(R.id.location_name);
        channelsTextView = (TextView) findViewById(R.id.selected_channels);
        saveLocationButton = (Button) findViewById(R.id.save_location_button);
        delLocationButton = (Button) findViewById(R.id.del_location_button);
        addChannelsButton = (Button) findViewById(R.id.add_channels);
        locationSeekBar.setMax(100);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        mGeofenceList = GeofenceUtils.getGeofencesListFromLocations(locationsStore, locationsList);
    }


    private void addSearchListener() {
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                editLocation = new Location(place.getLatLng());
                setMarker(editLocation);
            }

            @Override
            public void onError(Status status) {
                ErrorUtils.showErrorAlert(LocationActivity.this);
            }
        });
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

                List<Channel> channelsClicked = ChannelListHelper.getChannelsFromTextView(channelsTextView.getText().toString(), channelsList);
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

    public void setSelectedChannels(List<Channel> selectedChannels) {
        this.selectedChannels = selectedChannels;
    }


    private void addLocationBarListener() {
        locationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress >= 0 && progress <= locationSeekBar.getMax()) {
                        String progressString = String.valueOf(progress * 10);
                        locationRadiusValue.setText(progressString);
                        seekBar.setProgress(progress);
                        circle.setRadius(progress * 10);
                    }
                }
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

    private void setDetailValues() {

        if (locationClicked != null) {
            locationName.setText(locationClicked.getName());
            locationSeekBar.setProgress(new BigDecimal(locationClicked.getRadius() / 10).intValue());
            locationRadiusValue.setText(String.valueOf(locationClicked.getRadius() * 10));

            List<String> channelsSelected = new ArrayList<String>();
            for (Channel channel : locationClicked.getChannels()) {
                channelsSelected.add(channel.getName());
            }
            channelsTextView.setText(TextUtils.join(", ", channelsSelected));
            delLocationButton.setVisibility(View.VISIBLE);
        } else {
            float defaultProgress = Constants.DEFAULT_RADIUS_METERS / 10;
            locationSeekBar.setProgress(Math.round(defaultProgress));
            locationRadiusValue.setText(String.valueOf(Constants.DEFAULT_RADIUS_METERS));
            delLocationButton.setVisibility(View.GONE);
        }
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
