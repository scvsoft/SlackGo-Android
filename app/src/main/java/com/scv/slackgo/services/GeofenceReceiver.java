package com.scv.slackgo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.scv.slackgo.helpers.GeofenceUtils;

import java.util.List;


/**
 * Created by kado on 10/24/16.
 */

public class GeofenceReceiver extends BroadcastReceiver {

    protected static final String TAG = "GeofenceReceiver";
    protected GeofenceService geofenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "GeofenceReceiver");
        List<Geofence> mGeofenceList = GeofenceUtils.getGeofencesListFromLocations(context, null);
        geofenceService = new GeofenceService(context, mGeofenceList);
    }

}

