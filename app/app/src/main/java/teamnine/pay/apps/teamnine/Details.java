package teamnine.pay.apps.teamnine;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import teamnine.pay.apps.teamnine.connectors.Config;
import teamnine.pay.apps.teamnine.connectors.JSONParser;

/**
 * Created by kenneth on 11/19/16.
 */

public class Details extends AppCompatActivity implements View.OnClickListener{

    Button btnCancel;

    List<NameValuePair> details;

    String response;
    String status;
    String position;

    JSONParser jsonParser = new JSONParser();

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);
        setTitle("Emergency details");

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        if(getIntent().getStringExtra("coords")!=null){
            position = getIntent().getStringExtra("coords").toString();
        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnCancel){
            new cancelDanger().execute();
        }
    }

    class cancelDanger extends AsyncTask<String, String, String> {

        public cancelDanger() {

            //get stored incident id
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
            dialog = new ProgressDialog(Details.this);
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
                        Intent home = new Intent(getBaseContext(), Home.class);
                        startActivity(home);
                        finish();
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
}
