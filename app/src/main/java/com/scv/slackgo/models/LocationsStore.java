package com.scv.slackgo.models;

import android.content.Context;

import com.scv.slackgo.R;
import com.scv.slackgo.SlackGoApplication;
import com.scv.slackgo.exceptions.InvalidLocationException;
import com.scv.slackgo.helpers.Constants;
import com.scv.slackgo.helpers.Preferences;

import java.util.List;

/**
 * Created by kado on 11/1/16.
 */

public class LocationsStore {
    private Context context;
    private Preferences preferences;
    private static LocationsStore instance;

    public LocationsStore() {
        this.context = SlackGoApplication.get();
        this.preferences = new Preferences(context);
    }

    public static LocationsStore getInstance() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized LocationsStore getSync() {
        if (instance == null) {
            instance = new LocationsStore();
        }
        return instance;
    }

    // Everything related to Locations
    public List<Location> getList() {
        return preferences.getLocationsList();
    }

    public Location getLocation(String id) {
        List<Location> locations = getList();
        for (Location location : locations) {
            if (location.getName().equals(id)) {
                return location;
            }
        }
        return new Location(context);
    }

    public void updateLocations(List<Location> locations) {
        preferences.removeDataFromSharedPreferences(Constants.SHARED_PREFERENCES_LOCATIONS);
        preferences.addLocationsListToSharedPreferences(locations);
    }

    private void addLocation(Location location) {
        List<Location> locations = getList();
        locations.add(location);
        updateLocations(locations);
    }

    public void deleteLocation(Location location) {
        preferences.deleteLocationFromList(location, getList());
    }

    public Boolean isLocationsListEmpty() {
        List<Location> locations = getList();
        return ((locations == null) || (locations.size() == 0));
    }

    //Everything related to channels
    public List<Channel> getChannelsList() {
        return preferences.getChannelsList();
    }

    public void saveLocation(Location location, boolean checkChannels) throws InvalidLocationException {
        if (location.getName().isEmpty()) {
            throw new InvalidLocationException(context.getString(R.string.empty_location_name));
        }
        if (checkChannels) {
            if (location.getChannels().size() == 0) {
                throw new InvalidLocationException(context.getString(R.string.no_channels_added));
            }
        }
        if (getList().contains(location)) {
            throw new InvalidLocationException(context.getString(R.string.invalid_location_name));
        }
        addLocation(location);
    }

}
