package com.intelisys.backgroundlocation;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;
import java.util.prefs.PreferenceChangeEvent;

public class Common {
    public static final String KEY_REQUESTED_LOCATION_UPDATES = "LocationUpdateEnable";

    public static String getLocationText (Location mLocation){
        return mLocation == null ? "Unknow Location" : new StringBuilder()
                .append(mLocation.getLatitude())
                .append("/")
                .append(mLocation.getLongitude())
                .toString();
    }
    public static CharSequence getLocationTitle (MyBackgroundService myBackgroundService){
        return String.format("Virtual Mask Is Running : %1$s", DateFormat.getDateInstance().format(new Date()));
    }

    public static void setRequestLocationUpdates(Context context, boolean value) {
        PreferenceManager.
                getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTED_LOCATION_UPDATES,value)
                .apply();
    }

    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTED_LOCATION_UPDATES,false);
    }
}
