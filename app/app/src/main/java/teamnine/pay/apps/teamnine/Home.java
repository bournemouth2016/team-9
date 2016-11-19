package teamnine.pay.apps.teamnine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by kenneth on 11/18/16.
 */

public class Home extends AppCompatActivity implements View.OnClickListener{

    ImageView panicButton;

    Button btnRescue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        setTitle("Home");

        panicButton = (ImageView)findViewById(R.id.imgPanic);
        panicButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.imgPanic){
            try{
                //show dialog to confirm button press
                AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
                myDialog.setTitle("Reset password");

                myDialog.setPositiveButton("Reset password", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
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
}
