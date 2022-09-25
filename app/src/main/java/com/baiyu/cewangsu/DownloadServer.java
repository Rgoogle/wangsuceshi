package com.baiyu.cewangsu;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import android.widget.Toast;
import androidx.annotation.Nullable;

public class DownloadServer extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
}
