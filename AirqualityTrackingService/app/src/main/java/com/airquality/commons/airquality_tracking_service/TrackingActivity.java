package com.airquality.commons.airquality_tracking_service;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TrackingActivity extends AppCompatActivity {

    TextView latitudeFieldText, longitudeFieldText;
    private Button gpsButton;
    private Boolean isEnable = false;
    private Boolean beforeWasRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        latitudeFieldText = findViewById(R.id.latitudeTextField);
        longitudeFieldText = findViewById(R.id.longitudeTextField);
        gpsButton = findViewById(R.id.gpsButton);

        askAccessLocationPermission();
        if(!isLocationProviderServiceRunning()){
            gpsButton.setText("Start GPS");
        }
        else{
            gpsButton.setText("Stop GPS");
        }

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLocationProviderServiceRunning()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        gpsButton.setText("Stop GPS");
                        startForegroundService(new Intent(TrackingActivity.this, LocationProviderService.class).putExtra("email",getIntent().getStringExtra("email")));
                    } else {
                        startService(new Intent(TrackingActivity.this, LocationProviderService.class));
                    }
                } else {
                    gpsButton.setText("Start GPS");
                    stopService(new Intent(TrackingActivity.this, LocationProviderService.class));
                }
            }
        });
    }

    private void askAccessLocationPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // ask permissions here using below code
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    99);
        }
    }

    private boolean isLocationProviderServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationProviderService.class.getName().equals(runningServiceInfo.service.getClassName())) {
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

