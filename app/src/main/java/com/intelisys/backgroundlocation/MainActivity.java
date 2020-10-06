package com.intelisys.backgroundlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    Button requestLocation,removeLocation,Launch;
    private static final int LOCATION_PERMISSION_CODE = 100;
    MyBackgroundService mService = null;
    boolean mBound = false;
    private InterstitialAd mInterstitialAd;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            MyBackgroundService.LocalBinder binder = (MyBackgroundService.LocalBinder)iBinder;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound=false;
        }
    };

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Toast.makeText(MainActivity.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Location Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        "Location Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestLocation = (Button)findViewById(R.id.request_location_updates_button);
        removeLocation = (Button)findViewById(R.id.remove_location_updated_button);
        Launch = (Button)findViewById(R.id.launch);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4710290828352372/8066461278");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        //requestLocation.setEnabled(false);
        Launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
                boolean value = preferences.getBoolean("isBackgroundSet", Boolean.parseBoolean(""));
                if (value == true) {
                    Intent intent = new Intent(MainActivity.this, VirtualMask.class);
                    startActivity(intent);
                } else {
                    if (value == false) {
                        Toast.makeText(getApplicationContext(), "You have disable the location feature", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, VirtualMask.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please update the location feayure by clicking on it", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        requestLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_PERMISSION_CODE);
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE);
                checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, LOCATION_PERMISSION_CODE);
                checkPermission(Manifest.permission.FOREGROUND_SERVICE, LOCATION_PERMISSION_CODE);
                //Log.d("VKK_DEV","The Request id passed til here the in the main activity line 76");
                mService.requestLocationUpdates();

                editor.putBoolean("isBackgroundSet", true);
                editor.apply();
                editor.putBoolean("MaskStatus", false);
                editor.apply();
                //Intent intent = new Intent(MainActivity.this, VirtualMask.class);
                //startActivity(intent);

            }
        });
        removeLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }     //Log.d("VKK_DEV","The Request id passed til here the in the main activity line 76");
                mService.removeLocationUpdates();
                SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isBackgroundSet", false);
                editor.apply();
                //Intent intent = new Intent(MainActivity.this, VirtualMask.class);
                //startActivity(intent);
            }
        });

        setButtonState(Common.requestingLocationUpdates(MainActivity.this));
        bindService(new Intent(MainActivity.this,
                        MyBackgroundService.class),
                mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if(mBound){
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals(Common.KEY_REQUESTED_LOCATION_UPDATES)){
            setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUESTED_LOCATION_UPDATES,false));

        }
    }

    private void setButtonState(boolean isRequestEnable) {
        if(isRequestEnable){
            requestLocation.setEnabled(false);
            removeLocation.setEnabled(true);
        }
        else {
            requestLocation.setEnabled(true);
            removeLocation.setEnabled(false);
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event)
    {
        if(event!=null)
        {
            String data = new StringBuilder()
                    .append(event.getLocation().getLatitude())
                    .append('/')
                    .append(event.getLocation().getLongitude())
                    .toString();
            Toast.makeText(mService,data,Toast.LENGTH_SHORT).show();
        }
    }
}