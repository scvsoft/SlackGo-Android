package com.scv.slackgo.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.scv.slackgo.models.Channel;
import com.scv.slackgo.models.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kado on 10/17/16.
 */

public class Preferences {

    private SharedPreferences sharedPreferences;

    public Preferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }


    public void removeDataFromSharedPreferences(String keyToRemove) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(keyToRemove);
        editor.commit();
    }

    public ArrayList<Location> getLocationsList() {
        String locationsJSON = sharedPreferences.getString(Constants.SHARED_PREFERENCES_LOCATIONS, null);
        if (locationsJSON != null) {
            return GsonUtils.getListFromJson(locationsJSON, Location[].class);
        }
        return new ArrayList<Location>();
    }

    public ArrayList<Channel> getChannelsList() {
        String channelsJSON = sharedPreferences.getString(Constants.SHARED_PREFERENCES_CHANNELS, null);
        if (channelsJSON != null) {
            return GsonUtils.getListFromJson(channelsJSON, Channel[].class);
        }
        return new ArrayList<Channel>();
    }

    public void addLocationsListToSharedPreferences(List<Location> locations) {
        String locationsJSON = GsonUtils.getJsonFromObject(locations);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREFERENCES_LOCATIONS, locationsJSON);
        editor.commit();
    }

    public void addChannelsToSharedPreferences(List<Channel> channels) {
        String channelsJSON = GsonUtils.getJsonFromObject(channels);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREFERENCES_CHANNELS, channelsJSON);
        editor.commit();
    }

    public void deleteLocationFromList(Location location, List<Location> locationsList) {
        locationsList.remove(location);
        removeDataFromSharedPreferences(Constants.SHARED_PREFERENCES_LOCATIONS);
        if (locationsList.size() > 0) {
            addLocationsListToSharedPreferences(locationsList);
        }
    }
}
