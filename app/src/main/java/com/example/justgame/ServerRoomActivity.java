package com.example.justgame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import android.text.format.Formatter;
import android.widget.Toast;

public class ServerRoomActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView entry_list, game_list;
    private AlertDialog game_dialog;
    private ImageView QRcode;

    private String room_ip, user_id;
    private int room_port;

    private Socket socket;
    private DataOutputStream writeSocket;
    private DataInputStream readSocket;
    private Handler serverHandler = new Handler();
    private ConnectivityManager connectManager;
    private ServerSocket serverSocket;
    private String recv_msg;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_room);

        user_id=getIntent().getStringExtra("user_id");

        entry_list=(ListView)findViewById(R.id.entry_listview);
        game_list=(ListView)findViewById(R.id.game_listview);
        QRcode=(ImageView)findViewById(R.id.QRcode);

        game_list.setOnItemClickListener(this);
        entry_list.setOnItemLongClickListener(this);

        //서버 생성
        new SetServer().start();

        setRoomInfo();
        makeQrCode();

        //함수화 하자
        String[] game_list_datas=getResources().getStringArray(R.array.Games);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.game_item,R.id.game_name,game_list_datas);
        game_list.setAdapter(adapter);

        //방장에게 필요한 기능
        /*
         * 특정 Activity 띄우기
         * 특정 참가자 강퇴
         * 타이머 설정
         */

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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        new CloseServer().start();
    }

    public static String getIpAddress(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e){
            e.printStackTrace();
        }
        return null;
    }

    class SetServer extends Thread{
        public void run(){
            try{
                serverSocket = new ServerSocket(room_port);

                serverHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("SERVER", "@@@@@@@@@ server open success @@@@@@@@@");
                        setToast("서버 오픈 성공");
                        isConnected=true;
                    }
                });

                socket      = serverSocket.accept();
                writeSocket = new DataOutputStream(socket.getOutputStream());
                readSocket  = new DataInputStream(socket.getInputStream());

            } catch (Exception e) {
                e.printStackTrace();

                serverHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Server","!!!!!!!!!!!!!!! server open fail !!!!!!!!!!!!!!!");
                        setToast(getResources().getString(R.string.server_open_fail));
                        Finish_in_Thread();
                    }
                });
            }

            while(isConnected){
                try {
                    recv_msg = readSocket.readUTF();
                    serverHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setToast(recv_msg);
                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    class CloseServer extends Thread{
        public void run(){
            try{
                if(serverSocket != null){
                    serverSocket.close();;
                    socket.close();
                    serverHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            isConnected=false;
                            Log.d("SERVER", "@@@@@@@@@ server close success @@@@@@@@@");
                        }
                    });
                }
            } catch (Exception e){
                e.printStackTrace();
                serverHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("SERVER", "!!!!!!!!! server close fail !!!!!!!!!");

                    }
                });
            }
        }
    }



    public void setToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    void Finish_in_Thread(){ finish(); }

    private void makeQrCode() {
        //QR코드 생성(server 기준)
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

    private void setRoomInfo() {
        connectManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiInfo.isConnected()) {
            WifiInfo info = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
            room_ip = Formatter.formatIpAddress(info.getIpAddress());
            Log.d("IP_ADDRESS", "######### Wifi #########");
        } else {
            room_ip=getIpAddress();
            Log.d("IP_ADDRESS", "######### Ethernet #########");
        }

        if(room_ip == null) setToast("인터넷 연결 후 다시 방을 만들어주세요.");
        else{
            room_port= new Random().nextInt(2000)+7000;
            room_ip += "/"+room_port+"/"+user_id;
        }
        Log.d("IP_ADDRESS", "######### "+room_ip+" #########");
    }
}
