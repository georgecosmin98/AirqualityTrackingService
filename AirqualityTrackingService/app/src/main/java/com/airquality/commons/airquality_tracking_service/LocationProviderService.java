package com.airquality.commons.airquality_tracking_service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LocationProviderService extends Service implements LocationListener {

    public static final String NOTIFICATION_CHANNEL_ID = "HartaBV";

    protected LocationManager locationManager;
    private String email;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    //The system invokes this method by calling startService method
    public int onStartCommand(Intent intent, int flags, int startId) {
        email = intent.getStringExtra(Constants.EMAIL_EXTRA_STRING);
        Intent newIntent = new Intent(this, TrackingActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //After Android 8.0 (API level 26, or VERSION-CODE O), all notification
            //must be assigned to a channel, so that users can more easily manage
            //these notifications
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, "Location Provider Service Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        //PendingIntent is used for redirect user to TrackingActivity when press Airquality-Tracking-Service notification
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{newIntent}, 0);
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Airquality-Tracking-Service")
                .setContentText("Tracking application is running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();

        //Start LocationProviderService and make a sticky notification
        startForeground(1, notification);

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    //This method is called when service is initially created
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Verify if application have permission to access fine location
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
    }

    @Override
    //This method is called when the service is no longer used and is being destroyed
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", email);
            jsonBody.put("latitude", String.valueOf(location.getLatitude()));
            jsonBody.put("longitude", String.valueOf(location.getLongitude()));
            jsonBody.put("timestamp", System.currentTimeMillis());
            String requestBody = jsonBody.toString();
            //Instantiate the RequestQueue
            RequestQueue requestQueue = Volley.newRequestQueue(LocationProviderService.this);
            //Post authentication request to provided url
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.USERS_LOCATIONS_URL, new Response.Listener<String>() {
                @Override
                //onResponse is called when post request was succesfully
                public void onResponse(String response) {
                    //Verify if post request was successfully sent
                    if (Integer.parseInt(response) == 200) {
                    } else {
                        Toast.makeText(getApplicationContext(), Constants.ERROR_TOAST_MESSAGE, Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                //onErrorResponse is called when something wrong occured (post request was failed)
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error);
                    Toast.makeText(getApplicationContext(), Constants.ERROR_TOAST_MESSAGE, Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                //Convert requestBody object from string to byte[] object
                //to encoding post request body. UTF-8 is default encoding for application/json media type
                public byte[] getBody() {
                    try {
                        if (requestBody == null)
                            return null;
                        else
                            return requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException ex) {
                        System.out.println(ex.getStackTrace());
                        return null;
                    }
                }

                @Override
                //Parse network response, and return status code (we dont need JWT Token, so we return just status code)
                protected Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
                    String statusCode = "";
                    if (networkResponse != null) {
                        statusCode = String.valueOf(networkResponse.statusCode);
                    }
                    //Return just status code, without header
                    return Response.success(statusCode, null);
                }
            };
            //Add string request to queue
            requestQueue.add(stringRequest);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}


