package com.scv.slackgo.helpers;

/** Constants used in companion app. */
public final class Constants {

    private Constants() {
    }

    public static final String PACKAGE_NAME = "com.scv.slackgo";

    public static final float SCV_OFFICE_LAT = -34.6024f;
    public static final float SCV_OFFICE_LONG = -58.4543f;
    public static final float DEFAULT_LAT = -34.6037f;
    public static final float DEFAULT_LONG = -58.3819f;
    public static final String OFFICE = "Office";
    public static final String OFICINA_CHANNEL_ID = "C04C5T185";
    public static final int RC_ASK_PERMISSIONS = 123;
    public static final float DEFAULT_RADIUS_METERS = 100.0f;
    public static final float DEFAULT_CAMERA_ZOOM = 15.0f;

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".PREFERENCES";
    public static final String SLACK_TOKEN = SHARED_PREFERENCES_NAME + ".token";
    public static final String SHARED_PREFERENCES_LOCATIONS = SHARED_PREFERENCES_NAME + ".LOCATIONS_SHARED";
    public static final String SHARED_PREFERENCES_CHANNELS = SHARED_PREFERENCES_NAME + ".CHANNELS";

    public static final String INTENT_LOCATION_CLICKED = PACKAGE_NAME + ".REGION_CLICKED";
    public static final String INTENT_LOCATION_LIST = PACKAGE_NAME + ".REGION_LIST";


}
