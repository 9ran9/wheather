package com.coolweather.coolweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.coolweather.gson.Forecast;
import com.coolweather.coolweather.gson.Weather;
import com.coolweather.coolweather.service.AutoUpdateService;
import com.coolweather.coolweather.util.HttpUtil;
import com.coolweather.coolweather.util.Tool;
import com.coolweather.coolweather.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;

    private TextView titleUpdateTime;//date:日期

    private TextView degreeText;  //degree:度

    private TextView weatherInfoText;  //Info:信息

    private LinearLayout forecastLayout;  //forecast:预测

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefreshLayout;
    private String mWeatherId;

    public DrawerLayout drawerLayout;

    private ImageButton navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        View decorView = getWindow().getDecorView();//decor：装饰，获取窗口的装饰视图
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//装饰窗口显示在状态栏上面
        getWindow().setStatusBarColor(Color.TRANSPARENT);//状态栏设置为透明

        //初始化控件：
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        drawerLayout  = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (ImageButton)findViewById(R.id.nav_button);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null); //得到对应数据

        String bingPic = prefs.getString("bing_pic",null);

        if (weatherString != null){
            //有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId; //把Id改为全局变量
            showWeatherInfo(weather);
        }else {
            //无缓存，去服务器查天气，这个所谓的weather_id是主活动传入的对应数据：
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);  //加载的时候不可见
            requestWeather(mWeatherId); //响应天气
        }

        if (bingPic != null){
            Tool.logd("加载背景图片");
            Glide.with(this).load(bingPic).into(bingPicImg);
            //glide:图片加载库，得到对应的bingPic资源，填充进bingPicImg.
        }else {
            Tool.logd("加载背景图片");
            getImage();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId); //重新获得对应数据
            }
        });

        navButton.setOnClickListener(v->{
            drawerLayout.openDrawer(GravityCompat.START);
        });
    }



    //加载对应图片
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
                PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
        editor.putString("bing_pic", bingPic); //获得对于图片
        editor.apply();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
            }
        });
    }



    /**
     *根据id求对应信息
     */
    public void requestWeather(String weatherId) {
        //这个&key=..这个是自己申请的和风对应key,
        //这个http是那个对应城市的接口，大概就是表示对应id所得到的城市数据后+自己的key
        //依据id组合的网址,找到对应的数据：
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=bf629e7271e748fcb629fd08877f21a1";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();  //
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor =
                                    PreferenceManager.getDefaultSharedPreferences
                                            (WeatherActivity.this).edit();
                            editor.putString("weather", responseText);//把数据存进去，这里是运行内存？？
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);//关闭刷新的图标
                    }
                });
            }
            @Override
            public void onFailure(Call call ,IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);//关闭刷新的图标
                    }
                });
            }
        });
        getImage();
        //访问网址模式，最后调用加载图片方法
    }


    //显示天气信息，传入weather:
    private void showWeatherInfo(Weather weather) {
        //初始化数据：
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];//分割
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        Tool.logd("title="+cityName+"\nupdateTime="+updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        //依次遍历预测：
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);//加载完后显示。
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);//显示天气之后显示，确保有对应数据？？
    }
}