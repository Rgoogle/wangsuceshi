package com.baiyu.cewangsu.DownloadThread;

import android.net.http.HttpResponseCache;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * TCP下载
 */
public class DownloadThread implements Runnable {
    public static boolean isRun = false;//控制线程停止 true 为运行
    public static String url;//下载链接
    public static String path;//文件路径
    public static Handler loghandler;//异常handler


    @Override
    public void run() {

       label: while (true) {
            try {
                byte[] buff = new byte[1024];//1KB 大小内存
                int len;
                InputStream inputStream;
                URL Durl = new URL(url);
                HttpURLConnection conn;
                while (true) {

                    conn = (HttpURLConnection) Durl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);//链接服务器超时
                    conn.setReadTimeout(6000);//从服务器读取信息超时
                    conn.connect();

                    inputStream = conn.getInputStream();
                    len = inputStream.read(buff);
                    while (len != -1) {
                        if (!isRun) {//要求停止
                            break label;
                        }
                        len = inputStream.read(buff);
                    }
                    if (!isRun) {//要求停止
                        Thread.currentThread().interrupt();
                        break label;
                    }

                }

            } catch (MalformedURLException e) {
                Message msg = new Message();
                msg.obj = e.toString();
                loghandler.sendMessage(msg);
                continue;
            } catch (UnknownHostException e) {
                Message msg = new Message();
                msg.obj = e.toString();
                loghandler.sendMessage(msg);
                continue;
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.toString();
                loghandler.sendMessage(msg);
                continue;
            }

        }
    }



}
