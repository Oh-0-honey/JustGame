package com.example.justgame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.justgame.game.FindmeActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ClientRoomActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView entry_list, game_list;
    private ArrayAdapter entry_list_adapter;
    private TextView entry_list_title;
    private ArrayList<String> entry;
    private GameDialog game_dialog;
    private ImageView QRcode;

    private String[] room;
    private String user_id;
    private final String CLIENT="CLIENT";
    private final int PORT = 7070;

    private Socket socket;
    private DataOutputStream writeSocket;
    private DataInputStream readSocket;
    private Handler clientHandler = new Handler();
    private ConnectivityManager connectManager;
    private String recv_msg;
    private boolean isConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_room);

        user_id=getIntent().getStringExtra("user_id");

        entry_list = (ListView) findViewById(R.id.entry_listview);
        entry_list_title=(TextView)findViewById(R.id.entry_list_title);
        game_list = (ListView) findViewById(R.id.game_listview);
        QRcode = (ImageView) findViewById(R.id.QRcode);

        game_list.setOnItemClickListener(this);
        entry_list.setOnItemLongClickListener(this);

        room = getIntent().getStringExtra("room_info").split("/"); // ip/port/nickname
        makeQrCode();
        InitList();


        new Connect().start();
        
    }




    @Override
    protected void onDestroy(){
        super.onDestroy();
        new Disconnect().start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view == game_list) {/*
            switch (position) {
                case 0:
                    game_dialog = new GameDialog(this, "find me");
                    game_dialog.show();
                    break;
                case 1:
                    break;
                default:
                    break;
            }
            */
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }


    class Connect extends Thread{
        public void run(){
            try {
                socket      = new Socket(room[0],Integer.parseInt(room[1]));
                writeSocket = new DataOutputStream(socket.getOutputStream());
                readSocket  = new DataInputStream(socket.getInputStream());
                isConnected = true;

                clientHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(CLIENT, "@@@@@@@@@ client connect success @@@@@@@@@");
                        Log.d(CLIENT, writeSocket.toString()+"\n");
                        Log.d(CLIENT, readSocket.toString()+"\n");
                        Log.d(CLIENT, socket.toString()+"\n");
                        AddEntry(room[2]);
                        AddEntry(user_id);
                        setToast(room[2]+"님 방에 접속했습니다.");
                        SendMsg("0/"+user_id);
                    }
                });


            } catch (Exception e){
                e.printStackTrace();
                clientHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(CLIENT, "!!!!!!!!!!! client connect fail !!!!!!!!!!!");
                        setToast(getResources().getString(R.string.client_open_fail));
                        Finish_in_Thread();
                    }
                });
            }
            while(isConnected){
                try {
                    /*
                    byte[] b=new byte[100];
                    int ac = readSocket.read(b, 0, b.length);
                    String in=new String(b,0,b.length);
                    recv_msg = in.trim();
                    */
                    recv_msg = readSocket.readUTF();
                    if(recv_msg.equals("OUT")){
                        isConnected=false;
                        break;
                    }

                    clientHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setToast(recv_msg);
                            ReadCommand(recv_msg);
                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    class Disconnect extends Thread{
        public void run(){
            try{
                if(socket != null){
                    SendMsg("1/"+user_id);
                    socket.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void ReadCommand(String recv){
        String[] command=recv.split("/");
        switch (command[0]){
            case "2"://게임 시작
                Intent intent=null;
                switch (command[1]){
                    case "find me":
                        intent = new Intent(getApplication(), FindmeActivity.class);
                        break;
                    default:
                        break;
                }
                if(intent != null) startActivity(intent);
                break;
            default:
                break;
        }
    }

    public void SendMsg(String send_msg){
        if(writeSocket == null) return;
        final String msg = send_msg;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //UTF면 한글도 전송 가능
                    writeSocket.writeUTF(msg);
                    writeSocket.flush();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //////////////////////////////////////////////////
    // 참가자 목록 추가 및 제거
    public void AddEntry(String client_id){
        entry.add(client_id);
        entry_list_title.setText(getResources().getString(R.string.entry_list)+" ("+entry_list_adapter.getCount()+"명)");
        entry_list_adapter.notifyDataSetChanged();
    }
    public void RemoveEntry(String client_id){
        entry.remove(client_id);
        entry_list_title.setText(getResources().getString(R.string.entry_list)+" ("+entry_list_adapter.getCount()+"명)");
        entry_list_adapter.notifyDataSetChanged();
    }

    public void setToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    void Finish_in_Thread(){ finish(); }
    private void makeQrCode() {
        //QR코드 생성(Server 기준)
        MultiFormatWriter mfw_QRcode = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix=mfw_QRcode.encode(room[0]+"/"+room[1]+"/"+room[2], BarcodeFormat.QR_CODE,120,120);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap QR_bitmap = barcodeEncoder.createBitmap(bitMatrix);
            QRcode.setImageBitmap(QR_bitmap);
        }catch (Exception e){
            // 예외처리 없음
        }
    }


    //////////////////////////////////////////////////
    // 게임 리스트, 참가자 리스트 초기화
    public void InitList(){
        String[] game_list_datas=getResources().getStringArray(R.array.Games);
        ArrayAdapter<String> gamelist_adapter=new ArrayAdapter<String>(this,R.layout.game_item,R.id.game_name,game_list_datas);
        game_list.setAdapter(gamelist_adapter);

        entry = new ArrayList<String>();
        entry_list_adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,entry);
        entry_list.setAdapter(entry_list_adapter);
    }
}