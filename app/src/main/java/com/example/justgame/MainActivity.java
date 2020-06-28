package com.example.justgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView profile_img;
    Button enter_btn, create_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile_img=(ImageView)findViewById(R.id.profile_img);
        enter_btn=(Button)findViewById(R.id.enter_room);
        create_btn=(Button)findViewById(R.id.create_room);

        enter_btn.setOnClickListener(this);
        create_btn.setOnClickListener(this);


        //프로필 둥글게
        profile_img.setBackground(new ShapeDrawable(new OvalShape()));
        profile_img.setClipToOutline(true);
    }


    @Override
    public void onClick(View v){
        if(v==enter_btn){
            //QR코드 스캔 카메라 기능 필요
        }
        if(v==create_btn){
            startActivity(new Intent(getApplication(),RoomActivity.class));
            finish();
        }
    }
}
