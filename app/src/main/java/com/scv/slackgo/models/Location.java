package com.scv.slackgo.models;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.scv.slackgo.helpers.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kado on 10/13/16.
 */

public class Location {
    private String name;
    private double latitude;
    private double longitude;
    private float radius;
    private float cameraZoom;
    private List<Channel> channels;

    public Location(Context context) {
        /* TODO correct permissons to be able to set your location at first.
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        android.location.Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        */
        this.longitude = Constants.SCV_OFFICE_LONG;
        this.latitude = Constants.SCV_OFFICE_LAT;
        this.name = "";
        this.radius = Constants.DEFAULT_RADIUS_METERS;
        this.cameraZoom = Constants.DEFAULT_CAMERA_ZOOM;
        this.channels = new ArrayList<Channel>(Arrays.asList(new Channel(Constants.OFFICE, false, Constants.OFICINA_CHANNEL_ID)));
    }

    public Location(String name, double latitude, double longitude, float radius, float cameraZoom, List<Channel> channels) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.cameraZoom = cameraZoom;
        this.channels = channels;
    }

    public static Location getSCVLocation() {
        return new Location(Constants.OFFICE, Constants.SCV_OFFICE_LAT,
                Constants.SCV_OFFICE_LONG, Constants.DEFAULT_RADIUS_METERS, Constants.DEFAULT_CAMERA_ZOOM,
                new ArrayList<Channel>(Arrays.asList(new Channel(Constants.OFFICE, false, Constants.OFICINA_CHANNEL_ID))));
    }

    public Location(LatLng location) {
        this("Location 1", location.latitude, location.longitude,
                Constants.DEFAULT_RADIUS_METERS, Constants.DEFAULT_CAMERA_ZOOM,
                new ArrayList<Channel>(Arrays.asList(new Channel())));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getRadius() {
        return radius;
    }

    public float getCameraZoom() {
        return cameraZoom;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        final Location otherLocation = (Location) object;
        if ((this.name == null) ? (otherLocation.name != null) : !this.name.equals(otherLocation.name)) {
            return false;
        }
        return true;
    }
}
