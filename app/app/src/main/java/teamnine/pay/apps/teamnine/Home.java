package teamnine.pay.apps.teamnine;

import android.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
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

    List<NameValuePair> details;

    String response;

    JSONParser jsonParser = new JSONParser();

    ProgressDialog dialog;

    ArrayList<Double> coords = new ArrayList<>();

    LinearLayout layoutHome;

    Button btnRescue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        setTitle("Home");

        panicButton = (ImageView) findViewById(R.id.imgPanic);
        panicButton.setOnClickListener(this);

        btnRescue = (Button) findViewById(R.id.btnRescue);
        btnRescue.setText("People in danger: 0");
        btnRescue.setOnClickListener(this);

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
                    requestPermissions( permissions.toArray( new String[permissions.size()] ), 101 );
                }
                //return;
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 100, this);
            }
        }
        catch(Exception r){
            r.printStackTrace();
        }

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
                        //get lat and long
                        getCoordinates();

                        System.out.println("Coordinates: "+coords.toString());

                        new sendEmergencyNotif(coords).execute();
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
    }

    @Override
    public void onLocationChanged(Location location) {
        //System.out.println("Latitude is: "+location.getLatitude());
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

    public void getCoordinates(){
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
                requestPermissions( permissions.toArray( new String[permissions.size()] ), 101 );
            }
            //return;
        }else{
            System.out.println("Hooray I ALSO reached here!!");
            //check if user has activate location/GPS
            /*if(!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )){
                buildAlertMessageNoGps();
            }*/
            //else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 100, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER

            //}
        }
    }
}
