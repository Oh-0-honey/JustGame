package com.example.justgame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientRoomActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView entry_list, game_list;
    private AlertDialog game_dialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_room);

        user_id=getIntent().getStringExtra("user_id");

        entry_list = (ListView) findViewById(R.id.entry_listview);
        game_list = (ListView) findViewById(R.id.game_listview);
        QRcode = (ImageView) findViewById(R.id.QRcode);

        game_list.setOnItemClickListener(this);
        entry_list.setOnItemLongClickListener(this);

        room = getIntent().getStringExtra("room_info").split("/"); // ip/port/nickname
        makeQrCode();

        new Connect().start();
        
    }




    @Override
    protected void onDestroy(){
        super.onDestroy();
        new Disconnect().start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view == game_list) {
            switch (position) {
                case 0:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.game_dialog, null);
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


    class Connect extends Thread{
        public void run(){
            try {
                socket      = new Socket(room[0],Integer.parseInt(room[1]));
                writeSocket = new DataOutputStream(socket.getOutputStream());
                readSocket  = new DataInputStream(socket.getInputStream());

                clientHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(CLIENT, "@@@@@@@@@ client connect success @@@@@@@@@");
                        setToast(room[2]+"님 방에 접속했습니다.");
                    }
                });
                SendMsg(user_id+"님이 접속 했습니다.");

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
            while(true){
                try {
                    recv_msg = readSocket.readUTF();
                    if(recv_msg.equals("OUT")) break;

                    clientHandler.post(new Runnable() {
                        @Override
                        public void run() {

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
                    socket.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void SendMsg(String send_msg){
        if(writeSocket == null) return;
        final String msg = send_msg;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    writeSocket.writeUTF(msg);
                    writeSocket.flush();

                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
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
            Log.d(CLIENT, room[0]+" / "+room[1]+" / "+room[2]+" 방 접속 시도");
            QRcode.setImageBitmap(QR_bitmap);
        }catch (Exception e){
            // 예외처리 없음
        }
    }
}