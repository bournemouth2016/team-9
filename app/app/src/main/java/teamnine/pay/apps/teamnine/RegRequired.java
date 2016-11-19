package teamnine.pay.apps.teamnine;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

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

public class RegRequired extends Fragment implements View.OnClickListener{

    EditText enterfname, enterlname, enterphone, enterboatname, enterpassword, enterrpassword;

    Button btnRegister;

    LinearLayout layoutRegreq;

    int success;

    ProgressDialog dialog;

    List<NameValuePair> details;

    String response;

    JSONParser jsonParser = new JSONParser();

    //for GCM
    String ProjectNumber = "720117046048";
    GoogleCloudMessaging gcm;
    String registration_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.regrequired, container, false);

        enterfname = (EditText)rootView.findViewById(R.id.enterFName);
        enterlname = (EditText)rootView.findViewById(R.id.enterLName);
        enterphone = (EditText)rootView.findViewById(R.id.enterPhone);
        enterboatname = (EditText)rootView.findViewById(R.id.enterBoat);
        enterpassword = (EditText)rootView.findViewById(R.id.enterPass);
        enterrpassword = (EditText)rootView.findViewById(R.id.enterRPass);

        layoutRegreq = (LinearLayout)rootView.findViewById(R.id.layoutReq);

        btnRegister = (Button)rootView.findViewById(R.id.btnCRegister);
        btnRegister.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnCRegister){
            System.out.println("Register pressed!");
            try{
                //validate
                if(enterfname.getText().length()>0 && enterlname.getText().length()>0 && enterphone.getText().length()>0 &&
                        enterboatname.getText().length()>0 && enterpassword.getText().length()>0 && enterrpassword.getText().length()>0){

                    if(enterpassword.getText().toString().equals(enterrpassword.getText().toString())){
                        new registerMe(enterfname.getText().toString(), enterlname.getText().toString(), enterphone.getText().toString(), enterboatname.getText().toString(),
                                enterpassword.getText().toString()).execute();
                    }
                    else{
                        Snackbar.make(layoutRegreq, "Please ensure your passwords match", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener(){

                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                    }
                }
            }
            catch(Exception r){
                r.printStackTrace();
            }
        }
    }

    public void registerMe(String fname, String lname, String location, String boat, String password){

    }

    class registerMe extends AsyncTask<String, String, String> {

        public registerMe(String fname, String lname, String phone, String boat, String password) {

            details = new ArrayList<NameValuePair>();
            details.add(new BasicNameValuePair("owner_fname", fname));
            details.add(new BasicNameValuePair("owner_lname", lname));
            details.add(new BasicNameValuePair("location", ""));
            details.add(new BasicNameValuePair("boat_name", boat));
            details.add(new BasicNameValuePair("password", password));
            details.add(new BasicNameValuePair("phone", phone));
            details.add(new BasicNameValuePair("max_passengers", "0"));
            details.add(new BasicNameValuePair("vessel_type", ""));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show progress dialog
            dialog = new ProgressDialog(getActivity());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("One moment...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                //create GCM key
                gcm = GoogleCloudMessaging.getInstance(getActivity());
                registration_id = gcm.register(ProjectNumber);
                Log.d("GcmIntentService", "Device registered, registration ID=" + registration_id);
                System.out.println("GCM is: "+registration_id);

                details.add(new BasicNameValuePair("gcm_id", registration_id));

                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(Config.SERVER_URL+"register",
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
                        SharedPreferences pref = getActivity().getSharedPreferences("MyPref", 1); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("phone", enterphone.getText().toString());
                        editor.commit();

                        System.out.println("Phone is saved: "+enterphone.getText().toString());

                        Intent home = new Intent(getActivity(), Home.class);
                        startActivity(home);
                    }
                    catch(Exception r){
                        r.printStackTrace();
                    }
                }
                else{
                    Snackbar.make(layoutRegreq, "Error during registration. Please try again.", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener(){

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
