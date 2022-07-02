package com.huawei.audiodevicekit.bluetoothsample.presenter;

import java.lang.Math;

import com.huawei.audiobluetooth.api.data.Acc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FocusCheck {
    final private int sampletime=300;
    private Myqueue accset=new Myqueue(sampletime);
    final private int judgetime=100;
    private int time=sampletime;
    final private int gravity=4096;
    private boolean dosample=true;
    final private int K=2;
    private Acc[] means;
    private List<Acc>[] eachkind;
    private double[] angle;
    private int count=0;
    private int cnt;

    public int getCnt()
    {
        return cnt;
    }

    public boolean willexamine(){
        return count%judgetime==0;
    }

    public boolean judge(Acc a){
//        FileWriter fvar=new FileWriter(new File("D:\\txt (2)\\AIglass\\var.txt"),true);
//        FileWriter fave=new FileWriter(new File("D:\\txt (2)\\AIglass\\ave.txt"),true);
//        FileWriter fmeans=new FileWriter(new File("D:\\txt (2)\\AIglass\\means.txt"),true);
//        FileWriter fangle=new FileWriter(new File("D:\\txt (2)\\AIglass\\angle.txt"),true);
//        FileWriter ftrue=new FileWriter(new File("D:\\txt (2)\\AIglass\\true.txt"),true);
        boolean ret=false;
        if(!sample(a)){
            accset.add(a);
            count+=1;
            if(count%judgetime==0) {
                cnt=0;
                for(int i=accset.size()-judgetime;i<accset.size();i++) {
                    if(!inbound(accset.getvalue(i))) {
                        cnt++;
                    }
                }
                if(cnt>0.4*judgetime) {
                    ret=true;
                }
                System.out.println();
            }
            if(count%sampletime==0) {
                Kmeans();
                accset.clear();
            }
            count%=judgetime*sampletime;
        }
//        fvar.close();
//        fmeans.close();
//        fangle.close();
//        fave.close();
//        ftrue.close();
        return ret;
    }

    private Acc getave(){
        double x=0,y=0,z=0;
        for(int i=0;i<accset.size();i++){
            Acc a= accset.getvalue(i);
            x+=a.x;
            y+=a.y;
            z+=a.z;
        }
        Acc ret=new Acc();
        ret.x=(int)(x/accset.size());
        ret.y=(int)(y/accset.size());
        ret.z=(int)(z/accset.size());
        return ret;
    }

    private double getjudgevar(Acc ave){
        double sum=0;
        for(int i=0;i<accset.size();i++){
            Acc a= accset.getvalue(i);
            double d=dist(a,ave);
            sum+=d*d;
        }
        return sum/ accset.size();
    }

    private boolean inbound(Acc ave){
        boolean ret=false;
        for(int i=0;i<K;i++){
            if(dist(means[i],ave)<angle[i]){
                ret=true;
                break;
            }
        }
        return ret;
    }

    //采样180s
    private boolean sample(Acc a){
        if(!dosample)return false;
        double factor=moduls(a)/gravity;
        a.x=(int)(a.x/factor);
        a.y=(int)(a.y/factor);
        a.z=(int)(a.z/factor);
        accset.add(a);
        time=time-1;
        if(time==0){
            dosample=false;
            Kmeans();
            accset.clear();
        }
        return true;
    }

    //初始化
    public void reset(){
        accset.clear();
        time=sampletime;
        dosample=true;
        count=0;
    }

    private void Kmeans(){
        final int maxiter=10;
        means=RandChoicePoint();
        eachkind=getkind(means);
        for(int i=0;i<maxiter;i++){
            Acc[] initpoint=RandChoicePoint();
            Acc[] temp=initpoint;
            List<Acc>[] tempset;
            do{
                initpoint=temp;
                tempset=getkind(initpoint);
                temp=updateacc(tempset);
            }while(stopiter(initpoint,temp));
            if(better(means,temp)){
                means=temp;
                eachkind=tempset;
            }
        }
        angle=computeangle();
    }

    private Acc[] updateacc(List<Acc>[] kinds){
        Acc[] ret=new Acc[K];
        for(int i=0;i<K;i++) {
            ret[i]=new Acc();
        }
        for(int i=0;i<K;i++){
            double x=0;
            double y=0;
            double z=0;
            for(Acc a:kinds[i]){
                x+=a.x;
                y+=a.y;
                z+=a.z;
            }
            ret[i].x=(int)(x/kinds[i].size());
            ret[i].y=(int)(y/kinds[i].size());
            ret[i].z=(int)(z/kinds[i].size());
        }
        return ret;
    }

    private List<Acc>[] getkind(Acc[] initpoint){
        List<Acc>[] ret = new List[K];
        for(int i=0;i<K;i++){
            ret[i]=new ArrayList<>();
        }
        for(int j=0;j<accset.size();j++){
            Acc a=accset.getvalue(j);
            double mindist=Double.POSITIVE_INFINITY;
            int index=0;
            for(int g=0;g<K;g++){
                double d=dist(initpoint[g],a);
                if(d<mindist){
                    mindist=d;
                    index=g;
                }
            }
            ret[index].add(a);
        }
        return ret;
    }

    private double[] computeangle(){
        double[] ret=new double[K];
        for(int i=0;i<K;i++){
            ret[i]=cmpangle(i);
        }
        return ret;
    }

    private double cmpangle(int i){
        double angle=0.2;
        double ratio=0.8;
        double diff=Math.max(0.01,1.0/eachkind[i].size());
        double anotherangle=5;
        double begin;
        double end;
        double thisratio=getratio(i,angle);
        if(thisratio<ratio){
            do {
                anotherangle = angle;
                angle *= 2;
            }while(getratio(i,angle)<ratio);
        }
        return 0.3;
        /*
        if(Math.abs(thisratio-ratio)<diff)return angle;
        if(thisratio<ratio){
            do {
                anotherangle = angle;
                angle *= 2;
            }while(getratio(i,angle)<ratio);
            begin=anotherangle;
            end=angle;
        }else{
            do{
                anotherangle=angle;
                angle/=2;
            }while(getratio(i,angle)>ratio);
            begin=angle;
            end=anotherangle;
        }

        double mid;
        while(true){
            mid=(begin+end)/2;
            thisratio=getratio(i,mid);
            if(Math.abs(thisratio-ratio)<diff){
                return mid;
            }else if(thisratio<ratio){
                begin=mid;
            }else{
                end=mid;
            }
        }
        */
    }

    private double getratio(int i,double angle){
        int cnt=0;
        for(Acc a:eachkind[i]){
            if(dist(a,means[i])<=angle){
                cnt+=1;
            }
        }
        return (double)cnt/eachkind[i].size();
    }

    private boolean better(Acc[] a1,Acc[] a2){
        return variance(a1)>variance(a2);
    }

    private double variance(Acc[] a1){
        double[] var=new double[K];
        int[] cnt=new int[K];
        for(int j=0;j<accset.size();j++){
            Acc a=accset.getvalue(j);
            double mindist=Double.POSITIVE_INFINITY;
            int index=0;
            for(int g=0;g<K;g++){
                double d=dist(a1[g],a);
                if(d<mindist){
                    mindist=d;
                    index=g;
                }
            }
            double d=dist(a1[index],a);
            var[index]+=d*d;
            cnt[index]+=1;
        }
        double ret=0;
        for(int i=0;i<K;i++){
            ret+=var[i]/cnt[i];
            System.out.println(var[i]/cnt[i]);
        }
        return ret;
    }

    private boolean stopiter(Acc[] a1,Acc[] a2){
        boolean ret=false;
        for(int i=0;i<K;i++){
            if(dist(a1[i],a2[i])>0.05){
                ret=true;
            }
        }
        return ret;
    }

    private Acc[] RandChoicePoint() {
        Set<Integer> s=new HashSet<>();
        for(int i=0;i<K;i++){
            while(true){
                int num=(int)(Math.random()*accset.size());
                int oldsize=s.size();
                s.add(num);
                if(s.size()!=oldsize)break;
            }
        }
        Acc[] ret=new Acc[K];
        int cnt=0;
        for(int i:s){
            ret[cnt]=accset.getvalue(i);
            cnt+=1;
        }
        return ret;
    }

    private double moduls(Acc a){
        return Math.sqrt(a.x*a.x+a.y*a.y+a.z*a.z);
    }

    private double dist(Acc a1,Acc a2){
        int inner=a1.x*a2.x+a1.y*a2.y+a1.z*a2.z;
        double mod=moduls(a2);
        double x=inner/moduls(a1);
        double y=Math.sqrt(mod*mod-x*x);
        return Math.abs(Math.atan2(y,x));
    }
}