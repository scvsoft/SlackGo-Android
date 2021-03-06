/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scv.slackgo.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.scv.slackgo.R;
import com.scv.slackgo.activities.LocationsListActivity;
import com.scv.slackgo.helpers.GeofenceUtils;
import com.scv.slackgo.models.Channel;
import com.scv.slackgo.models.LocationsStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Listens for geofence transition changes.
 */
public class GeofenceTransitionsIntentService extends IntentService implements Observer {

    protected static final String TAG = "GeofenceTransitionsIS";
    private Map<String, List<Channel>> channelsForLocationMap;
    private List<Channel> channelsList;
    private LocationsStore locationsStore;

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        locationsStore = LocationsStore.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Log.e(TAG, "GeofencingEvent Error: " + event.getErrorCode());
            String description = getGeofenceTransitionDetails(event);
            sendNotification(getString(R.string.generic_notification_title), description);
            return;
        }
        int transitionType = event.getGeofenceTransition();
        channelsForLocationMap = GeofenceUtils.getChannelsForGeofences(locationsStore, event.getTriggeringGeofences());
        if (channelsList == null) {
            channelsList = locationsStore.getChannelsList();
        }

        Set<String> channelNames = new HashSet<>();
        for (List<Channel>channelList : channelsForLocationMap.values()) {
            for (Channel channel : channelList) {
                channelNames.add(channel.getName());
            }
        }
        String channels = TextUtils.join(", ", channelNames);
        String locations = TextUtils.join(", ", channelsForLocationMap.keySet());

        String title = "";
        String message = "";
        SlackApiService slackApiService = new SlackApiService(this);
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            title = getString(R.string.entering_geofence_title, locations);
            message = getString(R.string.entering_geofence_message, channels);
            joinChannelsFromGeofences(event.getTriggeringGeofences(), slackApiService);
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            title = getString(R.string.going_out_geofence_title, locations);
            message = getString(R.string.going_out_geofence_message, channels);
            leaveChannelsFromGeofences(event.getTriggeringGeofences(), slackApiService);
        }
        sendNotification(title, message);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data != null) {
            channelsList = (ArrayList<Channel>) data;
        }
    }

    private static String getGeofenceTransitionDetails(GeofencingEvent event) {
        String transitionString =
                GeofenceStatusCodes.getStatusCodeString(event.getGeofenceTransition());
        List triggeringIDs = new ArrayList();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }
        return String.format("%s: %s", transitionString, TextUtils.join(", ", triggeringIDs));
    }

    private void sendNotification(String title, String message) {
        // Create an explicit content Intent that starts MainActivity.
        Intent notificationIntent = new Intent(getApplicationContext(), LocationsListActivity.class);

        // Get a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(LocationsListActivity.class).addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setColor(Color.RED)
                .setSmallIcon(R.drawable.common_ic_googleplayservices)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);

        // Fire and notify the built Notification.
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    //Leave channels from geofence in transition
    private void leaveChannelsFromGeofences(List<Geofence> geofences, SlackApiService slackApiService) {
        for (Geofence geofence : geofences) {
            List<Channel> channels = channelsForLocationMap.get(geofence.getRequestId());
            for (Channel channel : channels) {
                slackApiService.leaveChannel(channel.getId());
            }
        }
    }

    //Join channels from geofences in transition
    private void joinChannelsFromGeofences(List<Geofence> geofences, SlackApiService slackApiService) {
        for (Geofence geofence : geofences) {
            List<Channel> channels = channelsForLocationMap.get(geofence.getRequestId());
            for (Channel channel : channels) {
                slackApiService.joinChannel(channel.getName());
            }
        }
    }
}
