package com.scv.slackgo.models;

import android.content.Context;

import com.scv.slackgo.SlackGoApplication;
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
        return null;
    }

    public void updateLocations(List<Location> locations) {
        preferences.removeDataFromSharedPreferences(Constants.SHARED_PREFERENCES_LOCATIONS);
        preferences.addLocationsListToSharedPreferences(locations);
    }

    public void addLocation(Location location) {
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

    public void addChannels(List<Channel> channels) {
        preferences.addChannelsToSharedPreferences(channels);
    }


}
