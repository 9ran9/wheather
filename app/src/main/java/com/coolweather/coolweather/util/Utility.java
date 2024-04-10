package com.coolweather.coolweather.util;

import android.text.TextUtils;

import com.coolweather.coolweather.db.City;
import com.coolweather.coolweather.db.County;
import com.coolweather.coolweather.db.Province;
import com.coolweather.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//具体每一阶层所使用的对应工具：
public class Utility {  //Utility:效用，实用工具

    /**
     *将返回的JSON解析为Weather因为对应格式处理好了，直接调用Gson的fromJson方法。
     */

    public static Weather handleWeatherResponse(String response){
        //得到天气响应
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");//和风
            String weatherContent = jsonArray.getJSONObject(0).toString();//从0开始
            return new Gson().fromJson(weatherContent, Weather.class);
            //将JSON数据转化为对应实体，因为这个Weather类的格式写好了（对应的Gson工具）
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    //Province:省级 Response: 响应
    public static boolean handleProvinceResponse(String response) {
        Tool.logd("解析省级信息");
        if (!TextUtils.isEmpty(response)) { //TextUtils:处理文本的工具类
            try {
                //JSONArray:JSON集合，处理对应的格式文件
                JSONArray allProvinces = new JSONArray(response);
                //依次把东西拿出来：
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();   //得到对应实列
                    //这个是基于普通的网页和类得到的。
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();  //更新进数据库
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;  //异常就返回false。
    }

    //市级
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //县级
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
