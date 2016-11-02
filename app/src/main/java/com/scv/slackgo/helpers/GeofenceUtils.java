package com.scv.slackgo.helpers;

import com.google.android.gms.location.Geofence;
import com.scv.slackgo.models.Channel;
import com.scv.slackgo.models.Location;
import com.scv.slackgo.models.LocationsStore;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kado on 10/31/16.
 */

public class GeofenceUtils {

    public static List<Geofence> getGeofencesListFromLocations(LocationsStore locationsStore, List<Location> locations) {
        if ((locations == null) || (locations.size() == 0)) {
            locations = locationsStore.getList();
        }
        return getGeofencesList(locations);
    }

    public static Map<String, List<Channel>> getChannelsForGeofences(LocationsStore locationsStore, List<Geofence> geofences) {
        Map<String, List<Channel>> channelsForLocation = new HashedMap<>();
        List<Location> locations = locationsStore.getList();
        Map<String, List<Channel>> allChannelsForLocationMap = ChannelListHelper.getChannelsListForLocations(locations);

        for (Location loc : locations) {
            for (Geofence geofence : geofences) {
                if (loc.getName().equals(geofence.getRequestId())) {
                    channelsForLocation.put(loc.getName(), allChannelsForLocationMap.get(loc.getName()));
                }

            }
        }
        return channelsForLocation;
    }

    public static Geofence geofenceBuilder(Location loc) {
        return new Geofence.Builder()
                .setRequestId(loc.getName())
                .setCircularRegion(loc.getLatitude(), loc.getLongitude(), loc.getRadius())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private static List<Geofence> getGeofencesList(List<Location> locations) {
        return new ArrayList<>(CollectionUtils.collect(locations, new Transformer<Location, Geofence>() {
            @Override
            public Geofence transform(Location loc) {
                return geofenceBuilder(loc);
            }
        }));
    }
}
