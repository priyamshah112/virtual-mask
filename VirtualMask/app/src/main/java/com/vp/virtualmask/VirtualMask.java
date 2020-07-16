package com.vp.virtualmask;

import android.annotation.SuppressLint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
//https://www.youtube.com/watch?v=zpCRElrwXg8 this is the link for the floating button menu

public class VirtualMask extends AppCompatActivity {
    private static final String TAG = "VirtualMaskActivity";

    // Geolocation
    private static final int REQUEST_PERMISSIONS = 100;
    boolean boolean_permission;
    Double latitude = 0.0, longitude = 0.0;
    Geocoder geocoder;

    //Animations
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;
    public ImageButton Demo_button; // this button might be removed to be checked in future testing
    FloatingActionButton fab_menu,fab_setting,fab_profile;
    Animation fabOpen,fabClose,fabClockwise,fabAntiClockwise;
    boolean isOpen= false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        geocoder = new Geocoder(this, Locale.getDefault());
        fn_permission();
        System.out.println("GeoLocation service started");
        Intent intent = new Intent(getApplicationContext(), GoogleService.class);
        startService(intent);

        System.out.println("Latitude ");
        System.out.println(get_lat());
        System.out.println("Longitude");
        System.out.println(get_long());
        System.out.println("Current Time");
        System.out.println(get_current_time());

        //Periodic Activity for minimum 15 minutes limit
//        System.out.println("Work Called");
//        Constraints constraints = new Constraints.Builder()
//                .setRequiresBatteryNotLow(true)
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .setRequiresStorageNotLow(true)
//                .build();
//
//        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
//                FifteenMinutesPeriodicWorker.class, 16, TimeUnit.MINUTES)
//                .setConstraints(constraints)
//                .build();
//
//        System.out.println("Worker Called");
//        WorkManager.getInstance(this).enqueue(periodicWorkRequest);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        final TextView tv = (TextView)findViewById(R.id.textView);
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        final ImageButton Demo_button = (ImageButton) findViewById(R.id.cough_image);
        final ImageButton Demo_button2 = (ImageButton) findViewById(R.id.mask_image);
        Demo_button2.setVisibility(View.INVISIBLE);
        Demo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Demo_button.setVisibility(View.INVISIBLE);
                Demo_button2.setVisibility(View.VISIBLE);
                mContentView.setBackgroundColor((Color.parseColor("#E9E5E4")));
                tv.setText("Thank you for wearing mask");
            }
        });

        Demo_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Demo_button2.setVisibility(View.INVISIBLE);
                Demo_button.setVisibility(View.VISIBLE);
                mContentView.setBackgroundColor((Color.parseColor("#D4CCCA")));
                tv.setText("PLease Wear Your Mask \n And click the Image");
            }
        });
        fab_menu=findViewById(R.id.menuFloatingActionButton);
        fab_setting=findViewById(R.id.settingFloatingActionButton);
        fab_profile=findViewById(R.id.profileFloatingActionButton);


        fabOpen= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fabClose= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        fabClockwise= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        fabAntiClockwise= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise);
//Thank you for wearing mask
        fab_menu.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(isOpen){
                    fab_profile.startAnimation(fabClose);
                    fab_setting.startAnimation(fabClose);
                    fab_menu.startAnimation(fabClockwise);
                    fab_setting.setClickable(false);
                    fab_profile.setClickable(false);
                    isOpen=false;
                }
                else{
                    fab_profile.startAnimation(fabOpen);
                    fab_setting.startAnimation(fabOpen);
                    fab_menu.startAnimation(fabAntiClockwise);
                    fab_setting.setClickable(true);
                    fab_profile.setClickable(true);
                    isOpen=true;
                }
            }
        });
        fab_setting.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(VirtualMask.this, "tou clicked settings",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VirtualMask.this, SettingActivity.class);
                startActivity(intent);
            }
        }));
        fab_profile.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(VirtualMask.this, "tou clicked profile",Toast.LENGTH_SHORT).show();
            }
        }));
        Log.w("just to get a nsp","do theis work");
        scheduleJob();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        delayedHide(100);

    }

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private void show() {
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(VirtualMask.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(VirtualMask.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION

                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;

                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            latitude = Double.valueOf(intent.getStringExtra("latutide"));
            longitude = Double.valueOf(intent.getStringExtra("longitude"));

//            System.out.println(latitude);
//            System.out.println(longitude);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(GoogleService.str_receiver));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    public double get_lat(){
        return latitude;
    }

    public double get_long(){
        return longitude;
    }

    public String get_current_time(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return ts;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void scheduleJob() {
        Log.w("just to get a nsp","do theis work");
        ComponentName componentName = new ComponentName(this, BackgroundWork.class);
        JobInfo info = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            info = new JobInfo.Builder(123, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)
                    .setPeriodic(1 * 60 * 1000)
                    .build();
        }
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void cancelJob() {
        JobScheduler scheduler = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        }
        scheduler.cancel(123);
        Log.d(TAG, "Job cancelled");
    }

}