package com.scv.slackgo.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.scv.slackgo.R;
import com.scv.slackgo.exceptions.GeocoderException;
import com.scv.slackgo.models.Location;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by scvsoft on 11/11/16.
 */

public class MapHelper {

    public static void setMarker(Location location, GoogleMap googleMap) {
        LatLng officePosition = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(officePosition);
        markerOptions.title(location.getName());

        googleMap.addCircle(new CircleOptions().center(officePosition)
                .radius(location.getRadius())
                .strokeColor(Color.argb(255, 239, 83, 80))
                .fillColor(Color.argb(25, 239, 83, 80)));

        googleMap.addMarker(markerOptions).showInfoWindow();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(officePosition, location.getCameraZoom()));
    }

    public static String getAddressFromLocation(Context context, Location location) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            return addresses.get(0).getAddressLine(0).split(",")[0];
        } catch (IOException e) {
            Log.e("Geocoder", "Error.", e);
            throw new GeocoderException(context.getString(R.string.geocoder_not_working));
        }
    }

    public static LatLng getLocationFromAddress(Context context, String address) {
        Geocoder coder = new Geocoder(context);
        List<Address> addressList;

        try {
            addressList = coder.getFromLocationName(address, 5);
            if (address == null) {
                return null;
            }
            Address location = addressList.get(0);

            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {

        }

        return null;
    }

    public static void centerLocation(Context context, GoogleMap googleMap) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        android.location.Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        List<LatLng> locationsLatLngList = new ArrayList<LatLng>();
        locationsLatLngList.add(new LatLng(location.getLatitude(), location.getLongitude()));
        centerAndUpdateZoomLevel(locationsLatLngList, googleMap);
    }

    public static void centerAndUpdateZoomLevel(List<LatLng> latLngList, GoogleMap googleMap) {
        CameraUpdate cameraUpdate = null;
        LatLngBounds.Builder builder = buildLatLnagBounds(latLngList);
        if (!latLngList.isEmpty()) {
            LatLngBounds bounds = builder.build();
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 500);
        }
        if (cameraUpdate != null && googleMap != null) {
            googleMap.moveCamera(cameraUpdate);
        }
    }

    private static LatLngBounds.Builder buildLatLnagBounds(List<LatLng> latLngList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLang : latLngList) {
            builder.include(latLang);
        }
        return builder;
    }

}
