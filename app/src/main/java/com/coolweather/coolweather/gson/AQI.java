package com.coolweather.coolweather.gson;

public class AQI {
    //aqi:空气指数：这些对应的类是由所得的gson所决定的
    public AQICity city;

    //这里为什么不是单纯的city？
    public class AQICity {

        public String aqi;

        public String pm25;

    }
}
