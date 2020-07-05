package com.example.justgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView profile_img;
    TextView profile_id;
    Button enter_btn, create_btn;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile_img=(ImageView)findViewById(R.id.profile_img);
        enter_btn=(Button)findViewById(R.id.enter_room);
        create_btn=(Button)findViewById(R.id.create_room);
        profile_id=(TextView)findViewById(R.id.profile_id);
        user_id =getIntent().getStringExtra("user_id");

        profile_img.setOnClickListener(this);
        enter_btn.setOnClickListener(this);
        create_btn.setOnClickListener(this);

        profile_id.setText(user_id);

        //프로필 둥글게
        profile_img.setBackground(new ShapeDrawable(new OvalShape()));
        profile_img.setClipToOutline(true);

    }


    @Override
    public void onClick(View v){
        if(v==profile_img){
            //다이얼로그

        }
        else if(v==enter_btn){
            Intent intent =new Intent(getApplication(), QRScanActivity.class);
            intent.putExtra("user_id",profile_id.getText().toString());
            startActivity(intent);
        }
        else if(v==create_btn){
            Intent intent =new Intent(getApplication(), ServerRoomActivity.class);
            intent.putExtra("user_id",profile_id.getText().toString());
            startActivity(intent);
        }
    }
}
