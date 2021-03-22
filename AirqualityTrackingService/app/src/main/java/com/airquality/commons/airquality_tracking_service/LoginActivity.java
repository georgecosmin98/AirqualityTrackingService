package com.airquality.commons.airquality_tracking_service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class LoginActivity extends Activity {

    private EditText email, password;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.textFieldEmail);
        password = findViewById(R.id.textFieldPassword);
        loginButton = findViewById(R.id.loginButton);
        JSONObject jsonBody = new JSONObject();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Close virtual keyboard after press Login button
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                //Try to send authentication request to tomcat backend server
                try {
                    jsonBody.put("username", email.getText().toString());
                    jsonBody.put("password", password.getText().toString());
                    String requestBody = jsonBody.toString();
                    //Instantiate the RequestQueue
                    RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                    //Post authentication request to provided url
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, "", new Response.Listener<String>() {

                        @Override
                        //onResponse is called when post request was succesfully
                        public void onResponse(String response) {
                            //Verify if user send valid credentials
                            if (Integer.parseInt(response) == 200) {
                                //Is an abstract description of an operations to be performed, in our case, we use it for
                                //startActivity method to switch between activity (application page)
                                Intent intentAdmin = new Intent(LoginActivity.this, TrackingActivity.class).putExtra("email",email.getText().toString());
                                startActivity(intentAdmin);
                            } else {
                                System.out.println("error");
                                Toast.makeText(getApplicationContext(),"Invalid username or password",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        //onErrorResponse is called when something wrong occured (post request was failed)
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error);
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
                        //Parse network response, and return status code
                        protected Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
                            String responseString = "";
                            if (networkResponse != null) {
                                responseString = String.valueOf(networkResponse.statusCode);
                            }
                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(networkResponse));
                        }
                    };
                    requestQueue.add(stringRequest);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}



