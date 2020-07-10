package com.vp.virtualmask;

import android.annotation.SuppressLint;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;
    public ImageButton Demo_button;
    FloatingActionButton fab_menu,fab_setting,fab_profile;
    Animation fabOpen,fabClose,fabClockwise,fabAntiClockwise;
    boolean isOpen= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
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
//                ImageButton Demo_button = (ImageButton) findViewById(R.id.cough_image);
//                Demo_button.setImageResource(R.drawable.man_mask);
                Demo_button.setVisibility(View.INVISIBLE);
                Demo_button2.setVisibility(View.VISIBLE);
                mContentView.setBackgroundColor((Color.parseColor("#E9E5E4")));
            }
        });

        Demo_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImageButton Demo_button = (ImageButton) findViewById(R.id.cough_image);
//                Demo_button.setImageResource(R.drawable.man_mask);
                Demo_button2.setVisibility(View.INVISIBLE);
                Demo_button.setVisibility(View.VISIBLE);
                mContentView.setBackgroundColor((Color.parseColor("#D4CCCA")));
            }
        });
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        fab_menu=findViewById(R.id.menuFloatingActionButton);
        fab_setting=findViewById(R.id.settingFloatingActionButton);
        fab_profile=findViewById(R.id.profileFloatingActionButton);


        fabOpen= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fabClose= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        fabClockwise= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        fabAntiClockwise= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise);

        fab_menu.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(isOpen){
                    fab_profile.startAnimation(fabClose);
                    fab_setting.startAnimation(fabClose);
                    fab_menu.startAnimation(fabClockwise);

                    fab_setting.setClickable(false);
                    fab_profile.setClickable(false);
                    Log.w("getting it in herer"+isOpen,"just in the invisibility functionality");
                    isOpen=false;

                }
                else{
                    fab_profile.startAnimation(fabOpen);
                    fab_setting.startAnimation(fabOpen);
                    fab_menu.startAnimation(fabAntiClockwise);

                    Log.w("getting it in herer"+isOpen,"just in the visibility functionality");
                    fab_setting.setClickable(true);
                    fab_profile.setClickable(true);

                    isOpen=true;

                }
            }
        });
        fab_setting.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FullscreenActivity.this, "tou clicked settings",Toast.LENGTH_SHORT).show();
                Log.w("eneter","setting on the go");
            }
        }));
        fab_profile.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(FullscreenActivity.this, "tou clicked profile",Toast.LENGTH_SHORT).show();
                Log.w("eneter","profile on the go");

            }
        }));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
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
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
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

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}