package com.example.hp_pc.facebookauth;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserDetailActivity extends AppCompatActivity implements LocationListener {

    //Declaring variable
    @BindView(R.id.name) TextView txtname;
    @BindView(R.id.email) TextView txtemail;
    @BindView(R.id.birthdate) TextView txtbirthday;
    @BindView(R.id.friends) TextView txtfrnds;
    @BindView(R.id.location)TextView txtlocation;
    @BindView(R.id.logout) Button logout;
    @BindView(R.id.locationuser) Button location;
    @BindView(R.id.profile_pic) CircleImageView imgprofile;

    LocationManager locationManager;
    String provider;
    double lat, lng;
    final int MY_PERMISSION_REQUEST_CODE = 7171;

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    //this method is used to handle the requested permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getLocation();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        //Initializing Variables
        ButterKnife.bind(this);

        //checking the permission to find the location once the app is open
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                Location mylocation = locationManager.getLastKnownLocation(provider);
                lat = mylocation.getLatitude();
                lng = mylocation.getLongitude();
                new GetAddress().execute(String.format("%.4f,%.4f",lat,lng));
            }
        });

        //getting the data from intent using getIntent() with the help of key
        String firstname = getIntent().getExtras().getString("firstname");
        String middlename = getIntent().getExtras().getString("middlename");
        String lastname = getIntent().getExtras().getString("lastname");
        String email = getIntent().getExtras().getString("email");
        String birthday = getIntent().getExtras().getString("birthday");
        String friends = getIntent().getExtras().getString("friends");
        String image = getIntent().getExtras().getString("image");

        //finally checking the condition if all the required info is available than set them into corresponding Views
        if (firstname != null && lastname != null && email != null && birthday != null && image != null) {
            txtname.setText(firstname + " " + middlename + " " + lastname);
            txtemail.setText("Email: " + email);
            txtbirthday.setText("Birthday: " + birthday);
            txtfrnds.setText("Total Friends: " + friends);
            Picasso.get().load(image.toString()).placeholder(R.drawable.com_facebook_profile_picture_blank_portrait).into(imgprofile);
        } else {
            Toast.makeText(getApplicationContext(), "Data not found", Toast.LENGTH_SHORT).show();
        }
//logout button to return back to the login activity
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(UserDetailActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        final Location location = locationManager.getLastKnownLocation(provider);
        if (location == null)
            Log.e("Error", "location is null");

    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        //creating AsyncTask class to handle data from web service
        new GetAddress().execute(String.format("%.4f,%.4f",lat,lng));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    //AsyncTask class to handle data from web service
    //it holds 3 methods
    //onPreExecute() before getting the data all the progressing process done here like progress dialog
    private class GetAddress extends AsyncTask<String, Void, String>{

        ProgressDialog dialog = new ProgressDialog(UserDetailActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        //doInbackground() once the onPreExecute() completed all the server side data process done here
        @Override
        protected String doInBackground(String... strings) {
            try{
                //spliting the parameters
                double lat = Double.parseDouble(strings[0].split(",")[0]);
                double lng = Double.parseDouble(strings[0].split(",")[1]);
                String response;

                //Using HttpDataHandler.GetHttpData to get result from Web Api
                HttpDataHandler http = new HttpDataHandler();
                //This is url of Google Api to convert lat & lng to address
                String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%.4f,%.4f&sensor=false",lat,lng);
                response = http.GetHTTPData(url);
                return response;
            }
            catch (Exception ex){

            }
            return null;
        }

        //atlast onPostExecuet() calls to show the data that was taken from server in JSON formet
        @Override
        protected void onPostExecute(String s) {
            try{
                JSONObject jsonObject = new JSONObject(s);
                //"results" is root object and "formatted_address" is element
                String address = ((JSONArray)jsonObject.get("results")).getJSONObject(0).get("formatted_address").toString();
                txtlocation.setText(address);

            }
            catch (JSONException e){
                e.printStackTrace();
            }

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }
}
