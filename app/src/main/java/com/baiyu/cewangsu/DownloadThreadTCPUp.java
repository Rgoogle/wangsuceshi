package com.baiyu.cewangsu;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class DownloadThreadTCPUp extends DownloadThread{
    static boolean isRun=false;//控制线程停止 true 为运行
    private static File file;

    @Override
    public void run(){
        if(!isRun){//要求停止
            Thread.interrupted();
        }
        file=new File(path);

        try {
            FileInputStream fis=new FileInputStream(file);
            URL Uurl=new URL(url);

            HttpURLConnection conn = (HttpURLConnection) Uurl.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            //conn.setRequestProperty("connection","Keep-Alive");
            conn.setConnectTimeout(5000);//链接服务器超时
            conn.setReadTimeout(6000);//从服务器读取信息超时
            conn.connect();
            OutputStream out =new DataOutputStream(conn.getOutputStream());
            InputStream in = new DataInputStream(fis);
            byte[] buff=new byte[1024];//1kb
            int len=in.read(buff);

            while (len != -1) {
                    out.write(buff, 0, len);
                    len = in.read(buff);
            }

            out.flush();
            out.close();
            fis.close();

            System.out.println("上传");


        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
