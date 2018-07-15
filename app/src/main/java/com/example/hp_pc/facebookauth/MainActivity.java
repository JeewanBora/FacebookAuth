package com.example.hp_pc.facebookauth;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    //Declairing variables
    CallbackManager callbackManager;

    LoginButton loginButton;
    Dialog mDialog;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing variables
        //callbackmanager is responsible to handle the events when we click the login button
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);

        //setReadPermissions is used to readout the user information
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setTitle("Logging In...");
                mDialog.show();
                String accessToken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        //calling the getFacebookData()
                        Bundle bundle = getFacebookData(object);
                        //after calling the above method and successfully storing the data into bundle object
                        //now retriving that from bundle object and storing into string variables
                        String firstname = bundle.getString("first_name");
                        String middlename = bundle.getString("middle_name");
                        String lastname = bundle.getString("last_name");
                        String email = bundle.getString("email");
                        String birthday = bundle.getString("birthday");
                        String friends = bundle.getString("friends");
                        String profilepic = bundle.getString("profile_pic");

                        //now passing the collected info to next activity using Intent
                        Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
                        //passing data using putExtra() with the help of key and value
                        intent.putExtra("firstname", firstname);
                        intent.putExtra("middlename", middlename);
                        intent.putExtra("lastname", lastname);
                        intent.putExtra("email", email);
                        intent.putExtra("birthday", birthday);
                        intent.putExtra("friends", friends);
                        intent.putExtra("image", profilepic);
                        startActivity(intent);
                        finish();
                        mDialog.dismiss();
                    }
                });

                //request graph API
                //we are using bundle to store multiple data or values (in our case user info) in a single key
                //here the key is fields and the corresponding values are id, first_name ... etc.
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, middle_name, last_name, email, birthday, friends");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    //creating bundle object and fetching the JSON data
    private Bundle getFacebookData(JSONObject object) {
        Bundle bundle = new Bundle();
        try {
            //using JSON object finding the id by getString(). since the data is in JSON format i.e. key value pairs
            //and defining the JSON object and JSON Array{[..]}
            String id = object.getString("id");
            //this url is to get the user image using the below link and passing the id for every individual user
            URL profile_picture = new URL("https://graph.facebook.com/" + id + "/picture?width=200&hight=200");
            //finally storing the image into bundle object using putString()
            bundle.putString("profile_pic", profile_picture.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        bundle.putString("idFacebook", id);

        //checking the condition if there is any first name or not if it exist then return to bundle object
        //similarly checking for further details
        if (object.has("first_name")){
            try {
                bundle.putString("first_name", object.getString("first_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (object.has("middle_name")){
            try {
                bundle.putString("middle_name", object.getString("middle_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (object.has("last_name")){
            try {
                bundle.putString("last_name", object.getString("last_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (object.has("email")){
            try {
                bundle.putString("email", object.getString("email"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (object.has("birthday")){
            try {
                bundle.putString("birthday", object.getString("birthday"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (object.has("friends")){
            try {
                bundle.putString("friends", object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return bundle;

    }

    //this method helps to load all the data once user clicks on the login button
    //it uses 3 parameters requestCode to request for the data
    //resultCode once the data found then get it as a result
    //data pass this through intent
    //atlast we call setVisibility(Gone) to hide the login button once the user successfully login otherwise its state will changed to logout
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        loginButton.setVisibility(View.GONE);
    }
}