package com.coolweather.coolweather.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.coolweather.coolweather.WeatherActivity;
import com.coolweather.coolweather.gson.Weather;
import com.coolweather.coolweather.util.HttpUtil;
import com.coolweather.coolweather.util.Tool;
import com.coolweather.coolweather.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        getImage();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000; // 这是8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        //elapsed:过去了。
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        //这个是开机到现在的时间。
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息。
     */
    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据，表示更新。
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;  //得到天气的id用于找到对应的地区。
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                    weatherId + "&key=bf629e7271e748fcb629fd08877f21a1";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                        //这里也只是修改Prefs里面的信息。
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     *String requestBingPic = "http://guolin.tech/api/bing_pic";//接口损坏
     */

    private void getImage(){
        final String bingUrl =
                "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(bingUrl)
                            .build();
                    Response response = null;
                    response = client.newCall(request).execute();
                    parseJSONWithJSONObject(response.body().string());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void parseJSONWithJSONObject(String jsonData){

        try {
            JSONArray jsonArray = new JSONObject(jsonData).getJSONArray("images");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String url = jsonObject.getString("url");
                String picurl="http://cn.bing.com"+url;
                Tool.logd(picurl);
                loadBingPic(picurl);
                //这里做一个循环去获取多个图片，但其实只有一个。
                //应该可以用其他方式对其进行改进！！

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void loadBingPic(final String response)  {

        final String bingPic = response;
        SharedPreferences.Editor editor =
                androidx.preference.PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).edit();
        editor.putString("bing_pic", bingPic); //获得对于图片
        editor.apply();
        //存到Prefs里面就好了，到对应的Weather界面优先考虑这个。
    }



}