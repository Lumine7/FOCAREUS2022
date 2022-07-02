package com.huawei.audiodevicekit.bluetoothsample.presenter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.huawei.audiobluetooth.api.AudioBluetoothApi;
import com.huawei.audiobluetooth.api.data.Acc;
import com.huawei.audiobluetooth.api.data.SensorData;
import com.huawei.audiobluetooth.api.data.SensorDataHelper;
import com.huawei.audiobluetooth.constant.ConnectState;
import com.huawei.audiobluetooth.layer.bluetooth.DiscoveryHelper;
import com.huawei.audiobluetooth.layer.protocol.mbb.DeviceInfo;
import com.huawei.audiobluetooth.utils.BluetoothUtils;
import com.huawei.audiobluetooth.utils.LogUtils;
import com.huawei.audiodevicekit.R;
import com.huawei.audiodevicekit.bluetoothsample.contract.SampleBtContract;
import com.huawei.audiodevicekit.bluetoothsample.model.SampleBtModel;
import com.huawei.audiodevicekit.bluetoothsample.model.SampleBtRepository;
import com.huawei.audiodevicekit.mvp.impl.ABaseModelPresenter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SampleBtPresenter extends ABaseModelPresenter<SampleBtContract.View, SampleBtModel>
        implements SampleBtContract.Presenter, SampleBtModel.Callback {
    private static final String TAG = "SampleBtPresenter";

    /**
     * 位置权限
     */
    private String[] locationPermission = {"android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"};

    private int LOCATION_PERMISSION_REQUEST_CODE = 188;

    private  int count1=0,count2=0;
    private AttentionCheck AC=new AttentionCheck();
    private Double asum=0.0;
    private int timecount=0,blurcount=0,healthycount=0;
    private int lastnotice=0;
    private int startflag=0;
    private long starttime;
    private FocusCheck fc=new FocusCheck();
//    private int nod_gstate=0;
//    private int nod_astate=0;
//    private int[] nod_lastfinal=new int[2];

    @Override
    public SampleBtModel createModel() {
        return new SampleBtRepository(this);
    }

    @Override
    public void initBluetooth(Context context) {
        LogUtils.i(TAG, "initBluetooth");
        if (!isUiDestroy()) {
            getModel().initBluetooth(context);
        }
    }

    @Override
    public void checkLocationPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                    .requestPermissions(activity, locationPermission, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            LogUtils.i(TAG, "Already got LOCATION Permission");
            startSearch();
        }
    }

    @Override
    public void processLocationPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startSearch();
            } else {
                if (!isUiDestroy()) {
                    getUi().voiceRemind("NoDevice.mp3");
                }
            }
        }
    }

    @Override
    public void deInit() {
        LogUtils.i(TAG, "deInit");
        AudioBluetoothApi.getInstance().deInit();
    }

    @Override
    public void startSearch() {
        LogUtils.i(TAG, "startSearch");
        if (!isUiDestroy()) {
            getUi().onStartSearch();
            getModel().startSearch();
        }
    }

    @Override
    public void connect(String mac) {
        LogUtils.i(TAG, "connect");
        if (!isUiDestroy()) {
            if (!BluetoothUtils.checkMac(mac)) {
                LogUtils.e(TAG, "Invalid MAC address.connect fail ! ");
                getUi().onError("Invalid MAC address.connect fail ! ");
                return;
            }
            getModel().connect(mac);
        }
    }

    @Override
    public boolean isConnected(String mac) {
        boolean connected = AudioBluetoothApi.getInstance().isConnected(mac);
        LogUtils.i(TAG, "isConnected connected = " + connected);
        return connected;
    }

    @Override
    public void disConnect(String mac) {
        LogUtils.i(TAG, "disConnect");
        if (!isUiDestroy()) {
            if (!BluetoothUtils.checkMac(mac)) {
                LogUtils.e(TAG, "Invalid MAC address.disConnect fail ! ");
                getUi().onError("Invalid MAC address.disConnect fail ! ");
                return;
            }
            getModel().disConnect(mac);
        }
    }

    @Override
    public void sendCmd(String mac, int cmdType) {
        LogUtils.i(TAG, "sendCmd");
        if (!isUiDestroy()) {
            if (!BluetoothUtils.checkMac(mac)) {
                LogUtils.e(TAG, "Invalid MAC address.sendCmd fail ! ");
                getUi().onError("Invalid MAC address.sendCmd fail ! ");
                return;
            }
            getModel().sendCmd(mac, cmdType);
            getUi().onError("do link");
        }
    }

    private int countheadcheck=0;
    public boolean HeadHealthyCheck(SensorData sensorData) {
        boolean ret=false;
        countheadcheck+=1;

        if (sensorData.accelDataLen != 0) {
            Double sum1 = 0.0, sum2 = 0.0;
            for (int i = 0; i < sensorData.accelDataLen; i++) {
                int ax = sensorData.accelData[i].x;
                int ay = sensorData.accelData[i].y;
                int az = sensorData.accelData[i].z;
                Double total = java.lang.Math.sqrt(ax * ax + ay * ay + az * az);
//            Double theta0=-90-java.lang.Math.asin(ax/total)*180/3.1415;
                Double theta1 = -java.lang.Math.asin(ay / total) * 180 / 3.1415;
                Double theta2 = java.lang.Math.asin(az / total) * 180 / 3.1415;

                sum1 += theta1;
                sum2 += theta2;
            }
            sum1 /= sensorData.accelDataLen;
            sum2 /= sensorData.accelDataLen;

            int down1 = -35, up1, down2 = -15, up2 = 15, threshold = 25;
            if (sum1 < down1) this.count1++;
            if (sum2 < down2 || sum2 > up2) this.count2++;
            if (sum1 > down1 && sum2 > down2 && sum2 < up2) {
                count1 = 0;
                count2 = 0;
            }

            if (count1 > threshold && lastnotice != 1) {
                lastnotice = 1;
                getUi().displayMessage("低头严重，请调整坐姿\n");
                getUi().voiceRemind("Downhead.mp3");
                ret = true;
            }
            if (count2 > threshold && lastnotice != 2) {
                lastnotice = 2;
                getUi().displayMessage("偏头严重，请调整坐姿\n");
                getUi().voiceRemind("Waitou.mp3");
                ret = true;
            } else {
                lastnotice = 0;
                healthycount++;
            }
        }
        return ret;
    }

    public boolean ACheck(SensorData sensorData)
    {
        boolean ret=false;
        if(sensorData.accelDataLen!=0){
            double x=0,y=0,z=0;
            for(int i=0;i<sensorData.accelDataLen;i++){
                x+= sensorData.accelData[i].x;
                y+= sensorData.accelData[i].y;
                z+= sensorData.accelData[i].z;
            }
            Acc a=new Acc();
            a.x=(int)(x/sensorData.accelDataLen);
            a.y=(int)(y/sensorData.accelDataLen);
            a.z=(int)(z/sensorData.accelDataLen);
            ret=fc.judge(a);
            if(ret){
                getUi().voiceRemind("Attention.mp3");
            }
        }
        return ret;
//        boolean ret=false;
//        int threshold=6000000;
//        if (sensorData.gyroDataLen != 0) {
//            Double sum=0.0;
//            for (int i = 0; i < sensorData.gyroDataLen; i++) {
//                long wr = sensorData.gyroData[i].roll;
//                long wp = sensorData.gyroData[i].pitch;
//                long wy = sensorData.gyroData[i].yaw;
//                Double total = java.lang.Math.sqrt(wr*wr+wp*wp+wy*wy*0.66);
//                sum+=total;
//            }
//            sum/=sensorData.gyroDataLen;
//
//            asum+=sum;timecount++;
//
//            if(timecount%150==0)
//            {
//                if(asum>=threshold)
//                {
//                    getUi().displayMessage("检测到注意力涣散："+asum.toString()+"\n");
//                    if(!oldacheck)getUi().voiceRemind("Attention.mp3");
//                    blurcount+=2;
//                    ret=true;
//                }
//                else if(asum>=threshold*0.75)
//                {
//                    getUi().displayMessage("检测到注意力可能涣散："+asum.toString()+"\n");
//                    blurcount+=1;
//                }
//                else {
//                    getUi().displayMessage("注意力正常："+asum.toString()+"\n");
//                }
//                asum=0.0;
//            }
//            if(timecount%300==0)
//            {
//                Double score=(1-((float)blurcount*150/timecount)*0.77)*100;
//                if(score<70)
//                {
//                    getUi().displayMessage("最近一段时间的注意力得分为："+score.toString()+"请专注一些\n");
//                }
//                else if(score<85)
//                {
//                    getUi().displayMessage("最近一段时间的注意力得分为："+score.toString()+"加油啊\n");
//                }
//                else
//                {
//                    getUi().displayMessage("最近一段时间的注意力得分为："+score.toString()+"再接再励\n");
//                }
//            }
//            if(timecount==864000)timecount=0;
//        }
//        oldacheck=ret;
//        return ret;
    }

    @Override
    public void registerListener(String mac) {
        if (!isUiDestroy()) {
            getModel().registerListener(mac, result -> {
                LogUtils.i(TAG, "result = " + result);
                byte[] appData = result.getAppData();
                if (!isUiDestroy()) {
                    SensorData sensorData = SensorDataHelper.genSensorData(appData);
                    getUi().onSensorDataChanged(sensorData);
                    MultiCheck(sensorData);
                }
            });
        }
    }

    private boolean begin=false;
    private boolean rest=true;
    private long count=0;
    public void beginexamine(){
        begin=true;
    }

    final private int studytime=300;
    final private int resttime=50;
    public void MultiCheck(SensorData sensorData)
    {

        if(rest){
            if(count==0){
                rest=false;
                getUi().voiceRemind("beginstudy.mp3");
            }
            count++;
            count%=resttime;
        }else{
            if(count==0){
                rest=true;
                getUi().voiceRemind("Rest.mp3");
            }
            count++;
            count%=studytime;
        }
        //getUi().onError(String.valueOf(count));
        if(rest)return;

        if(startflag==0)
        {
            startflag=1;
            starttime=System.currentTimeMillis();
        }
        if(!HeadHealthyCheck(sensorData))
        {
            ACheck(sensorData);
        }

        timecount++;
        if(timecount%20==0)
        {
            sendMessage();
        }
    }

    public void sendMessage()
    {
        Double score=(1-((float)fc.getCnt()/timecount)*0.77)*100;
        String s= String.valueOf(Math.round(score));

        int totalmin=(int)(System.currentTimeMillis()-starttime)/60000;
        String m0=""+totalmin;

        int focusmin=(int)(totalmin*(1-((float)fc.getCnt()/timecount)));
        String m1=""+focusmin;

        int healthymin=(int)(totalmin*((float)healthycount/timecount));
        String m2=""+healthymin;

        int blurmin=totalmin-focusmin;
        String m3=""+blurmin;

        getUi().pushNewMessage(s,m0,m1,m2,m3);//接收5个string
    }

    @Override
    public void unregisterListener(String mac) {
        if (!isUiDestroy()) {
            getModel().unregisterListener(mac);
        }
    }

    @Override
    public void onDeviceFound(DeviceInfo deviceInfo) {
        //String mac=String.format("[%s] %s", deviceInfo.getDeviceBtMac(), "HUAWEI Eyewear").substring(1,18);
        getUi().onDeviceFound(deviceInfo);
    }

    @Override
    public void onConnectStateChanged(BluetoothDevice device, int state) {
        String stateInfo;
        switch (state) {
            case ConnectState.STATE_UNINITIALIZED:
                stateInfo = getUi().getContext().getResources().getString(R.string.not_initialized);
                break;
            case ConnectState.STATE_CONNECTING:
                stateInfo = getUi().getContext().getResources().getString(R.string.connecting);
                break;
            case ConnectState.STATE_CONNECTED:
                stateInfo = getUi().getContext().getResources().getString(R.string.connected);
                getUi().onError("连接成功");
                break;
            case ConnectState.STATE_DATA_READY:
                stateInfo = getUi().getContext().getResources().getString(R.string.data_channel_ready);
                registerListener(device.getAddress());
                getUi().onError("连接成功2");
                break;
            case ConnectState.STATE_DISCONNECTED:
                stateInfo = getUi().getContext().getResources().getString(R.string.disconnected);
                unregisterListener(device.getAddress());
                break;
            default:
                stateInfo = getUi().getContext().getResources().getString(R.string.unknown) + state;
                break;
        }
        LogUtils.i(TAG,
                "onConnectStateChanged  state = " + state + "," + stateInfo + "," + ConnectState
                        .toString(state));
        if (!isUiDestroy()) {
            getUi().onConnectStateChanged(stateInfo);
            getUi().onDeviceChanged(device);
        }
    }

    @Override
    public void onSendCmdResult(boolean isSuccess, Object result) {
        LogUtils.i(TAG, "onSendCmdResult  isSuccess = " + isSuccess + ", result = " + result);
        if (!isUiDestroy()) {
            if (isSuccess) {
                getUi().onSendCmdSuccess(result);
            } else {
                getUi().onError("AT指令发送失败，错误码：" + result);
            }
        }
    }
}
