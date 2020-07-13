package com.example.justgame;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.justgame.game.FindmeActivity;

public class GameDialog extends Dialog{
    private View.OnClickListener clickListener_;
    private TextView game_title_;
    private TextView game_introduction_;
    private ImageView game_img_;
    private Button start_btn_;
    private Class game_activity;
    private Context context;

    private String title_, intro_;
    private int image_id_;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount=0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.game_dialog);
        game_title_=(TextView)findViewById(R.id.game_title);
        game_introduction_=(TextView)findViewById(R.id.game_introduction);
        game_img_=(ImageView)findViewById(R.id.game_img);
        start_btn_=(Button)findViewById(R.id.start_btn);

        game_title_.setText(title_);
        game_introduction_.setText(intro_);
        game_img_.setImageResource(image_id_);

        start_btn_.setOnClickListener(clickListener_);
    }

    public GameDialog(Context context, String title, View.OnClickListener clickListener){
        super(context,android.R.style.Theme_Translucent_NoTitleBar);
        this.context=context;
        switch (title){
            case "find me":
                title_ = (context.getResources().getString(R.string.find_me));
                intro_ = (context.getResources().getString(R.string.find_me_intro));
                image_id_ = R.drawable.splash_test;
                game_activity= FindmeActivity.class;
                clickListener_ = clickListener;
                break;
            case "mafia":
                game_title_.setText(context.getResources().getString(R.string.mafia));
                break;
            default:
                break;
        }
    }

}
