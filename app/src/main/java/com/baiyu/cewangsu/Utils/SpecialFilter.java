package com.baiyu.cewangsu.Utils;

import android.widget.TextView;
import org.json.JSONArray;

import java.util.List;

//特殊过滤器 专为特定url 定制
public class SpecialFilter {

    public static boolean Urlfilter(List<String> spinnerData, String  url){
        if(url.equals("116.128.138.96")){
            spinnerData.add("游戏加速宝");
            return true;
        }
        else if (url .equals( "116.128.138.253")){
            spinnerData.add("腾讯游戏公免");
            return true;
        }
        else if (url .equals( "101.89.17.208")){
            spinnerData.add("爱听4G");
            return true;
        }
        else if (url .equals( "153.35.121.1")){
            spinnerData.add("Amy音乐免流");
            return true;
        }

        return false;


    }
    public static boolean isContain(String url, TextView textView){
       if (url.equals("游戏加速宝")) {
           textView.setText("116.128.138.96");
           return true;
       }
       else if (url.equals("腾讯游戏公免")){

           textView.setText("116.128.138.253");
           return true;
       }

       else if (url.equals("爱听4G")){
           textView.setText("101.89.17.208");
           return true;
       }

       else if (url.equals("Amy音乐免流")){
           textView.setText("153.35.121.1");
           return true;

       }

       return false;
    }




}
