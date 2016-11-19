package teamnine.pay.apps.teamnine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by kenneth on 11/19/16.
 */

public class Details extends AppCompatActivity implements View.OnClickListener{

    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);
        setTitle("Emergency details");

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

    }
}
