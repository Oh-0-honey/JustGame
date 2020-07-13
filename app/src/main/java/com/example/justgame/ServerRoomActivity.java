package com.example.justgame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
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

import com.example.justgame.game.FindmeActivity;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

import android.text.format.Formatter;
import android.widget.TextView;
import android.widget.Toast;

public class ServerRoomActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView entry_list, game_list;
    private ArrayAdapter entry_list_adapter;
    private TextView entry_list_title;
    private ArrayList<String> entry;
    private GameDialog game_dialog;
    private ImageView QRcode;
    private String game_title;

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

    private final String SERVER = "SERVER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_room);

        user_id=getIntent().getStringExtra("user_id");

        entry_list=(ListView)findViewById(R.id.entry_listview);
        entry_list_title=(TextView)findViewById(R.id.entry_list_title);
        game_list=(ListView)findViewById(R.id.game_listview);
        QRcode=(ImageView)findViewById(R.id.QRcode);

        game_list.setOnItemClickListener(this);
        entry_list.setOnItemClickListener(this);


        setRoomInfo();
        makeQrCode();

        //함수화 하자

        InitList();




        //서버 생성
        new SetServer().start();

        //방장에게 필요한 기능
        /*
         * 특정 Activity 띄우기
         * 특정 참가자 강퇴
         * 타이머 설정
         */

    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        if(parent==game_list){
            switch (position){
                case 0:
                    game_title="find me";
                    game_dialog=new GameDialog(this,"find me", GameStartListener);
                    game_dialog.show();
                    break;
                case 1:
                    break;
                default:
                    break;
            }
        }
        if(parent==entry_list){
            switch (position){
                case 0:
                    SendMsg("Hello");
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



    //////////////////////////////////////////////////
    // 서버 열기
    class SetServer extends Thread{
        public void run(){
            try{
                serverSocket = new ServerSocket(room_port);

                serverHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(SERVER, "@@@@@@@@@ server open success @@@@@@@@@");
                        setToast("서버 오픈 성공");
                        isConnected=true;
                    }
                });
                //서버에 접속하는 클라 소켓 얻어오기
                socket      = serverSocket.accept();//클라이언트가 들어올 때까지 여기서 대기
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
                            String[] sign= recv_msg.split("/");
                            switch (sign[0]){
                                case "0":
                                    AddEntry(sign[1]);
                                    setToast(sign[1]+"님이 접속했습니다.");
                                    break;
                                case "1":
                                    RemoveEntry(sign[1]);
                                    setToast(sign[1]+"님이 나갔습니다.");
                                    break;
                            }

                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();

                }
            }
        }
    }

    //////////////////////////////////////////////////
    // 서버 닫기
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

    //////////////////////////////////////////////////
    // 메세지 전송
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
    // 게임 시작 버튼 클릭시 작동
    public View.OnClickListener GameStartListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SendMsg("2/"+game_title);
            Intent intent = null;
            switch (game_title){
                case "find me":
                    intent=new Intent(getApplication(), FindmeActivity.class);
                    intent.putExtra("number",entry_list_adapter.getCount());//인원수
                    intent.putExtra("entry_list","");
                    break;
                default:
                    break;
            }
            if(intent != null) startActivity(intent);
        }
    };

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

    //////////////////////////////////////////////////
    // 게임 리스트, 참가자 리스트 초기화
    public void InitList(){
        String[] game_list_datas=getResources().getStringArray(R.array.Games);
        ArrayAdapter<String> gamelist_adapter=new ArrayAdapter<String>(this,R.layout.game_item,R.id.game_name,game_list_datas);
        game_list.setAdapter(gamelist_adapter);

        entry = new ArrayList<String>();
        entry.add(user_id);
        entry_list_adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,entry);
        entry_list.setAdapter(entry_list_adapter);
    }


    //////////////////////////////////////////////////
    // IP 정보, port 정보 등을 담는 과정
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

    //////////////////////////////////////////////////
    //IP 정보 얻어오기
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
}
