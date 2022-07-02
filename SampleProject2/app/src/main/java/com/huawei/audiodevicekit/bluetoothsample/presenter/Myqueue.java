package com.huawei.audiodevicekit.bluetoothsample.presenter;

import com.huawei.audiobluetooth.api.data.Acc;

public class Myqueue{
    private Acc[] queue;
    int begin;
    int end;
    int max;
    int cnt;

    public Myqueue(int num){
        queue=new Acc[num+1];
        begin=0;
        end=0;
        max=num;
        cnt=0;
    }

    public boolean isempty(){
        return begin==end;
    }

    public boolean isfull(){
        return (end+1)%(max+1)==begin;
    }

    public boolean add(Acc a){
        if(isfull())return false;
        queue[end]=a;
        end=(end+1)%(max+1);
        cnt=cnt+1;
        return true;
    }

    public boolean remove(){
        if(isempty())return false;
        //Acc ret=queue[begin];
        begin=(begin+1)%(max+1);
        cnt=cnt-1;
        return true;
    }

    public Acc getvalue(int index){
        if(index>cnt)return null;
        return queue[(begin+index)%(max+1)];
    }

    public void clear(){
        begin=end=cnt=0;
    }

    public  int size(){
        return cnt;
    }
}