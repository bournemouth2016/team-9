package teamnine.pay.apps.teamnine;

import android.*;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import teamnine.pay.apps.teamnine.connectors.Config;
import teamnine.pay.apps.teamnine.connectors.JSONParser;

/**
 * Created by kenneth on 11/18/16.
 */

public class Home extends AppCompatActivity implements View.OnClickListener, LocationListener {

    ImageView panicButton;

    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;

    List<NameValuePair> details;

    String response;
    String status;
    String incID;

    JSONParser jsonParser = new JSONParser();

    ProgressDialog dialog;

    ArrayList<Double> coords = new ArrayList<>();

    LinearLayout layoutHome;

    Button btnRescue;
    Button btnTrip;

    PendingIntent resultPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        setTitle("Home");

        panicButton = (ImageView) findViewById(R.id.imgPanic);
        panicButton.setOnClickListener(this);

        btnTrip = (Button) findViewById(R.id.btnTrip);
        btnTrip.setOnClickListener(this);

        btnRescue = (Button) findViewById(R.id.btnRescue);
        btnRescue.setText("People in danger: 0");
        btnRescue.setOnClickListener(this);

        layoutHome = (LinearLayout) findViewById(R.id.layoutHome);

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {

                List<String> permissions = new ArrayList<String>();
                permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
                permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
                permissions.add(android.Manifest.permission.VIBRATE);

                System.out.println("I REACHED HERE!!!");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions.toArray(new String[permissions.size()]), 101);
                }
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 100, this);

            // Create an instance of GoogleAPIClient.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult conresult) {
                            Toast.makeText(getBaseContext(), conresult.toString(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();

            //check if coordinates passed to close
            if (getIntent().getStringExtra("incID") != null) {
                System.out.println("I REACHED HERE");
                incID = getIntent().getStringExtra("incID").toString();
                new cancelDanger(incID).execute();
            }

        } catch (Exception r) {
            r.printStackTrace();
        }

    }

    public void onResume() {
        super.onResume();
        //check if coordinates passed to close
        if (getIntent().getStringExtra("incID") != null) {
            System.out.println("I REACHED HERE");
            incID = getIntent().getStringExtra("incID").toString();
            new cancelDanger(incID).execute();
        }

        //Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 100, this);
    }

    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==R.id.action_logout){
            //Check if logged in
            SharedPreferences pref = getSharedPreferences("MyPref", 1); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            editor.remove("phone");
            editor.commit();

            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.imgPanic){
            try{
                //show dialog to confirm button press
                AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
                myDialog.setTitle("Call for help?");
                myDialog.setMessage("Please confirm that you are in need of help");

                myDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        System.out.println("Coordinates: "+coords.toString());

                        if(coords.size()>0){
                            new sendEmergencyNotif(coords).execute();
                        }
                        else{
                            System.out.println("No coordinates so plan b");
                            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    mGoogleApiClient);
                            coords.add(mLastLocation.getLatitude());
                            coords.add(mLastLocation.getLongitude());
                            System.out.println("Latitude is: "+mLastLocation.getLatitude());

                            if(coords.size()>0){
                                new sendEmergencyNotif(coords).execute();
                            }
                            else{
                                Toast.makeText(getBaseContext(), "Could not get coordinates. Please try again", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
                myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = myDialog.create();
                alertDialog.show();
            }
            catch(Exception r){
                r.printStackTrace();
            }
        }
        if(v.getId()==R.id.btnRescue){
            try{
                new getInDanger().execute();
            }
            catch (Exception r){
                r.printStackTrace();
            }
        }
        if(v.getId()==R.id.btnTrip){
            try{
                if(btnTrip.getText().toString().contains("Sail out")){
                    status = "Leaving";
                    new pingServer(status).execute();
                }
                else{
                    status = "Returned";
                    new pingServer(status).execute();
                }
            }
            catch(Exception r){
                r.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("Latitude is: "+location.getLatitude());

        coords.clear();
        coords.add(location.getLatitude());
        coords.add(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    class sendEmergencyNotif extends AsyncTask<String, String, String> {

        public sendEmergencyNotif(ArrayList<Double> coords) {
            //get stored phone number
            SharedPreferences pref = getSharedPreferences("MyPref", 1); // 0 - for private mode
            String myphone = pref.getString("phone", null);

            System.out.println("My phone: "+myphone);

            details = new ArrayList<NameValuePair>();
            details.add(new BasicNameValuePair("phone", myphone));
            details.add(new BasicNameValuePair("gps", coords.toString()));
            details.add(new BasicNameValuePair("details", ""));
            details.add(new BasicNameValuePair("passengers", ""));
            details.add(new BasicNameValuePair("casualties", ""));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show progress dialog
            dialog = new ProgressDialog(Home.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("One moment...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(Config.SERVER_URL+"danger",
                        "POST", details);

                // check log cat fro response
                Log.d("Create Response", json.toString());

                // check for success tag
                //success = json.getString("status");
                response = json.getString("status");
                incID = json.getString("incident_id");
                System.out.println(response);

            } catch (Exception e) {
                e.printStackTrace();
                response = "Connection error. Please try again";
            }
            return response;
        }

        protected void onPostExecute(String getResponse) {
            // dismiss the dialog once done
            try{
                dialog.dismiss();
                getResponse = response;
                if(getResponse.equals("ok")){
                    try{
                        //create notification to help cancel emergency
                        NotificationManager nm = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        long[] vibrate_ptn = {0, 100, 300, 500};

                        Long timestamp = System.currentTimeMillis()/1000;

                        System.out.println("Timestamp is: "+timestamp);

                        Intent intentClose = new Intent(getBaseContext(), Home.class);
                        intentClose.setAction("Close");
                        intentClose.putExtra("incID", incID);
                        intentClose.putExtra("notifID", Integer.parseInt(timestamp.toString()));
                        resultPendingIntent = PendingIntent.getActivity(getBaseContext(),
                                0,
                                intentClose,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getBaseContext())
                                .setSmallIcon(R.drawable.app_icon)
                                .setContentTitle("Help! Emergency!")
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Tap here to cancel your emergency help request."))
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVibrate(vibrate_ptn);
                        //.setOngoing(true);
                        //.setAutoCancel(true)
                        mBuilder.setContentIntent(resultPendingIntent);
                        nm.notify(Integer.parseInt(timestamp.toString()), mBuilder.build());

                        //save incident ID to shared preferences
                        SharedPreferences pref = getSharedPreferences("MyPref", 1); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();
                        editor.remove("incID");
                        editor.putString("incID", incID);
                        editor.commit();

                        //create dialog to help give more details
                        helpOnTheWay();
                    }
                    catch(Exception r){
                        r.printStackTrace();
                    }
                }
                else{
                    Snackbar.make(layoutHome, "Error sending for help. Please try again.", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }
            }catch(Exception r){
                r.printStackTrace();
            }
        }
    }

    class pingServer extends AsyncTask<String, String, String> {

        public pingServer(String status) {
            //get stored phone number
            SharedPreferences pref = getSharedPreferences("MyPref", 1); // 0 - for private mode
            String myphone = pref.getString("phone", null);

            //get current timestamp
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();

            System.out.println("My phone: "+myphone);

            details = new ArrayList<NameValuePair>();
            details.add(new BasicNameValuePair("phone", myphone));
            details.add(new BasicNameValuePair("status", status));
            details.add(new BasicNameValuePair("time", ts));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show progress dialog
            dialog = new ProgressDialog(Home.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("One moment...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(Config.SERVER_URL+"trip",
                        "POST", details);

                // check log cat fro response
                Log.d("Create Response", json.toString());

                // check for success tag
                //success = json.getString("status");
                response = json.getString("status");
                System.out.println(response);

            } catch (Exception e) {
                e.printStackTrace();
                response = "Connection error. Please try again";
            }
            return response;
        }

        protected void onPostExecute(String getResponse) {
            // dismiss the dialog once done
            try{
                dialog.dismiss();
                getResponse = response;
                if(getResponse.equals("ok")){
                    try{
                        Snackbar.make(layoutHome, "Trip confirmed. Enjoy your trip and don't forget to press the trip button again once you return", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener(){

                            @Override
                            public void onClick(View v) {

                            }
                        }).show();

                        if(status.equals("Leaving")){
                            btnTrip.setText("Press this again to confirm return from trip");
                            btnTrip.setTextColor(Color.parseColor("#2ECC71"));
                        }
                        else{
                            btnTrip.setText("Sail out");
                            btnTrip.setTextColor(Color.parseColor("#FFFFFF"));
                        }
                    }
                    catch(Exception r){
                        r.printStackTrace();
                    }
                }
                else{
                    Snackbar.make(layoutHome, "Error sending for help. Please try again.", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }
            }catch(Exception r){
                r.printStackTrace();
            }
        }
    }

    class cancelDanger extends AsyncTask<String, String, String> {

        public cancelDanger(String incidentID) {

            //get stored incident ID
            SharedPreferences pref = getSharedPreferences("MyPref", 1); // 0 - for private mode
            String theincID = pref.getString("incID", null);

            System.out.println("My incident ID: "+theincID);

            details = new ArrayList<NameValuePair>();
            details.add(new BasicNameValuePair("incident_id", theincID));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show progress dialog
            dialog = new ProgressDialog(Home.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("One moment...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(Config.SERVER_URL+"canceldanger",
                        "POST", details);

                // check log cat fro response
                Log.d("Create Response", json.toString());

                // check for success tag
                //success = json.getString("status");
                response = json.getString("status");
                System.out.println(response);

            } catch (Exception e) {
                e.printStackTrace();
                response = "Connection error. Please try again";
            }
            return response;
        }

        protected void onPostExecute(String getResponse) {
            // dismiss the dialog once done
            try{
                dialog.dismiss();
                getResponse = response;
                if(getResponse.equals("ok")){
                    try{
                        Toast.makeText(getBaseContext(), "Emergency alarm cancelled", Toast.LENGTH_LONG).show();
                    }
                    catch(Exception r){
                        r.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(getBaseContext(), "Problem retrieving people in danger. Please try again.", Toast.LENGTH_LONG).show();
                }
            }catch(Exception r){
                r.printStackTrace();
            }
        }
    }

    class getInDanger extends AsyncTask<String, String, String> {

        public getInDanger() {

            details = new ArrayList<NameValuePair>();
            details.add(new BasicNameValuePair("getpeople", "getpeople"));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show progress dialog
            dialog = new ProgressDialog(Home.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("One moment...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(Config.SERVER_URL+"indanger",
                        "GET", details);

                // check log cat fro response
                Log.d("Create Response", json.toString());

                // check for success tag
                //success = json.getString("status");
                response = json.getString("results");
                System.out.println(response);

            } catch (Exception e) {
                e.printStackTrace();
                response = "Connection error. Please try again";
            }
            return response;
        }

        protected void onPostExecute(String getResponse) {
            // dismiss the dialog once done
            try{
                dialog.dismiss();
                getResponse = response;
                if(getResponse.length()>0){
                    try{
                        //String [] coords = response.replace("[", "").replace("]", "").split(",");
                        //System.out.println("First one: "+coords[0].toString());
                        Intent map = new Intent(getBaseContext(), Map.class);
                        map.putExtra("coordinates", response);
                        startActivity(map);
                    }
                    catch(Exception r){
                        r.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(getBaseContext(), "Problem retrieving people in danger. Please try again.", Toast.LENGTH_LONG).show();
                }
            }catch(Exception r){
                r.printStackTrace();
            }
        }
    }

    public void helpOnTheWay(){
        try{
            //show dialog to confirm button press
            AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
            myDialog.setTitle("Help is on the way!");
            myDialog.setMessage("Are you able to add more details about your problem?");

            myDialog.setPositiveButton("Give details", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Add details
                    Intent details = new Intent(getBaseContext(), Details.class);
                    details.putExtra("coords", coords.toString());
                    startActivity(details);
                }
            });
            myDialog.setNegativeButton("JUST SAVE ME!", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = myDialog.create();
            alertDialog.show();
        }
        catch(Exception r){
            r.printStackTrace();
        }
    }
}
