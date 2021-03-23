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

    private TextView applicationStatus;
    private Button gpsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        gpsButton = findViewById(R.id.gpsButton);
        applicationStatus = findViewById((R.id.applicationStatus));
        gpsButton.setBackgroundColor(Color.parseColor("#29a19c"));
        gpsButton.setTextColor(Color.WHITE);
        askAccessLocationPermission();
        if (!isLocationProviderServiceRunning()) {
            applicationStatus.setText("Application is stopped!");
            applicationStatus.setBackgroundColor(Color.parseColor("#435055"));
            gpsButton.setText("Start GPS");
        } else {
            applicationStatus.setText("Application is running!");
            applicationStatus.setBackgroundColor(Color.GREEN);
            gpsButton.setText("Stop GPS");
        }

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLocationProviderServiceRunning()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        applicationStatus.setText("Application is running!");
                        applicationStatus.setBackgroundColor(Color.GREEN);
                        gpsButton.setText("Stop GPS");
                        startForegroundService(new Intent(TrackingActivity.this, LocationProviderService.class).putExtra("email", getIntent().getStringExtra("email")));
                    } else {
                        startService(new Intent(TrackingActivity.this, LocationProviderService.class));
                    }
                } else {
                    applicationStatus.setText("Application is stopped!");
                    applicationStatus.setBackgroundColor(Color.parseColor("#435055"));
                    gpsButton.setText("Start GPS");
                    stopService(new Intent(TrackingActivity.this, LocationProviderService.class));
                }
            }
        });
    }

    private void askAccessLocationPermission() {
        //Check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        //If permission is not granted, we ask user to grant access to
        //fine location and access background location
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
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

