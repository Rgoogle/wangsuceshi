package com.baiyu.cewangsu.DownloadThread;

import java.io.*;
import java.net.*;

public class DownloadTeadUDPUp extends DownloadThread{
    public static boolean isRun=false;//控制线程停止 true 为运行
    private File file=new File(path);
    private InetAddress inet;
    @Override
    public void run(){

        DatagramSocket client = null;
        int len;
        String[] ipAndPort=url.split(":");
        System.out.println("长度"+ipAndPort.length);
        try {
            inet=InetAddress.getByName(ipAndPort[0]);//ip
            byte[] buff = new byte[1024];
            FileInputStream fis=null;
            client = new DatagramSocket();//端口
            while(true)
            {

                fis=new FileInputStream(file);
                len=fis.read(buff);

                while(len!=-1){
                    if(!isRun){//要求停止

                    break;
                }
                    DatagramPacket packet = new DatagramPacket(buff,buff.length,inet, ipAndPort.length==1?16001:Integer.parseInt(ipAndPort[1]));
                    len=fis.read(buff);
                    client.send(packet);
                }
                //client.close();
                if(!isRun){//要求停止
                    Thread.currentThread().interrupt();
                    break;
                }
                //System.out.println("UDP上传");
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
