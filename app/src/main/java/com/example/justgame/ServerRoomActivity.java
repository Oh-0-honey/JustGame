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

import android.text.format.Formatter;
import android.widget.Toast;

public class ServerRoomActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView entry_list, game_list;
    private AlertDialog game_dialog;
    private ImageView QRcode;

    private String room_ip;
    private final int PORT=7070;

    private Socket socket;
    private DataOutputStream writeSocket;
    private DataInputStream readSocket;
    private Handler serverHandler = new Handler();
    private ConnectivityManager connectManager;
    private ServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_room);

        entry_list=(ListView)findViewById(R.id.entry_listview);
        game_list=(ListView)findViewById(R.id.game_listview);
        QRcode=(ImageView)findViewById(R.id.QRcode);

        game_list.setOnItemClickListener(this);
        entry_list.setOnItemLongClickListener(this);

        //서버 생성
        new SetServer().start();

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
        Log.d("IP_ADDRESS", "######### "+room_ip+" #########");

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

        //함수화 하자
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
                serverSocket = new ServerSocket(PORT);

                serverHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("SERVER", "@@@@@@@@@ server open success @@@@@@@@@");
                        Log.d("SERVER", "@@@@@@@@@ PORT : "+PORT+" @@@@@@@@@");
                    }
                });

                socket = serverSocket.accept();
                writeSocket = new DataOutputStream(socket.getOutputStream());
                readSocket  = new DataInputStream(socket.getInputStream());

                serverHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("SERVER", "@@@@@@@@@ IP_ADDRESS : "+room_ip+" @@@@@@@@@");
                    }
                });

                while(true){
                    byte[] bytes = new byte[100];
                    int ac=readSocket.read(bytes,0,bytes.length);
                    String input = new String(bytes, 0, bytes.length);
                    final String recvInput = input.trim();
                    if(ac == -1) break;
                }

                serverSocket.close();
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Server","!!!!!!!!!!!!!!! server open fail !!!!!!!!!!!!!!!");
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
                            Log.d("SERVER", "@@@@@@@@@ server close success @@@@@@@@@");
                        }
                    });
                }
            } catch (Exception e){

            }
        }
    }

    public void setToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT);
    }
}
