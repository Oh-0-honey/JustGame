package com.example.justgame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientRoomActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView entry_list, game_list;
    private AlertDialog game_dialog;
    private ImageView QRcode;

    private String room_ip;
    private final int PORT = 7070;

    private Socket socket;
    private DataOutputStream writeSocket;
    private DataInputStream readSocket;
    private Handler serverHandler = new Handler();
    private ConnectivityManager connectManager;
    private ServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_room);

        entry_list = (ListView) findViewById(R.id.entry_listview);
        game_list = (ListView) findViewById(R.id.game_listview);
        QRcode = (ImageView) findViewById(R.id.QRcode);

        game_list.setOnItemClickListener(this);
        entry_list.setOnItemLongClickListener(this);

        room_ip = getIntent().getStringExtra("room_ip");

        //QR코드 생성
        MultiFormatWriter mfw_QRcode = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix=mfw_QRcode.encode(room_ip, BarcodeFormat.QR_CODE,120,120);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap QR_bitmap = barcodeEncoder.createBitmap(bitMatrix);
            QRcode.setImageBitmap(QR_bitmap);
        }catch (Exception e){
            // 예외처리 없음
        }
        
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
}