package teamnine.pay.apps.teamnine;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import teamnine.pay.apps.teamnine.connectors.Config;
import teamnine.pay.apps.teamnine.connectors.JSONParser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnRegister;
    Button btnLogin;

    List<NameValuePair> details;

    String response;

    JSONParser jsonParser = new JSONParser();

    ProgressDialog dialog;

    String myphone;

    LinearLayout layoutMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);

        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        layoutMain = (LinearLayout)findViewById(R.id.activity_main);

        try{
            //Check if logged in
            SharedPreferences pref = getSharedPreferences("MyPref", 1); // 0 - for private mode
            String myphone = pref.getString("phone", null);

            if(myphone.length()>0 || myphone!=null){
                Intent home = new Intent(this, Home.class);
                startActivity(home);
            }
        }
        catch(Exception r){
            r.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnRegister){
            try{
                Intent register = new Intent(this, Register.class);
                startActivity(register);
            }
            catch(Exception r){
                r.printStackTrace();
            }
        }
        if(v.getId()==R.id.btnLogin){
            try{
                loginPopup();
            }
            catch(Exception r){
                r.printStackTrace();
            }
        }
    }

    public void loginPopup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Login");
        builder.setMessage("Enter your username and password to log in");
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.login_popup, null);
        builder.setView(layout);

        final EditText phone = (EditText)layout.findViewById(R.id.enterLPhone);
        final EditText pass = (EditText)layout.findViewById(R.id.enterLPass);

        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myphone = phone.getText().toString();
                new checkLogin(phone.getText().toString(), pass.getText().toString()).execute();
            }
        });

        builder.setNegativeButton("Close", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    class checkLogin extends AsyncTask<String, String, String> {

        public checkLogin(String phone, String password) {

            details = new ArrayList<NameValuePair>();
            details.add(new BasicNameValuePair("phone", phone));
            details.add(new BasicNameValuePair("password", password));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show progress dialog
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("One moment...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(Config.SERVER_URL+"login",
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
                        //Store phone number in sharedpref session
                        SharedPreferences pref = getSharedPreferences("MyPref", 1); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("phone", myphone);
                        editor.commit();

                        System.out.println("Phone is saved: "+myphone);

                        Intent home = new Intent(getBaseContext(), Home.class);
                        startActivity(home);
                    }
                    catch(Exception r){
                        r.printStackTrace();
                    }
                }
                else{
                    Snackbar.make(layoutMain, "Error during registration. Please try again.", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener(){

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
}
