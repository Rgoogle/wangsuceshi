package com.baiyu.cewangsu.Utils;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * 实时网速检测工具
 */
public class NetWorkSpeedUtils {
    private static NetWorkSpeedUtils netWorkInstance;

    /**
     * 记录传进来的context
     */
    private Context context;
    /**
     * 记录传进来的handler
     */

    private Handler mHandler;

    /**
     * 临时历史总流量，这次打开应用用的总流量 单位 B
     */
    public volatile static long temptotal = 0L;
    /**
     * 文件获取历史总流量,单位 字节 B
     */
    public static long totalData = 0L;


    /**
     * 上一次流量的多少
     */
    private long lastTotalRxBytes = 0;
    /**
     * 上一次的时间戳是多少
     */
    private long lastTimeStamp = 0;
    /**
     * 格式化网速专用
     */
    private static DecimalFormat showFloatFormat = new DecimalFormat("0.00");

    /**
     *
     */
    public static List<Runnable> taskList=new ArrayList<>();
    Map<String, String> messages = new HashMap<>();

    /**
     * TimerTask是Android中定时器的一种实现方式，用于在指定的时间间隔内定期执行某项任务。它的实现原理是通过创建一个新的线程来执行任务，
     * 但是它可以在主线程中执行。它可以帮助开发者实现一些定时任务，比如定时发送消息、定时同步数据等等。
     * 这里理解为一个任务，执行showNetSpeed 方法的
     */
//    TimerTask task = new TimerTask() {
//        @Override
//        public void run() {
//
//        }
//
//    };
    Runnable task = new Runnable() {
        @Override
        public void run() {
            showNetSpeed();
        }
    };

    //    Timer timer = null;
    ScheduledExecutorService scheduledExecutorService = null;

    private NetWorkSpeedUtils(Context context, Handler mHandler) {
        this.context = context;
        this.mHandler = mHandler;
    }

    public static NetWorkSpeedUtils getInstance(Context context, Handler mHandler) {
        if (netWorkInstance == null) {
            netWorkInstance = new NetWorkSpeedUtils(context, mHandler);
        }
        return netWorkInstance;
    }


    /**
     * 开始实时网速 外部调用
     */
    public void startShowNetSpeed() {
        //获取字节数 B
        lastTotalRxBytes = getTotalRxBytes();
        //它返回从 1970 年 1 月 1 日 00:00:00 GMT 到当前时间的毫秒数。理解为时间错
        lastTimeStamp = System.currentTimeMillis();
        //在指定的延迟时间delay之后，每隔period的时间就执行一次task任务。
        // 1s后启动任务，隔1s执行一次
//        timer = new Timer();

        scheduledExecutorService = Executors.newScheduledThreadPool(1);

//        timer.schedule(task, 1000, 1000);
        //1秒后延时执行，隔1秒运行，指定单位为秒  不知道为什么我测试了好久 3000这个数值才能和实际网速差不多匹配的上，无解
        //我觉得应该3秒刷新正常吧，1秒刷新太频繁了
        taskList.add(task);
        scheduledExecutorService.scheduleAtFixedRate(task, 1000, 3000, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取指定UID的接收流量字节数 B单位 字节单位
     *
     * @return
     */
    private long getTotalRxBytes() {
        //getUidRxBytes方法用于获取指定UID的接收流量字节数。该方法的作用是获取指定UID的接收流量字节数，以便统计指定应用的网络流量使用情况。
        // B 单位
        return TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes());
    }

    /**
     * 获取实时网速B/s
     */
    private void showNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();

        long nowTimeStamp = System.currentTimeMillis();
        // 获得一个整数 单位为 B/ms*1000  字节/秒  B/s
        // 毫秒转换
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / ((nowTimeStamp - lastTimeStamp)));
        //计算当前打开应用用的多少流量
        String s=showSpeed(totalData);
        synchronized (this) {
            temptotal += speed;
        }

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;

        Message msg = mHandler.obtainMessage();
        msg.what = 100;
        
        messages.put("speed", showSpeed(speed));
        messages.put("totalhistory", getTotalData(speed));
        messages.put("temptotal",getShow(temptotal));
        messages.put("longtemptotal", String.valueOf(temptotal));
//        System.out.println("正在运行"+showSpeed(temptotal)+"   "+showSpeed(speed)+"+"+s+"="+showSpeed(totalData));
        msg.obj = messages;
        //更新界面
        mHandler.sendMessage(msg);


    }

    /**
     * B/s 转正常人可以认识的网速
     *
     * @param speed B/s
     * @return
     */
    public static @NotNull String showSpeed(long speed) {
        String speedString = null;

        //大于等于1M 单位应该MB/s
        if (speed >= 1048756L&&speed<=1073926144L) {
            speedString = showFloatFormat.format(speed / 1024d / 1024d) + "MB/s";
            return speedString;
        }//小于 1MB  大于等于B  单位应该是kb/s
        else if (speed < 1048576L && speed >= 1024L) {

            speedString = showFloatFormat.format(speed / 1024d) + "KB/s";
            return speedString;
        } //小于等于KB 单位应该B/s
        else if (speed < 1024L) {
            speedString = showFloatFormat.format(speed) + "B/s";
            return speedString;
        } else {
            speedString = showFloatFormat.format(speed / 1024d / 1024d / 1024d) + "GB/s";
            return speedString;
        }


    }

    /**
     * 获取人可读的 GB MB
     * @param speed
     * @return
     */
    public static @NotNull String getShow(long speed) {
        String speedString = null;

        //大于等于1M 单位应该MB/s
        if (speed >= 1048756L&&speed<=1073926144L) {
            speedString = showFloatFormat.format(speed / 1024d / 1024d) + "MB";
            return speedString;
        }//小于 1MB  大于等于B  单位应该是kb/s
        else if (speed < 1048576L && speed >= 1024L) {

            speedString = showFloatFormat.format(speed / 1024d) + "KB";
            return speedString;
        } //小于等于KB 单位应该B/s
        else if (speed < 1024L) {
            speedString = showFloatFormat.format(speed) + "B";
            return speedString;
        } else {
            speedString = showFloatFormat.format(speed / 1024d / 1024d / 1024d) + "GB";
            return speedString;
        }


    }

    /**
     * 计算历史总流量 人可读的 MB KB GB
     *
     * @return
     */
    private String getTotalData(long speed) {//B
        String total;
        synchronized (this) {
            totalData += speed;
        }

        total = getShow(totalData);
        return total;
    }

    /**
     * 停止实时网速任务
     */
    public void stopTimerTask() {
//        task.cancel();
//        timer.cancel();
        if (scheduledExecutorService==null){
            return;
        }
        scheduledExecutorService.shutdownNow();

    }
}