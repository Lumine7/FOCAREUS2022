package com.huawei.audiodevicekit.bluetoothsample.view;

import androidx.appcompat.app.AppCompatActivity;
import com.huawei.audiodevicekit.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Activity3 extends AppCompatActivity {

    private Button btncreate;
    private Button btntonext31;
    private Button btntonext34;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
        btncreate=findViewById(R.id.btn_to_create);
        btncreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2=new Intent();
                intent2.setClass(Activity3.this,Activity2.class);
                startActivity(intent2);
            }
        });
        btntonext31=findViewById(R.id.btn_to_next_31);
        btntonext34=findViewById(R.id.btn_to_next_34);
        btntonext31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        btntonext34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Intent intent2 = new Intent();
                intent.setClass(Activity3.this,Activity4.class);
                startActivity(intent);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


}