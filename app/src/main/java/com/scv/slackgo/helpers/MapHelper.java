package com.scv.slackgo.helpers;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.scv.slackgo.R;
import com.scv.slackgo.exceptions.GeocoderException;
import com.scv.slackgo.models.Location;

import java.io.IOException;
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
                .strokeColor(Color.argb(200, 255, 0, 255))
                .fillColor(Color.argb(25, 255, 0, 255)));

        googleMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(officePosition, location.getCameraZoom()));
    }

    public static String getAddressFromLocation(Context context, Location location) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            return addresses.get(0).getAddressLine(0);
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

}
