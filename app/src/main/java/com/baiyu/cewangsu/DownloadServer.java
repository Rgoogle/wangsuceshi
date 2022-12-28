package com.baiyu.cewangsu;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.baiyu.cewangsu.DownloadThread.DownloadTeadUDPUp;
import com.baiyu.cewangsu.DownloadThread.DownloadThread;
import com.baiyu.cewangsu.Utils.ApplicationFile;

/**
 * 服务 用来下载用的 启动线程 网速检测的
 */
public class DownloadServer extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //PowerManager.PARTIAL_WAKE_LOCK：表示CPU保持运行， 。
        //MyWakelockTag：表示唤醒锁的标签，可以用于在日志中标识唤醒锁,表示为这个WakeLock定义一个标签，以便在检查电源使用情况时能够识别它
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();
//         在不再需要WakeLock时释放：
//wakeLock.release();
    }

    /**
     *startService 调用该方法 自动执行下面的方法 和OnCeate 方法 ，onCreate只能调用一次,下面的方法会多次调用
     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String nums=intent.getStringExtra("线程数");
        DownloadThread.url =intent.getStringExtra("url");//下载链接
        DownloadThread.path=intent.getStringExtra("path");//文件路径
        boolean TCP=intent.getBooleanExtra("TCP",true);
        boolean UP=intent.getBooleanExtra("UP",true);

        int num=Integer.valueOf(nums);

        while (num!=0) {
            DownloadThread downloadThread=null;
            if(TCP) {
                if(!UP){
                    downloadThread = new DownloadThread();//TCP 下载

                    ApplicationFile.setJsonObjectUrl(intent.getStringExtra("url"));
                }
                else {
                   //downloadThread=new DownloadThreadTCPUp();//TCP 上传

                    Toast.makeText(this,"暂时不支持TCP 上传",Toast.LENGTH_SHORT).show();
                }

            }

            else {
                if (UP) {//UDP上传
                   /* if(DownloadThread.path.length()==0){//没有选择文件
                        stopService();//服务自己结束
                    }
                    if (DownloadThread.url.split(":")[0].length() == 0) {
                        Toast.makeText(this, "还需要在输入框输入ip和端口呢!!!",Toast.LENGTH_SHORT).show();
                        stopSelf();

                    }*/

                    //System.out.println("url"+DownloadThread.url.split(":")[0].length());

                     downloadThread = new DownloadTeadUDPUp();//UDP 上传
                    ApplicationFile.setJsonObjectUrl(intent.getStringExtra("url"));
                }
                else {


                    Toast.makeText(this,"暂时不支持UDP 下载",Toast.LENGTH_SHORT).show();
                }
            }
            Thread t = new Thread(downloadThread);
            t.start();
            num--;

        }
       // Toast.makeText(this,nums,Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent, flags, startId);
    }


/**
 * 不在下载 进来开始按钮可以点击
 * 在下载 进来看是按钮 不可以点击
 * 在下载 进程突然结束 进来 开始按钮应该是 可以点击
 *
 */

}
