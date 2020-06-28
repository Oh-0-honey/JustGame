package com.example.justgame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RoomActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    ListView entry_list, game_list;
    AlertDialog game_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        entry_list=(ListView)findViewById(R.id.entry_listview);
        game_list=(ListView)findViewById(R.id.game_listview);


        game_list.setOnItemClickListener(this);
        entry_list.setOnItemLongClickListener(this);

        String[] game_list_datas=getResources().getStringArray(R.array.Games);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.game_item,R.id.game_name,game_list_datas);
        game_list.setAdapter(adapter);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        if(view==game_list){
            switch (position){
                case 0:
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                    View v=inflater.inflate(R.layout.game_dialog,null);
                    builder.setView(v);
                    break;
                case 1:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }
}
