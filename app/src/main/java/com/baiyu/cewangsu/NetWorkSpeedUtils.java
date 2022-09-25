package com.baiyu.cewangsu;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class NetWorkSpeedUtils {
    private Context context;

    private Handler mHandler;

    public static String total;

    private long lastTotalRxBytes = 0;

    private long lastTimeStamp = 0;

    public NetWorkSpeedUtils(Context context, Handler mHandler){
        this.context = context;

        this.mHandler = mHandler;

    }

    TimerTask task = new TimerTask() {
        @Override

        public void run() {
            showNetSpeed();

        }

    };

    public void startShowNetSpeed(){
        lastTotalRxBytes = getTotalRxBytes();

        lastTimeStamp = System.currentTimeMillis();

        new Timer().schedule(task, 1000, 1000); // 1s后启动任务，每2s执行一次

    }

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 :(TrafficStats.getTotalRxBytes()/1024);//转为KB

    }

    private void showNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();

        long nowTimeStamp = System.currentTimeMillis();

        long speed = ((nowTotalRxBytes - lastTotalRxBytes)  *1000/ (nowTimeStamp - lastTimeStamp));//毫秒转换

        long speed2 = ((nowTotalRxBytes - lastTotalRxBytes)  *1000% (nowTimeStamp - lastTimeStamp));//毫秒转换

        lastTimeStamp = nowTimeStamp;

        lastTotalRxBytes = nowTotalRxBytes;

        Message msg = mHandler.obtainMessage();

        msg.what = 100;

        long totalBit = speed+speed2;//多少B

        msg.obj=showSpeed(totalBit);

        mHandler.sendMessage(msg);//更新界面

    }



    private DecimalFormat showFloatFormat =new DecimalFormat("0.00");
    private String showSpeed(double speed) {
        String speedString;

        if (speed >=1024d) {//KB
            total=getTotalData((long) (speed*1000));//B
            speedString =showFloatFormat.format(speed /1024d) +"MB/s";

        }else {
            total=getTotalData((long) (speed*1000));//B
            speedString =showFloatFormat.format(speed /1d) +"KB/s";

        }

        return speedString;

    }

   static long totalData;
    public String getTotalData(long speed){//B

        long speedsum=0;//B

        String total;
        synchronized (this) {

            totalData = totalData + (speed / 1024);
            if (totalData <= 1048576) {//KB 小于1G
                total = showFloatFormat.format(totalData / 1024d) + "MB";
            } else {

                total = showFloatFormat.format(totalData / 1048576d) + "GB";
            }
        }
        return total;

    }


    public  void stopTimerTask(){
        task.cancel();
    }
}