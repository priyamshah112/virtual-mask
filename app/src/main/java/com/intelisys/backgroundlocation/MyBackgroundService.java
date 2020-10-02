package com.intelisys.backgroundlocation;


import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;



import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import java.util.logging.ConsoleHandler;


public class MyBackgroundService extends Service {
    private static final String CHANNEL_ID = "my_channel";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = "com.intelisys.backgroundlocation" + "started_from_notification";
    private float latitudes[] = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
    private float longitudes[] = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
    private float Mask_latitudes[] = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
    private float Mask_longitudes[] = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
    private final IBinder mBinder = new LocalBinder();
    private static final long UPDATE_INTERVAL_IN_MIL = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MIL = UPDATE_INTERVAL_IN_MIL / 2;
    private static final int NOTI_ID = 1223;
    private boolean mChangingConfigurationManager = false;
    private NotificationManager mNotificationManager;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler mServiceHandler;
    private Location mLocation;

    public MyBackgroundService() {
    }

    @Override
    public void onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread("VKK_DEV");
        handlerThread.start();
        mServiceHandler = new Handler((handlerThread.getLooper()));
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        mChangingConfigurationManager = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,false);
        if(startedFromNotification){
            removeLocationUpdates();
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    public void removeLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates((locationCallback));
            Common.setRequestLocationUpdates(this,false);
            stopSelf();
        }catch (SecurityException ex){
            Common.setRequestLocationUpdates(this,true);
            Log.e("VKK_DEV","Last location permission. Could not remove updates " +ex);
        }
    }

    private void getLastLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null)
                                mLocation = task.getResult();
                            else
                                Log.e("VKK_DEV", "Failed to get the location");
                        }
                    });
        }catch (SecurityException ex){
            Log.e("VKK_DEV","last location permission"+ex);
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval((UPDATE_INTERVAL_IN_MIL));
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MIL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void onNewLocation(Location lastLocation) {
        mLocation = lastLocation;
        EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));

        if(serviceIsRunningInForeground(this)) {
            String text = Common.getLocationText(mLocation);
            int iend = text.indexOf("/");
            String latitude= text.substring(0 , iend);;
            String longitude= text.substring(iend+1,text.length());
            Float latitude_float = Float.parseFloat(latitude);
            Float longitude_float = Float.parseFloat(longitude);
            boolean ArrayEmpty=false;
            boolean MaskArrayEmpty=false;
            for(int i=0;i<longitudes.length;i++)
            {
                if(longitudes[i]==0)
                {
                    Log.d("vkk_dev", "onNewLocation: entered the for");
                    longitudes[i]=longitude_float;
                    latitudes[i]=latitude_float;
                    ArrayEmpty=true;
                    break;
                }
            }
            for(int i=0;i<Mask_longitudes.length;i++)
            {
                if(Mask_longitudes[i]==0)
                {
                    Mask_longitudes[i]=longitude_float;
                    Mask_latitudes[i]=latitude_float;
                    MaskArrayEmpty=true;
                    break;
                }
            }
            if(MaskArrayEmpty==false){
                Log.d("vkk_dev","the latitude last falue is " + Mask_latitudes[Mask_latitudes.length-1]);
                for( int i = 0 ; i < Mask_longitudes.length-1; i++ ){
                    Mask_latitudes[i]=Mask_latitudes[i+1];
                    Mask_longitudes[i]=Mask_longitudes[i+1];
                }
                double distance = distance(latitudes[0],latitudes[latitudes.length-1],longitudes[0],longitudes[longitudes.length-1],0,0);
                System.out.println("the distance in mask is "+distance);
                if(distance<5){
                    Log.d("vkk_dev","setting the mask status back to false");
                    SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("MaskStatus", false);
                    editor.apply();
                }
            }
            if(ArrayEmpty==false){
                //Log.d("vkk_dev","the latitude last falue is " + latitudes[latitudes.length-1]);
                //Log.d("vkk_dev", "onNewLocation: entered the if");
                for( int i = 0 ; i < longitudes.length-1; i++ ){
                    latitudes[i]=latitudes[i+1];
                    longitudes[i]=longitudes[i+1];
                }
                longitudes[longitudes.length-1]=longitude_float;
                latitudes[latitudes.length-1]=latitude_float;
                double distance = distance(latitudes[0],latitudes[latitudes.length-1],longitudes[0],longitudes[longitudes.length-1],0,0);
                System.out.println("the distance is "+distance);
                if(distance > 20){
                    for( int i = 0 ; i < longitudes.length-1; i++ ){
                        latitudes[i]=0;
                        longitudes[i]=0;
                    }
                    for( int i = 0 ; i < Mask_longitudes.length-1; i++ ){
                        Mask_latitudes[i]=0;
                        Mask_longitudes[i]=0;
                    }

                    SharedPreferences preferences = getSharedPreferences("gender", MODE_PRIVATE);
                    boolean value = preferences.getBoolean("MaskStatus", Boolean.parseBoolean(""));
                    if(value==false)
                        mNotificationManager.notify(NOTI_ID, getNotification());
                }
            }
        }
    }

    private double distance(float lat1, float lat2, float lon1,
                                  float lon2, float el1, float el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    private Notification getNotification() {
        Intent intent = new Intent(this,MyBackgroundService.class);
        String text = Common.getLocationText(mLocation);
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION,true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,0,new Intent(this,VirtualMask.class),0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_baseline_launch_24,"Launch",activityPendingIntent)
                .addAction(R.drawable.ic_baseline_cancel_24,"Remove",servicePendingIntent)
                .setContentText("Please Wear up Your Mask")
                .setContentTitle(Common.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Please Wear up your Mask")
                .setWhen(System.currentTimeMillis());
        //Log.e("", "Outputting the version : "+Build.VERSION.SDK_INT+" the build version code is : "+Build.VERSION_CODES.O );
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            builder.setChannelId(CHANNEL_ID);
        }
        return builder.build();
    }

    private boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service:manager.getRunningServices(Integer.MAX_VALUE))
            if(getClass().getName().equals((service.service.getClassName())))
                if(service.foreground)
                    return true;
        return false;
    }

    public void requestLocationUpdates() {
        Common.setRequestLocationUpdates(this,true);
        Log.d("VKK_DEV","The Request id passed till here");
        startService(new Intent(getApplicationContext(),MyBackgroundService.class));
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }catch (SecurityException ex){
            Log.e("VKK_DEV","LOST LOCATION PERMISSION> COULD NOT REQUEST IT "+ex);
        }
    }

    public class LocalBinder extends Binder {
        MyBackgroundService getService() {return MyBackgroundService.this;}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        mChangingConfigurationManager = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent){
        stopForeground(true);
        mChangingConfigurationManager = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent){
        if(!mChangingConfigurationManager && Common.requestingLocationUpdates(this))
            startForeground(NOTI_ID,getNotification());
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacks(null);
        super.onDestroy();
    }

}


/*
package com.intelisys.backgroundlocation;


import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;



public class MyBackgroundService extends Service {
    private static final String CHANNEL_ID = "my_channel";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = "com.intelisys.backgroundlocation" + "started_from_notification";

    private final IBinder mBinder = new LocalBinder();
    private static final long UPDATE_INTERVAL_IN_MIL = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MIL = UPDATE_INTERVAL_IN_MIL / 2;
    private static final int NOTI_ID = 1223;
    private boolean mChangingConfigurationManager = false;
    private NotificationManager mNotificationManager;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler mServiceHandler;
    private Location mLocation;

    private NotificationManagerCompat notificationManager;


    public MyBackgroundService() {
    }

    @Override
    public void onCreate() {
        notificationManager = NotificationManagerCompat.from(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread("VKK_DEV");
        handlerThread.start();
        mServiceHandler = new Handler((handlerThread.getLooper()));
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        mChangingConfigurationManager = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,false);
        if(startedFromNotification){
            removeLocationUpdates();
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    public void removeLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates((locationCallback));
            Common.setRequestLocationUpdates(this,false);
            stopSelf();
        }catch (SecurityException ex){
            Common.setRequestLocationUpdates(this,true);
            Log.e("VKK_DEV","Last location permission. Could not remove updates " +ex);
        }
    }

    private void getLastLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null)
                                mLocation = task.getResult();
                            else
                                Log.e("VKK_DEV", "Failed to get the location");
                        }
                    });
        }catch (SecurityException ex){
            Log.e("VKK_DEV","last location permission"+ex);
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval((UPDATE_INTERVAL_IN_MIL));
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MIL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void onNewLocation(Location lastLocation) {
        mLocation = lastLocation;
        EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));

        if(serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTI_ID, getNotification());
            Notification notification = new NotificationCompat.Builder(this, Reminder_Channel)
                    .setContentTitle("Mask It UP")
                    .setContentText("Please wear your mask")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
            notificationManager.notify(1, notification);
        }
    }

    private Notification getNotification() {
        Intent intent = new Intent(this,MyBackgroundService.class);
        String text = Common.getLocationText(mLocation);

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION,true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_baseline_launch_24,"Launch",activityPendingIntent)
                .addAction(R.drawable.ic_baseline_cancel_24,"Remove",servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Common.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());
        Log.e("", "Outputting the version : "+Build.VERSION.SDK_INT+" the build version code is : "+Build.VERSION_CODES.O );
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            builder.setChannelId(CHANNEL_ID);
        }
        return builder.build();
    }

    private boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service:manager.getRunningServices(Integer.MAX_VALUE))
            if(getClass().getName().equals((service.service.getClassName())))
                if(service.foreground)
                    return true;
        return false;
    }

    public void requestLocationUpdates() {
        Common.setRequestLocationUpdates(this,true);
        Log.d("VKK_DEV","The Request id passed till here");
        startService(new Intent(getApplicationContext(),MyBackgroundService.class));
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }catch (SecurityException ex){
            Log.e("VKK_DEV","LOST LOCATION PERMISSION> COULD NOT REQUEST IT "+ex);
        }
    }

    public class LocalBinder extends Binder {
        MyBackgroundService getService() {return MyBackgroundService.this;}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        mChangingConfigurationManager = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent){
        stopForeground(true);
        mChangingConfigurationManager = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent){
        if(!mChangingConfigurationManager && Common.requestingLocationUpdates(this))
            startForeground(NOTI_ID,getNotification());
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacks(null);
        super.onDestroy();
    }

}

 */