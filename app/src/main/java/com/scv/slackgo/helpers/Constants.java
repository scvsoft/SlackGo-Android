package com.scv.slackgo.helpers;

/**
 * Constants used in companion app.
 */
public final class Constants {

    private Constants() {
    }

    public static final String PACKAGE_NAME = "com.scv.slackgo";

    public static final String SCV_ADDRESS = "Doctor Nicolás Repetto 1841, Ciudad Autonoma Buenos Aires, Argentina";
    public static final float SCV_OFFICE_LAT = -34.6026f;
    public static final float SCV_OFFICE_LONG = -58.4546f;
    public static final float DEFAULT_LAT = SCV_OFFICE_LAT;
    public static final float DEFAULT_LONG = SCV_OFFICE_LONG;
    public static final String OFFICE = "Office";
    public static final String OFICINA_CHANNEL_ID = "C04C5T185";
    public static final String SLACK_SCV_TEAM = "SCV";
    public static final int RC_ASK_PERMISSIONS = 123;
    public static final float DEFAULT_RADIUS_METERS = 100.0f;
    public static final float DEFAULT_CAMERA_ZOOM = 16.0f;

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".PREFERENCES";
    public static final String SLACK_TOKEN = SHARED_PREFERENCES_NAME + ".token";
    public static final String SHARED_PREFERENCES_LOCATIONS = SHARED_PREFERENCES_NAME + ".LOCATIONS_SHARED";
    public static final String SHARED_PREFERENCES_CHANNELS = SHARED_PREFERENCES_NAME + ".CHANNELS";

    public static final String INTENT_LOCATION_CLICKED = PACKAGE_NAME + ".REGION_CLICKED";
    public static final String INTENT_LOCATION_LIST = PACKAGE_NAME + ".REGION_LIST";
}
