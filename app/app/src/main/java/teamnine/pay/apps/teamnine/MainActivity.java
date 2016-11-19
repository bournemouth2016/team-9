package teamnine.pay.apps.teamnine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnRegister;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);

        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

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
                Intent home = new Intent(this, Home.class);
                startActivity(home);
            }
            catch(Exception r){
                r.printStackTrace();
            }
        }
    }
}
