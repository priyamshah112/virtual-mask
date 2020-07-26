package com.pratikbutani.workerexample;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.pratikbutani.workerexample.databinding.ActivityMainBinding;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

	private static final int PERMISSION_REQUEST_CODE = 200;
	private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
	private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
	private static final String TAG = "LocationUpdate";

	ActivityMainBinding mainBinding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

		setSupportActionBar(mainBinding.toolbar);

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//			if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//					== PackageManager.PERMISSION_GRANTED) {
//				if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//						!= PackageManager.PERMISSION_GRANTED) {
//					if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
//						final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//						builder.setTitle("This app needs background location access");
//						builder.setMessage("Please grant location access so this app can detect beacons in the background.");
//						builder.setPositiveButton(android.R.string.ok, null);
//						builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//							@TargetApi(23)
//							@Override
//							public void onDismiss(DialogInterface dialog) {
//								requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//										PERMISSION_REQUEST_BACKGROUND_LOCATION);
//							}
//
//						});
//						builder.show();
//					}
//					else {
//						final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//						builder.setTitle("Functionality limited");
//						builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
//						builder.setPositiveButton(android.R.string.ok, null);
//						builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//							@Override
//							public void onDismiss(DialogInterface dialog) {
//							}
//
//						});
//						builder.show();
//					}
//
//				}
//			} else {
//				if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
//					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
//									Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//							PERMISSION_REQUEST_FINE_LOCATION);
//				}
//				else {
//					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//					builder.setTitle("Functionality limited");
//					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
//					builder.setPositiveButton(android.R.string.ok, null);
//					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//						@Override
//						public void onDismiss(DialogInterface dialog) {
//						}
//
//					});
//					builder.show();
//				}
//
//			}
//		}
		if (!checkLocationPermission()) {
			ActivityCompat.requestPermissions(this,
					new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_CODE);
		}

		try {
			if (isWorkScheduled(WorkManager.getInstance().getWorkInfosByTag(TAG).get())) {
				mainBinding.appCompatButtonStart.setText(getString(R.string.button_text_stop));
				mainBinding.message.setText(getString(R.string.message_worker_running));
				mainBinding.logs.setText(getString(R.string.log_for_running));
			} else {
				mainBinding.appCompatButtonStart.setText(getString(R.string.button_text_start));
				mainBinding.message.setText(getString(R.string.message_worker_stopped));
				mainBinding.logs.setText(getString(R.string.log_for_stopped));
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		mainBinding.appCompatButtonStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mainBinding.appCompatButtonStart.getText().toString().equalsIgnoreCase(getString(R.string.button_text_start))) {
					// START Worker
					PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(MyWorker.class, 15, TimeUnit.MINUTES)
							.addTag(TAG)
							.build();
					WorkManager.getInstance().enqueueUniquePeriodicWork("Location", ExistingPeriodicWorkPolicy.REPLACE, periodicWork);

					Toast.makeText(MainActivity.this, "Location Worker Started : " + periodicWork.getId(), Toast.LENGTH_SHORT).show();

					mainBinding.appCompatButtonStart.setText(getString(R.string.button_text_stop));
					mainBinding.message.setText(periodicWork.getId().toString());
					mainBinding.logs.setText(getString(R.string.log_for_running));
				} else {

					WorkManager.getInstance().cancelAllWorkByTag(TAG);

					mainBinding.appCompatButtonStart.setText(getString(R.string.button_text_start));
					mainBinding.message.setText(getString(R.string.message_worker_stopped));
					mainBinding.logs.setText(getString(R.string.log_for_stopped));
				}
			}
		});
	}

	private boolean isWorkScheduled(List<WorkInfo> workInfos) {
		boolean running = false;
		if (workInfos == null || workInfos.size() == 0) return false;
		for (WorkInfo workStatus : workInfos) {
			running = workStatus.getState() == WorkInfo.State.RUNNING | workStatus.getState() == WorkInfo.State.ENQUEUED;
		}
		return running;
	}

	/**
	 * All about permission
	 */

	private boolean checkLocationPermission() {
		int result3 = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);
		int result4 = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
		int result5 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

		return result3 == PackageManager.PERMISSION_GRANTED &&
				result4 == PackageManager.PERMISSION_GRANTED && result5 == PackageManager.PERMISSION_GRANTED;
	}

//	@Override
//	public void onRequestPermissionsResult(int requestCode,
//										   String permissions[], int[] grantResults) {
//		switch (requestCode) {
//			case PERMISSION_REQUEST_FINE_LOCATION: {
//				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//					Log.d(TAG, "fine location permission granted");
//				} else {
//					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//					builder.setTitle("Functionality limited");
//					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
//					builder.setPositiveButton(android.R.string.ok, null);
//					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//						@Override
//						public void onDismiss(DialogInterface dialog) {
//						}
//
//					});
//					builder.show();
//				}
//				return;
//			}
//			case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
//				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//					Log.d(TAG, "background location permission granted");
//				} else {
//					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//					builder.setTitle("Functionality limited");
//					builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
//					builder.setPositiveButton(android.R.string.ok, null);
//					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//						@Override
//						public void onDismiss(DialogInterface dialog) {
//						}
//
//					});
//					builder.show();
//				}
//				return;
//			}
//		}
//	}
}
