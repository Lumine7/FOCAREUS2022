package com.huawei.audiodevicekit.bluetoothsample.view;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.util.Log;

import com.huawei.audiobluetooth.utils.DateUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by yangwenlong on 2020/5/16.
 */

public class FileOperation {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    //然后通过一个函数来申请
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String createfile(){
        String filename = "/1.txt";
        filename = Environment.getExternalStorageDirectory()+filename;
        File f=new File(filename);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

    static boolean filewrite(String filename,String data){
        try{
            File f=new File(filename);
            if(!f.exists())return false;
            FileWriter fw=new FileWriter(new File(filename),true);
            fw.write(DateUtils.getCurrentDate()+':'+data+'\n');
            fw.flush();
            fw.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
