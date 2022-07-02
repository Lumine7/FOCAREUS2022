package com.huawei.audiodevicekit.bluetoothsample.view;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.huawei.audiobluetooth.api.data.Acc;
import com.huawei.audiobluetooth.api.data.SensorData;
import com.huawei.audiobluetooth.layer.protocol.mbb.DeviceInfo;
import com.huawei.audiobluetooth.utils.DateUtils;
import com.huawei.audiobluetooth.utils.LogUtils;
import com.huawei.audiodevicekit.R;
import com.huawei.audiodevicekit.bluetoothsample.contract.SampleBtContract;
import com.huawei.audiodevicekit.bluetoothsample.presenter.SampleBtPresenter;
import com.huawei.audiodevicekit.mvp.view.support.BaseAppCompatActivity;

import java.io.File;
import java.util.Calendar;

public class SampleBtActivity
        extends BaseAppCompatActivity<SampleBtContract.Presenter, SampleBtContract.View>
        implements SampleBtContract.View {
    private static final String TAG = "SampleBtActivity";

//    private  int count1=0,count2=0;
    String mac=null;
    private Button btntonext14;
    private Button btntonext12;
    private TextView textdata;
    private TextView textm0;
    private TextView textm1;
    private TextView textm2;
    private TextView textm3;
    private TextView texts;
    private Button btn;
    private Button btn2;
    private Button btn3;

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public SampleBtContract.Presenter createPresenter() {
        return new SampleBtPresenter();
    }

    @Override
    public SampleBtContract.View getUiImplement() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        FileOperation.verifyStoragePermissions(this);
        Intent intent = new Intent();
        intent.setClass(SampleBtActivity.this, Activity3.class);
        //this前面为当前activty名称，class前面为要跳转到得activity名称
        startActivity(intent);
        textdata.setText(getdata());
        texts.setText("0");
        textm0.setText("0");
        textm1.setText("0");
        textm2.setText("0");
        textm3.setText("0");
    }

    private String getdata(){
        Calendar ca = Calendar.getInstance();
        int month = ca.get(Calendar.MONTH);//第几个月
        int year = ca.get(Calendar.YEAR);
        final String[] s={
                "jan","Feb","March","April","May","June",
                "July","August","Sept","OCt","Nov","Dec"
        };
        return s[month]+", "+year;
    }

    @Override
    protected int getResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        btntonext14 = findViewById(R.id.btn_to_next_14);
        btntonext12=findViewById(R.id.btn_to_next_12);
        texts=findViewById(R.id.s);
        textdata=findViewById(R.id.data);
        textm0=findViewById(R.id.m0);
        textm1=findViewById(R.id.m1);
        textm2=findViewById(R.id.m2);
        textm3=findViewById(R.id.m3);
        btn=findViewById(R.id.button);
        btn2=findViewById(R.id.button2);
        btn3=findViewById(R.id.button3);
    }

    @Override
    protected void initData() {
        getPresenter().initBluetooth(this);
    }

    @Override
    protected void setOnclick() {
        super.setOnclick();
        btntonext12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(SampleBtActivity.this, Activity3.class);
                //this前面为当前activty名称，class前面为要跳转到得activity名称
                startActivity(intent);
                //voiceRemind("Rest.mp3");
            }
        });
        btntonext14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(SampleBtActivity.this, Activity4.class);
                //this前面为当前activty名称，class前面为要跳转到得activity名称
                startActivity(intent);
                //voiceRemind("NoDevice.mp3");
            }
        });
        btn.setOnClickListener(v->getPresenter().checkLocationPermission(this));
        btn2.setOnClickListener(v->getPresenter().connect(mac));
        btn3.setOnClickListener(v->getPresenter().sendCmd(mac,19));
    }

    public void pushNewMessage(String s,String m0,String m1,String m2,String m3){
        textdata.setText(getdata());
        texts.setText(s);
        textm0.setText(m0);
        textm1.setText(m1);
        textm2.setText(m2);
        textm3.setText(m3);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getPresenter().processLocationPermissionsResult(requestCode, grantResults);
    }

    @Override
    public void onDeviceFound(DeviceInfo info) {
        mac=String.format("[%s] %s", info.getDeviceBtMac(), "HUAWEI Eyewear").substring(1,18);
        onError("找到设备，可以进行连接");
    }

    private String oldtime="00:00:70";
    public void voiceRemind(String name){
        String time=DateUtils.getCurrentDate();
        if(Math.abs(time2int(time)-time2int(oldtime))>4) {
            playAudioFile(name);
            oldtime=time;
        }
    }

    private int time2int(String s){
        String[] ss=s.split(":",3);
        return Integer.parseInt(ss[0])*3600+Integer.parseInt(ss[1])*60+Integer.parseInt(ss[2]);
    }

    private MediaPlayer mediaPlayer;
    private void playAudioFile(String s) {
        String filename= Environment.getExternalStorageDirectory()+"/"+s;
        File file=new File(filename);
        if(!file.exists()){
            onError("文件不存在");
            onSendCmdSuccess(filename);
        }
        LogUtils.d(TAG, "playAudioFile: " + file.getAbsolutePath());
        //MediaPlayer mediaPlayer;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setLooping(false);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    LogUtils.d(TAG, "Play local sound onError: " + i + ", " + i1);
                    return true;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "playAudioFile: ", e);
            onError("播放失败");
        }
    }

    @Override
    public void onStartSearch() {
    }

    @Override
    public void onDeviceChanged(BluetoothDevice device) {
        mac=String.format("[%s] %s", device.getAddress(), "HUAWEI Eyewear").substring(1, 18);
    }

    @Override
    public void onConnectStateChanged(String stateInfo) {
    }


    @Override
    public void onSensorDataChanged(SensorData sensorData) {
    }

    @Override
    public void onSendCmdSuccess(Object result) {
    }

    @Override
    public void onError(String errorMsg) {
        runOnUiThread(
                () -> Toast.makeText(SampleBtActivity.this, errorMsg, Toast.LENGTH_LONG).show());
    }

    @Override
    public void displayMessage(String str) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().deInit();
    }
}
