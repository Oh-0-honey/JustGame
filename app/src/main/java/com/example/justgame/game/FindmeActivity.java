package com.example.justgame.game;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.justgame.R;

public class FindmeActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView[] user_img;
    private Animation[] animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findme);


        animation=new Animation[6];
        user_img=new ImageView[6];
        for(int i=0;i<6;i++){
            user_img[i]=(ImageView)findViewById(R.id.user_img1+i);
            user_img[i].setOnClickListener(this);
            user_img[i].setBackground(new ShapeDrawable(new OvalShape()));
            user_img[i].setClipToOutline(true);


        }
        animation[0] = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.first_impression1);
        animation[1] = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.first_impression2);
        animation[2] = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.first_impression3);
        TranslateImg();


    }

    @Override
    public void onClick(View v) {

    }

    public void TranslateImg(){
        for(int i=0;i<3;i++) user_img[i].startAnimation(animation[i]);
    }
}