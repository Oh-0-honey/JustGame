package com.example.justgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    Button login;
    AutoCompleteTextView id_text;
    TextView pw_text;
    CheckBox auto_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login=(Button)findViewById(R.id.login);
        id_text=(AutoCompleteTextView)findViewById(R.id.id_text);
        pw_text=(TextView)findViewById(R.id.pw_text);
        auto_login=(CheckBox)findViewById(R.id.auto_login);

        login.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v==login){
            if(id_text.getText().toString().isEmpty() || pw_text.getText().toString().isEmpty()){
                showSnack(R.string.empty_idpw);
            } else {
                startActivity(new Intent(getApplication(),MainActivity.class));
                finish();
            }
        }
    }


    public void showSnack(int stringId){
        Snackbar.make(getWindow().getDecorView().getRootView(), stringId, Snackbar.LENGTH_SHORT).show();
    }
}
