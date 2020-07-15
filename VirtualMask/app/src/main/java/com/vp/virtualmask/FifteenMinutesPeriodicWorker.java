package com.vp.virtualmask;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class FifteenMinutesPeriodicWorker extends Worker {

    private static final String TAG = "My15MinutesPeriodicWork";

    public FifteenMinutesPeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        System.out.println("In Worker Constructor");
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.e(TAG, "doWork: Work is done.");
        System.out.println("From Worker");
        return Result.success();
    }
}
