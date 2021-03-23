package com.airquality.commons.airquality_tracking_service;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TrackingActivity extends Activity {

    public static final String STOPPED_APPLICATION_MESSAGE = "Application is stopped!";
    public static final String RUNNING_APPLICATION_MESSAGE = "Application is running!";
    public static final String START_GPS_BUTTON_TEXT = "Start GPS";
    public static final String STOP_GPS_BUTTON_TEXT = "Stop GPS";
    private TextView applicationStatus;
    private Button gpsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        gpsButton = findViewById(R.id.gpsButton);
        applicationStatus = findViewById((R.id.applicationStatus));
        gpsButton.setBackgroundColor(Color.parseColor(Constants.BUTTONS_BACKGROUND_COLOR));
        gpsButton.setTextColor(Color.WHITE);
        askAccessLocationPermission();

        if (!isLocationProviderServiceRunning()) {
            setGPSButtonStart();
        } else {
            setGPSButtonStop();
        }

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLocationProviderServiceRunning()) {
                    //After android 8, we must start our services like a foreground service
                    //We treated all options to add support for older phones (Android version < 8)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setGPSButtonStop();
                        startForegroundService(new Intent(TrackingActivity.this, LocationProviderService.class)
                                .putExtra(Constants.EMAIL_EXTRA_STRING, getIntent().getStringExtra(Constants.EMAIL_EXTRA_STRING)));
                    } else {
                        setGPSButtonStop();
                        startService(new Intent(TrackingActivity.this, LocationProviderService.class)
                                .putExtra(Constants.EMAIL_EXTRA_STRING, getIntent().getStringExtra(Constants.EMAIL_EXTRA_STRING)));
                    }
                } else {
                    setGPSButtonStart();
                    stopService(new Intent(TrackingActivity.this, LocationProviderService.class));
                }
            }
        });
    }

    private void setGPSButtonStart() {
        applicationStatus.setText(STOPPED_APPLICATION_MESSAGE);
        applicationStatus.setBackgroundColor(Color.parseColor("#435055"));
        gpsButton.setText(START_GPS_BUTTON_TEXT);
    }

    private void setGPSButtonStop() {
        applicationStatus.setText(RUNNING_APPLICATION_MESSAGE);
        applicationStatus.setBackgroundColor(Color.GREEN);
        gpsButton.setText(STOP_GPS_BUTTON_TEXT);
    }

    private void askAccessLocationPermission() {
        //Check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        //If permission is not granted, we ask user to grant access to
        //fine location and access background location
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    99);
        }
    }

    //Check if location provider foreground service is running on android device
    private boolean isLocationProviderServiceRunning() {

        //ActivityManager contains information about services
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        //If we have an activity manager
        if (activityManager != null) {

            //Search between all running services and check if we have a LocationProviderServices service running
            for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationProviderService.class.getName().equals(runningServiceInfo.service.getClassName())) {
                    //Verify if find process is a foreground one (running in background with lockscreen)
                    if (runningServiceInfo.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }
}

