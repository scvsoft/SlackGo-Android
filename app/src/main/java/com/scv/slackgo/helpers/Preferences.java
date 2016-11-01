package com.scv.slackgo.helpers;

import android.app.Activity;
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

    public static void removeDataFromSharedPreferences(Activity activity, String keyToRemove) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(keyToRemove);
        editor.commit();
    }

    public static ArrayList<Location> getLocationsList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String locationsJSON = sharedPreferences.getString(Constants.SHARED_PREFERENCES_LOCATIONS, null);
        if (locationsJSON != null) {
            return GsonUtils.getListFromJson(locationsJSON, Location[].class);
        }

        return new ArrayList<Location>();
    }

    public static ArrayList<Channel> getChannelsList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String channelsJSON = sharedPreferences.getString(Constants.SHARED_PREFERENCES_CHANNELS, null);
        if (channelsJSON != null) {
            return GsonUtils.getListFromJson(channelsJSON, Channel[].class);
        }

        return new ArrayList<Channel>();
    }

    public static Boolean isLocationsListEmpty(Activity activity) {
        ArrayList<Location> locations = getLocationsList(activity);
        return ((locations == null) || (locations.size() == 0));
    }

    public static void addLocationsListToSharedPreferences(Activity activity, List<Location> locations) {
        String locationsJSON = GsonUtils.getJsonFromObject(locations);
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREFERENCES_LOCATIONS, locationsJSON);
        editor.commit();
    }

    public static void addChannelsToSharedPreferences(Activity activity, List<Channel> channels) {
        String channelsJSON = GsonUtils.getJsonFromObject(channels);
        SharedPreferences sharedPreferecnes = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferecnes.edit();
        editor.putString(Constants.SHARED_PREFERENCES_CHANNELS, channelsJSON);
        editor.commit();
    }

    public static void addLocationToSharedPreferences(Activity activity, Location location) {
        ArrayList<Location> listOfLocations = new ArrayList<Location>();
        if (!isLocationsListEmpty(activity)) {
            listOfLocations.addAll(getLocationsList(activity));
        }
        removeDataFromSharedPreferences(activity, Constants.SHARED_PREFERENCES_LOCATIONS);
        if (!locationWithNameExistsInList(listOfLocations, location.getName())) {
            listOfLocations.add(location);
        }
        addLocationsListToSharedPreferences(activity, listOfLocations);
    }

    public static void deleteLocationFromList(Activity activity, Location location, List<Location> locationsList) {
        locationsList.remove(location);
        removeDataFromSharedPreferences(activity, Constants.SHARED_PREFERENCES_LOCATIONS);
        if (locationsList.size() > 0) {
            addLocationsListToSharedPreferences(activity, locationsList);
        }
    }
    
    private static boolean locationWithNameExistsInList(ArrayList<Location> locations, String locationName) {
        for (Location location : locations) {
            if (location.getName().equals(locationName)) {
                return true;
            }
        }
        return false;
    }
}
