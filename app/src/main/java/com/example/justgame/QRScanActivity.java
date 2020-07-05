package com.example.justgame;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScanActivity extends AppCompatActivity {
    private IntentIntegrator qrScan;
    TextView text_result;
    String scan_result;

    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        user_id=getIntent().getStringExtra("user_id");
        qrScan=new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        qrScan.setPrompt(getResources().getString(R.string.scan_QR));
        qrScan.initiateScan();

        text_result=(TextView)findViewById(R.id.Scan_test);
    }

    //스캔 결과
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() == null){
                setToast(getResources().getString(R.string.not_scanned));
                finish();
            } else{
                scan_result = result.getContents();

                Intent intent =new Intent(getApplication(), ClientRoomActivity.class);
                intent.putExtra("room_info",scan_result);
                intent.putExtra("user_id",user_id);
                startActivity(intent);
                finish();
            }
            text_result.setText(scan_result);
        } else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void setToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}