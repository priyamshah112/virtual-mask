package com.vp.virtualmask;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import androidx.work.impl.model.Preference;

import java.text.DateFormat;
import java.util.Date;
import java.util.prefs.PreferenceChangeEvent;

public class Common {
    public static final String KEY_REQUESTING_LOCATION_UPDATES = "LocationUpdateEnable";

    public static String getLocationText(Location mLocation){
        return mLocation == null ? "Unknown Location" : new StringBuilder()
                .append(mLocation.getLatitude())
                .append("/")
                .append(mLocation.getLongitude())
                .toString();
    }

    public static CharSequence getLocationTitle() {
        return String.format("Location Updated: %1$s", DateFormat.getDateInstance().format(new Date()));
    }

    public static void setRequestLocationUpdates(Context con, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(con)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES,value)
                .apply();
    }

    public static boolean requestLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences( context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES,false);

    }
}
