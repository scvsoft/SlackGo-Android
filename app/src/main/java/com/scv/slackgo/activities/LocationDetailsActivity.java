package com.scv.slackgo.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.greenfrvr.hashtagview.HashtagView;
import com.scv.slackgo.R;
import com.scv.slackgo.adapters.ChannelsListAdapter;
import com.scv.slackgo.exceptions.GeocoderException;
import com.scv.slackgo.exceptions.InvalidLocationException;
import com.scv.slackgo.helpers.Constants;
import com.scv.slackgo.helpers.ErrorUtils;
import com.scv.slackgo.helpers.GeofenceUtils;
import com.scv.slackgo.helpers.MapHelper;
import com.scv.slackgo.models.Channel;
import com.scv.slackgo.models.Location;
import com.scv.slackgo.models.LocationsStore;
import com.scv.slackgo.services.GeofenceService;
import com.scv.slackgo.services.SlackApiService;
import com.scv.slackgo.services.listeners.ChannelsListListener;
import com.scv.slackgo.services.listeners.SlackTokenListener;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kado on 10/11/16.
 */

public class LocationDetailsActivity extends AppCompatActivity {

    protected static final String TAG = "LocationDetailsActivity";

    EditText locationName;
    TextView locationAddress;
    EditText channelsSearch;
    ListView channelsListView;
    HashtagView channelsSelectedTags;
    ChannelsListAdapter channelsListAdapter;
    InputMethodManager imm;

    LocationsStore locationsStore;
    GeofenceService geofenceService;
    SlackApiService slackService;

    protected List<Geofence> mGeofenceList;
    private Location editLocation;
    private List<Location> locationsList;
    private List<Channel> channelsList;
    private List<Channel> selectedChannels;
    boolean isEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        getLayoutResources();
        initializeVariables();
        setDetailValues();
    }

    @Override
    public void onBackPressed() {
        this.saveLocation(null);
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

    private void getLayoutResources() {
        locationName = (EditText) findViewById(R.id.location_name);
        locationName.setFocusable(false);
        locationAddress = (TextView) findViewById(R.id.location_address);
        locationAddress.setFocusable(false);
        channelsListView = (ListView) findViewById(R.id.channel_list);
        channelsListView.setTextFilterEnabled(true);
        channelsSelectedTags = (HashtagView) findViewById(R.id.channels_tags);
        channelsSearch = (EditText) findViewById(R.id.channels_search);
        channelsSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                channelsListAdapter.getFilter().filter(arg0);
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override
            public void afterTextChanged(Editable arg0) {}
        });
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

        channelsList = new ArrayList<Channel>();
        channelsList = locationsStore.getChannelsList();

        mGeofenceList = GeofenceUtils.getGeofencesListFromLocations(locationsStore, locationsList);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        slackServiceActions();
    }


    private void setDetailValues() {
        if (isEditing) {
            locationName.setText(editLocation.getName());
            try {
                locationAddress.setText(MapHelper.getAddressFromLocation(this, editLocation));
            } catch (GeocoderException e) {
                ErrorUtils.toastError(this, e.getMessage(), Toast.LENGTH_SHORT);
                locationAddress.setText(getString(R.string.location));

            }
            selectedChannels = editLocation.getChannels();
        }
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
                }

                @Override
                public void onError(Context context) {
                    ErrorUtils.showErrorAlert(context);
                }
            });
        } else {
            setChannelsListFromService();
        }
    }

    private void setChannelsListFromService() {
        slackService.getAvailableChannels(new ChannelsListListener() {
            @Override
            public void onResponse(final List<Channel> channels) {
                channelsList = channels;
                channelsListAdapter = new ChannelsListAdapter(LocationDetailsActivity.this, 0, channelsList, selectedChannels);
                channelsListView.setAdapter(channelsListAdapter);

                channelsSelectedTags.setData(selectedChannels, CHANNEL);
                channelsSelectedTags.addOnTagClickListener(new HashtagView.TagsClickListener() {
                    @Override
                    public void onItemClicked(Object item) {
                        Channel channelClicked = (Channel) item;
                        selectedChannels.remove(channelClicked);
                        channelsSelectedTags.removeItem(item);
                        channelsListAdapter.setChannelSelected(channelClicked, false);
                        //channelGridAdapter.notifyDataSetChanged()
                        channelsListAdapter.notifyDataSetChanged();
                        editLocation.setChannels(selectedChannels);
                    }
                });

                channelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
                        Channel channelClicked = channelsListAdapter.getChannelFromListPosition(position);
                        if (selectedChannels.contains(channelClicked)) {
                            selectedChannels.remove(channelClicked);
                            channelsSelectedTags.removeItem(channelClicked);
                            channelsListAdapter.setChannelSelected(channelClicked, false);
                        } else {
                            selectedChannels.add(channelClicked);
                            if (selectedChannels.size() == 1) {
                                channelsSelectedTags.setData(selectedChannels, CHANNEL);
                            } else {
                                channelsSelectedTags.addItem(channelClicked);
                            }
                            channelsListAdapter.setChannelSelected(channelClicked, true);
                        }
                        channelsListAdapter.notifyDataSetChanged();
                        editLocation.setChannels(selectedChannels);
                    }

                });
            }

            @Override
            public void onError(Context context) {
                ErrorUtils.showErrorAlert(context);
            }
        });
    }

    private void transitionToLocationsListActivity() {
        Intent locationsIntent = new Intent(getApplicationContext(), LocationsListActivity.class);
        startActivity(locationsIntent);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        finish();
    }

    public void saveLocation(View view) {
        try {
            if (isEditing) {
                locationsList.remove(editLocation);
                locationsStore.updateLocations(locationsList);
            }
            updateEditLocation();
            locationsStore.saveLocation(editLocation, true);
            updateGeofencesList(editLocation.getName());
            geofenceService = new GeofenceService(LocationDetailsActivity.this, mGeofenceList);
            transitionToLocationsListActivity();
        } catch (InvalidLocationException e) {
            ErrorUtils.toastError(this, e.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    public void editMapLocation(View view) {
        saveLocation(view);
        Intent locationIntent = new Intent(getApplicationContext(), LocationMapActivity.class);
        locationIntent.putExtra(Constants.INTENT_LOCATION_CLICKED, editLocation.getName());
        startActivity(locationIntent);
        finish();
    }

    public void toggleLocationNameEdit(View view) {
        locationName.setFocusableInTouchMode(true);
        locationName.setFocusable(true);
        locationName.requestFocus();
        imm.showSoftInput(locationName, InputMethodManager.SHOW_FORCED);
    }

    private void updateEditLocation() {
        editLocation.setName(locationName.getText().toString());
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

    public static final HashtagView.DataTransform<Channel> CHANNEL = new HashtagView.DataTransform<Channel>() {
        @Override
        public CharSequence prepare(Channel item) {
            SpannableString spannableString = new SpannableString("#" + item.getName());
            return spannableString;
        }
    };
}