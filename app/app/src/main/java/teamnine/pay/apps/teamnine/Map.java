package teamnine.pay.apps.teamnine;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kenneth on 11/19/16.
 */

public class Map extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    GoogleMap mMap;

    GoogleApiClient mGoogleApiClient;
    LocationManager locationManager;

    double myLat;
    double myLong;

    Marker myLocation;

    ArrayList<Double> coords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        setTitle("People in danger");


        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

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

            //add my location to search array for directions
            //mySearchedNames.add("My current location");
            //mySearchedLat.add(0.0);
            //mySearchedLong.add(0.0);

        } catch (Exception r) {
            Toast.makeText(this, "Sorry your device does not support the map feature", Toast.LENGTH_LONG).show();
            r.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        if(myLocation!=null){
            myLocation.remove();
        }
        LatLng place = new LatLng(location.getLatitude(), location.getLongitude());
        myLocation = mMap.addMarker(new MarkerOptions().position(place).title("You are here :-)"));
        myLocation.setPosition(place);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            List<String> permissions = new ArrayList<String>();
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.VIBRATE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions( permissions.toArray( new String[permissions.size()] ), 101 );
            }
            //return;
        }else{
            locationManager.removeUpdates(this);
        }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mMap = googleMap;

            mMap.setMyLocationEnabled(true);
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                System.out.println("Hooray I reached here!!");
                double lat = mLastLocation.getLatitude();
                double lon = mLastLocation.getLongitude();

                myLat = lat;
                myLong = lon;

                //check if lat and long are 0.0
                if(myLat==0.0 && myLong==0.0){
                    //buildAlertMessageNoGps();
                }
                else{
                    LatLng myLoc = new LatLng(lat, lon);
                    //mMap.clear();
                    if(myLocation!=null){
                        myLocation.remove();
                    }
                    myLocation = mMap.addMarker(new MarkerOptions().position(myLoc).title("You are here :-)"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

                }
            } else {
                //plan b
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
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
                    if(!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )){
                        //buildAlertMessageNoGps();
                    }
                    else{
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 100, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
                    }
                }

                //TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                //System.out.println("My country is: " + tm.getNetworkCountryIso().toString());

                //LatLng place = new LatLng(-1.3047996, 36.7069645);
                //mMap.addMarker(new MarkerOptions().position(place).title("Marker in Nairobi"));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            }

        } catch (Exception r) {
            r.printStackTrace();
        }
    }
}
