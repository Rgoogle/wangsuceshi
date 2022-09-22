package com.baiyu.cewangsu;

import android.app.Activity;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.SQLOutput;
import java.util.List;

public class ApplicationFile {
    public static JSONObject jsonObject;
    public static File configFile;
    public static JSONArray jsonArray;
    //创建配置文件函数
    public static void createConfigurationFile(Activity act){
       File file=act.getExternalFilesDir("");//得到Android/data/包名/files 目录
       configFile=new File(file.toString()+"/config.json");


       if (!configFile.exists()) {
           try {
               configFile.createNewFile();
               System.out.println("成功了");
           } catch (IOException e) {
               System.out.println("配置文件异常");
           }
       }




        try {
            BufferedInputStream buf=new BufferedInputStream(new FileInputStream(configFile));
            byte[] buffer = new byte[1024];
            int len=buf.read(buffer);
            String jsonString ="";

            while (len != -1) {
                jsonString =jsonString+new String(buffer, 0, len);
                len = buf.read(buffer);
            }

            if(jsonString.length() == 0){
                System.out.println("文件空内容:写入成功");
                jsonString="{\"url\":[\"https:\\/\\/baidu.com\",\"https:\\/\\/pm.myapp.com\\/invc\\/xfspeed\\/qqpcmgr\\/download\\/QQPCDownload320001.exe\"],\"TCP\":true,\"UP\":false,\"switch\":true}";

            }
            else {
                System.out.println("womaiga:"+jsonString.length());
            }

            //System.out.println("hello"+jsonString);
            jsonObject=new JSONObject(jsonString);//文件内容加载进入json对象
            //System.out.println("对象："+jsonObject);
            buf.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public static void setJsonObjectUrl(String url) {
        try {
            jsonArray=jsonObject.getJSONArray("url");
           // System.out.println("测试"+jsonArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        jsonArray.put(url);

        try {
            jsonObject.put("url",jsonArray);
            loadFile();
            //System.out.println("文件类容:"+jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setSwitchCondiction(boolean tcp ,boolean up){
        try {

            jsonObject.put("TCP",tcp);
            jsonObject.put("UP",up);
            loadFile();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public static void removeSwitchCondiction(){

        jsonObject.remove("TCP");
        jsonObject.remove("UP");
    }


    private static void loadFile(){
        FileWriter fileWriter= null;
        try {
            fileWriter = new FileWriter(configFile);
            fileWriter.write(jsonObject.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static boolean[] getSwitchCondiction(){
        boolean[] bool=new boolean[2];
        try {
            bool[0]=jsonObject.getBoolean("TCP");
            bool[1]=jsonObject.getBoolean("UP");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return bool;
    }


    public static void setStartAndStop(boolean bool){
        try {
            jsonObject.remove("switch");
            jsonObject.put("switch",bool);//true 开始按钮可以点击 停止不可以 false 相反
            loadFile();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
    public static boolean getStartAndStop(){

        try {
            return jsonObject.getBoolean("switch");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public static void getArraySpinnerFromFile(List<String> spinnerData){
        try {
            JSONArray jsonArray1=jsonObject.getJSONArray("url");
            for (int i = 0; i < jsonArray1.length(); i++) {
                //System.out.println("hello:"+jsonArray1.getString(i));
                spinnerData.add(jsonArray1.getString(i));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
