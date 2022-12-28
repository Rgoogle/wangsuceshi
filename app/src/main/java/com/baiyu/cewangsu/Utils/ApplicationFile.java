package com.baiyu.cewangsu.Utils;

import android.app.Activity;
import com.baiyu.cewangsu.Utils.SpecialFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.List;

/**
 * 应用配置文件工具类
 */
public class ApplicationFile {
    /**
     * 序列化工具
     */
    public static JSONObject jsonObject;
    /**
     * 表示配置文件
     */
    public static File configFile;
    public static JSONArray jsonArray;

    //创建配置文件函数
    public static void createConfigurationFile(Activity act) {
        File file = act.getExternalFilesDir("");//得到Android/data/包名/files 目录
        configFile = new File(file.toString() + "/config.json");
        //如果文件不存在
        if (!configFile.exists()) {
            try {//创建配置文件
                configFile.createNewFile();
//               System.out.println("成功了");
            } catch (IOException e) {
                System.out.println("配置文件创建异常");
            }
        }


        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(configFile));
            byte[] buffer = new byte[1024];
            int len = buf.read(buffer);
            String jsonString = "";

            while (len != -1) {
                jsonString = jsonString + new String(buffer, 0, len);
                len = buf.read(buffer);
            }

            if (jsonString.length() == 0) {
//                System.out.println("文件空内容:写入成功");
                jsonString = "{\"url\":[\"116.128.138.96\",\"116.128.138.253\",\"101.89.17.208\",\"153.35.121.1\",\"https:\\/\\/gameplus-platform.cdn.bcebos.com\\/gameplus-platform\\/upload\\/file\\/source\\/9752b85b3f8bddf3c09e1f6b41433d27.apk\",\"https:\\/\\/pm.myapp.com\\/invc\\/xfspeed\\/qqpcmgr\\/download\\/QQPCDownload320001.exe\"],\"TCP\":true,\"UP\":false,\"switch\":true,\"pos\":1}";

            }
            //else {
////                System.out.println("womaiga:" + jsonString.length());
//            }

            //System.out.println("hello"+jsonString);
            jsonObject = new JSONObject(jsonString);//文件内容加载进入json对象
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

    /**
     * 统计url 为历史记录
     * @param url
     */
    public static void setJsonObjectUrl(String url) {
        boolean bool = isExisitInArray(url);//判断是否存在再加，不然会重复 false 为不可加  true 为可添加
        try {
            jsonArray = jsonObject.getJSONArray("url");
            // System.out.println("测试"+jsonArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (bool) {//true 表示要添加
            jsonArray.put(url);
        } else {//有就啥都不做
            return;
        }


        try {
            jsonObject.put("url", jsonArray);
            loadFile();
            //System.out.println("文件类容:"+jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 记录界面的 TCP up 情况
     * @param tcp
     * @param up
     */
    public static void setSwitchCondiction(boolean tcp, boolean up) {
        try {

            jsonObject.put("TCP", tcp);
            jsonObject.put("UP", up);
            loadFile();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public static void removeSwitchCondiction() {

        jsonObject.remove("TCP");
        jsonObject.remove("UP");
    }


    private static void loadFile() {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(configFile);
            fileWriter.write(jsonObject.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 获取TCP up情况
     * @return
     */
    public static boolean[] getSwitchCondiction() {
        boolean[] bool = new boolean[2];
        try {
            bool[0] = jsonObject.getBoolean("TCP");
            bool[1] = jsonObject.getBoolean("UP");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return bool;
    }


    /**
     * 设置界面开关按钮情况
     * @param bool
     */
    public static void setStartAndStop(boolean bool) {
        try {
            jsonObject.remove("switch");
            jsonObject.put("switch", bool);//true 开始按钮可以点击 停止不可以 false 相反
            loadFile();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
    /**
     * 获取界面开关按钮情况
     */
    public static boolean getStartAndStop() {

        try {
            return jsonObject.getBoolean("switch");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public static void getArraySpinnerFromFile(List<String> spinnerData) {
        try {
            JSONArray jsonArray1 = jsonObject.getJSONArray("url");
            for (int i = 0; i < jsonArray1.length(); i++) {
                //System.out.println("hello:"+jsonArray1.getString(i));

                if (!SpecialFilter.Urlfilter(spinnerData, jsonArray1.getString(i))) {//false 表示不是特殊url
                    spinnerData.add(jsonArray1.getString(i));
                }

            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断url 是否添加过
     * @param url
     * @return
     */
    public static boolean isExisitInArray(String url) {//true 表示 连接不存在 false 表示 连接存在
        JSONArray jsonArray1 = null;
        //url=url.replace("//","\\/\\/");

        try {
            jsonArray1 = jsonObject.getJSONArray("url");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < jsonArray1.length(); i++) {
            try {
                if (url.equals(jsonArray1.getString(i))) {
                    return false;
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    /**
     * 记录上次使用的连接
     * @param position
     */
    public static void insertUrlRecord(int position) {//记录下列列表的位置
        try {

            jsonObject.remove("pos");
            jsonObject.put("pos", position);//
            loadFile();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getUrlRecord() {
        try {
            return jsonObject.getInt("pos");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 记录历史总流量
     * @param totalData
     */
    public static void setTotalData(long totalData) {
        jsonObject.remove("totalData");
        try {
            jsonObject.put("totalData", totalData);
            loadFile();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 获取历史总流量
     * @return
     */
    public static long getTotalData() {
        try {
            return jsonObject.getLong("totalData");
        } catch (JSONException e) {
            return 0;
        }
    }


    /**
     * 设置返回界面是否运行
      * @param bool
     */
    public static void setBackGroupRun(boolean bool) {
        try {
            jsonObject.remove("backRun");
            jsonObject.put("backRun", bool);//true 为需要开启网速检测
            loadFile();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 记录返回界面是否运行
     */
    public static boolean getBackGroupRun() {
        try {
            return jsonObject.getBoolean("backRun");
        } catch (JSONException e) {//获取不到
            return true;
        }
    }
}
