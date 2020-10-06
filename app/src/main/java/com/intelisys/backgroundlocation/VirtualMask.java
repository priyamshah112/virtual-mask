package com.intelisys.backgroundlocation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

//import androidx.work.Constraints;
//import androidx.work.NetworkType;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkManager;
//https://www.youtube.com/watch?v=zpCRElrwXg8 this is the link for the floating button menu

public class VirtualMask extends AppCompatActivity {
    private static final String TAG = "VirtualMaskActivity";

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
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private InterstitialAd mInterstitialAd;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

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
        final ImageButton Demo_button_women = (ImageButton) findViewById(R.id.cough_image_women);
        final ImageButton Demo_button2_women = (ImageButton) findViewById(R.id.mask_image_women);
        SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
        final int value = preferences.getInt("GenderStatus", 0);
        boolean mask_status = preferences.getBoolean("MaskStatus", Boolean.parseBoolean(""));
        Log.d("vkk_dev","the vaule if the mask id :"+mask_status);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4710290828352372/8066461278");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        //Remote Config
        HashMap<String, Object> defaultsRate = new HashMap<>();
        defaultsRate.put("version", String.valueOf(getVersionCode()));

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10) // change to 3600 on published app
                .build();

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(defaultsRate);

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    final String version = mFirebaseRemoteConfig.getString("maskapp_version");

                    //change package name here
                    if(Integer.parseInt(version) > getVersionCode())
                        showTheDialog("com.intelisys.backgroundlocation", version );
                }
                else Log.e("MYLOG", "mFirebaseRemoteConfig.fetchAndActivate() NOT Successful");

            }
        });



        Demo_button2.setVisibility(View.INVISIBLE);
        Demo_button2_women.setVisibility(View.INVISIBLE);
        Demo_button.setVisibility(View.INVISIBLE);
        Demo_button_women.setVisibility(View.INVISIBLE);
        if(mask_status==true) {
            if (value == 1) {
                mContentView.setBackgroundColor((Color.parseColor("#E9E5E4")));
                Demo_button2.setVisibility(View.VISIBLE);
            } else {
                Demo_button2_women.setVisibility(View.VISIBLE);
                mContentView.setBackgroundColor((Color.parseColor("#E9E5E4")));
            }
        }
        else {
            if (value == 1) {
                Demo_button.setVisibility(View.VISIBLE);
                mContentView.setBackgroundColor((Color.parseColor("#D4CCCA")));
            } else {
                Demo_button_women.setVisibility(View.VISIBLE);
                mContentView.setBackgroundColor((Color.parseColor("#D4CCCA")));
            }
        }
        Demo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if (value == 1) {
                    Demo_button.setVisibility(View.INVISIBLE);
                    Demo_button2.setVisibility(View.VISIBLE);
                    Demo_button_women.setVisibility(View.INVISIBLE);
                    Demo_button2_women.setVisibility(View.INVISIBLE);
                }
                editor.putBoolean("MaskStatus", true);
                editor.apply();
                mContentView.setBackgroundColor((Color.parseColor("#E9E5E4")));
                tv.setText("Thank you for wearing mask");
                boolean mask_statu = preferences.getBoolean("MaskStatus", Boolean.parseBoolean(""));
                Log.d("vkk_dev", "the vaule if the mask id :" + mask_statu);
            }
        });

        Demo_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if (value == 1) {
                    Demo_button.setVisibility(View.VISIBLE);
                    Demo_button2.setVisibility(View.INVISIBLE);
                    Demo_button_women.setVisibility(View.INVISIBLE);
                    Demo_button2_women.setVisibility(View.INVISIBLE);
                }
                editor.putBoolean("MaskStatus", false);
                editor.apply();
                mContentView.setBackgroundColor((Color.parseColor("#D4CCCA")));
                tv.setText("PLease Wear Your Mask \n And click the Image");
                boolean mask_statu = preferences.getBoolean("MaskStatus", Boolean.parseBoolean(""));
                Log.d("vkk_dev", "the vaule if the mask id :" + mask_statu);

            }
        });
        Demo_button_women.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if (value == 2) {
                    Demo_button.setVisibility(View.INVISIBLE);
                    Demo_button2.setVisibility(View.INVISIBLE);
                    Demo_button_women.setVisibility(View.INVISIBLE);
                    Demo_button2_women.setVisibility(View.VISIBLE);
                }
                editor.putBoolean("MaskStatus", true);
                editor.apply();
                mContentView.setBackgroundColor((Color.parseColor("#E9E5E4")));
                tv.setText("Thank you for wearing mask");
                boolean mask_statu = preferences.getBoolean("MaskStatus", Boolean.parseBoolean(""));
                Log.d("vkk_dev", "the vaule if the mask id :" + mask_statu);

            }
        });
        Demo_button2_women.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if (value == 2) {
                    Demo_button.setVisibility(View.INVISIBLE);
                    Demo_button2.setVisibility(View.INVISIBLE);
                    Demo_button_women.setVisibility(View.VISIBLE);
                    Demo_button2_women.setVisibility(View.INVISIBLE);
                }
                editor.putBoolean("MaskStatus", false);
                editor.apply();
                mContentView.setBackgroundColor((Color.parseColor("#D4CCCA")));
                tv.setText("PLease Wear Your Mask \n And click the Image");
                boolean mask_statu = preferences.getBoolean("MaskStatus", Boolean.parseBoolean(""));
                Log.d("vkk_dev", "the vaule if the mask id :" + mask_statu);

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
                //Toast.makeText(VirtualMask.this, "tou clicked settings",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VirtualMask.this, MainActivity.class);
                startActivity(intent);
            }
        }));
        fab_profile.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VirtualMask.this, SelectGenderActivity.class);
                startActivity(intent);
                //Toast.makeText(VirtualMask.this, "tou clicked profile",Toast.LENGTH_SHORT).show();
            }
        }));
        Log.w("just to get a nsp","do theis work");
//        scheduleJob();
    }

    private void showTheDialog(final String appPackageName, String versionFromRemoteConfig){
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Update")
                .setMessage("New version of app is available. Version : "+versionFromRemoteConfig)
                .setPositiveButton("UPDATE", null)
                .show();

        dialog.setCancelable(false);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + appPackageName)));
                }
                catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
    }

    private PackageInfo pInfo;
    public int getVersionCode() {
        pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            Log.i("MYLOG", "NameNotFoundException: "+e.getMessage());
        }
        System.out.println(pInfo.versionCode);
        return pInfo.versionCode;
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


}