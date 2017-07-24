package com.scv.slackgo.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.scv.slackgo.R;
import com.scv.slackgo.exceptions.GeocoderException;
import com.scv.slackgo.exceptions.InvalidLocationException;
import com.scv.slackgo.helpers.Constants;
import com.scv.slackgo.helpers.ErrorUtils;
import com.scv.slackgo.helpers.GeofenceUtils;
import com.scv.slackgo.helpers.MapHelper;
import com.scv.slackgo.models.Location;
import com.scv.slackgo.models.LocationsStore;
import com.scv.slackgo.services.SlackApiService;
import com.scv.slackgo.services.listeners.SlackTokenListener;
import com.scv.slackgo.services.listeners.TeamNameListener;

import java.util.ArrayList;
import java.util.List;

import static com.scv.slackgo.R.id.channel_map;

/**
 * Created by scvsoft on 11/10/16.
 */

public class LocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    protected static final String TAG = "LocationMapActivity";

    TextView firstLocation;
    TextView mapLocationAddress;
    EditText locationName;

    GoogleMap googleMap;
    LocationsStore locationsStore;
    SlackApiService slackService;
    Location editLocation;
    private List<Location> locationsList;
    protected List<Geofence> mGeofenceList;
    static int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private String toastMsg;
    boolean isEditing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkUserPermissions();
        setContentView(R.layout.activity_location_map);
        getLayoutResources();
        initializeVariables();
        initializeLayoutValues();
        initMapFragment();
        slackServiceActions();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = this.googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            LocationMapActivity.this, R.raw.map_style));
            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

        MapHelper.setMarker(editLocation, this.googleMap);

        try {
            mapLocationAddress.setText(MapHelper.getAddressFromLocation(this, editLocation));
        } catch (GeocoderException e) {
            ErrorUtils.toastError(this, e.getMessage(), Toast.LENGTH_SHORT);
            mapLocationAddress.setHint(getString(R.string.location));
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();
                editLocation.setLatitude(latLng.latitude);
                editLocation.setLongitude(latLng.longitude);
                MapHelper.setMarker(editLocation, googleMap);
                try {
                    mapLocationAddress.setText(MapHelper.getAddressFromLocation(LocationMapActivity.this, editLocation));
                } catch (GeocoderException e) {
                    ErrorUtils.toastError(LocationMapActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    mapLocationAddress.setHint(getString(R.string.location));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                googleMap.clear();
                Place place = PlaceAutocomplete.getPlace(this, data);
                editLocation.setLatitude(place.getLatLng().latitude);
                editLocation.setLongitude(place.getLatLng().longitude);
                MapHelper.setMarker(editLocation, this.googleMap);
                try {
                    mapLocationAddress.setText(MapHelper.getAddressFromLocation(this, editLocation));
                } catch (GeocoderException e) {
                    ErrorUtils.toastError(this, e.getMessage(), Toast.LENGTH_SHORT);
                    mapLocationAddress.setText(getString(R.string.location));

                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent locationIntent = new Intent(getApplicationContext(), LocationsListActivity.class);
        startActivity(locationIntent);
        finish();
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

    private void getLayoutResources() {
        firstLocation = (TextView) findViewById(R.id.first_location);
        mapLocationAddress = (TextView) findViewById(R.id.map_location_address);
        locationName = (EditText) findViewById(R.id.location_name);
    }

    private void initializeVariables() {


        slackService = new SlackApiService(this);
        locationsStore = LocationsStore.getInstance();


        Intent myIntent = getIntent();
        String locationId = myIntent.getStringExtra(Constants.INTENT_LOCATION_CLICKED);
        isEditing = (locationId != null);

        locationsList = locationsStore.getList();
        locationsList = locationsList == null ? new ArrayList<Location>() : locationsList;

        editLocation = locationsStore.getLocation(locationId);

        mGeofenceList = GeofenceUtils.getGeofencesListFromLocations(locationsStore, locationsList);
        locationName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addLocationDetails(v);
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void initializeLayoutValues() {
        String strForLocation = (isEditing) ? editLocation.getName() : "";
        locationName.setText(strForLocation);
    }

    //TODO repeated method. See if is necessary MapActivity, now can't be re-used because of fragment.
    public void initMapFragment() {
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(channel_map, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);
    }

    public void searchLocationClicked(View view) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void addLocationDetails(View view) {
        try {
            if (isEditing) {
                locationsList.remove(editLocation);
                locationsStore.updateLocations(locationsList);
            }
            updateEditLocationName();
            locationsStore.saveLocation(editLocation, false);
            Intent locationIntent = new Intent(getApplicationContext(), LocationDetailsActivity.class);
            locationIntent.putExtra(Constants.INTENT_LOCATION_CLICKED, editLocation.getName());
            startActivity(locationIntent);
            finish();
        } catch (InvalidLocationException e) {
            ErrorUtils.toastError(this, e.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    private void updateEditLocationName() {
        editLocation.setName(locationName.getText().toString());
    }

    private void slackServiceActions() {
        String slackCode = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE).getString(Constants.SLACK_TOKEN, null);
        if (slackCode == null) {
            String code = getIntent().getData().getQueryParameters("code").get(0);
            String slackUrl = String.format(getString(R.string.slack_token_url),
                    getString(R.string.client_id), getString(R.string.client_secret), code, getString(R.string.redirect_oauth));

            slackService.getSlackToken(slackUrl, new SlackTokenListener() {
                @Override
                public void onResponse() {
                    setLocationToTeam();
                }

                @Override
                public void onError(Context context) {
                    ErrorUtils.showErrorAlert(context);
                }
            });
        } else {
            setLocationToTeam();
        }
    }

    private void setLocationToTeam() {
        slackService.getTeam(new TeamNameListener() {
            @Override
            public void onResponse(String team) {
                if (team.equals(Constants.SLACK_SCV_TEAM)) {
                    try {
                        LatLng scvLatLng = MapHelper.getLocationFromAddress(LocationMapActivity.this, Constants.SCV_ADDRESS);
                        editLocation.setLongitude(scvLatLng.longitude);
                        editLocation.setLatitude(scvLatLng.latitude);
                        MapHelper.setMarker(editLocation, googleMap);
                        //mapLocationAddress.setText(MapHelper.getAddressFromLocation(this, editLocation));
                        mapLocationAddress.setText(Constants.SCV_ADDRESS);
                    } catch (GeocoderException e) {
                        ErrorUtils.toastError(LocationMapActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                        mapLocationAddress.setText(getString(R.string.location));

                    }
                }
            }

            @Override
            public void onError(Context context) {
                ErrorUtils.showErrorAlert(context);
            }
        });
    }

}
