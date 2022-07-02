package com.huawei.audiodevicekit.bluetoothsample.view;

import androidx.appcompat.app.AppCompatActivity;
import com.huawei.audiodevicekit.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Activity4 extends AppCompatActivity {

    private Button btntonext41;
    private Button btntonext43;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);
        btntonext41=findViewById(R.id.btn_to_next_41);
        btntonext43=findViewById(R.id.btn_to_next_43);
        btntonext41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        btntonext43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Intent intent2 = new Intent();
                intent.setClass(Activity4.this,Activity3.class);
                startActivity(intent);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}