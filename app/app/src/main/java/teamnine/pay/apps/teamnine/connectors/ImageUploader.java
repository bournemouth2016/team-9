package teamnine.pay.apps.teamnine.connectors;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;

/**
 * Created by kenneth on 4/11/16.
 */
public class ImageUploader {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    HttpEntity resEntity;

    // constructor
    public ImageUploader() {

    }


    public JSONObject makeImageUploadRequest(String url, File imgFile,
                                      String fname, String lname, String email, String pass, String dob, String country,
                                             String occ, String gender) {

        try
        {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            FileBody bin1 = new FileBody(imgFile);
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("photo", bin1);
            reqEntity.addPart("fname", new StringBody(fname));
            reqEntity.addPart("lname", new StringBody(lname));
            reqEntity.addPart("email", new StringBody(email));
            reqEntity.addPart("password", new StringBody(pass));
            reqEntity.addPart("gender", new StringBody(gender));
            reqEntity.addPart("country", new StringBody(country));
            reqEntity.addPart("dob", new StringBody(dob));
            reqEntity.addPart("occ", new StringBody(occ));
            post.setEntity(reqEntity);
            HttpResponse response = client.execute(post);
            resEntity = response.getEntity();
            final String response_str = EntityUtils.toString(resEntity);
            if (resEntity != null) {
                json = response_str;
                System.out.println("Response from server: "+response_str);
            }
            else{
                json = "Sorry an error occurred";
            }
            jObj = new JSONObject(json);
        }
        catch (Exception ex){
            Log.e("Debug", "error: " + ex.getMessage(), ex);
        }
        return jObj;
    }
}
