package com.coolweather.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

//调用HTTP请求的工具，访问对应网址
public class HttpUtil {
    //方法，静态，全局调用：
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        Tool.logd("开始访问网址了呢");
        OkHttpClient client = new OkHttpClient(); //得到client
        Request request = new Request.Builder().url(address).build(); //访问对应网址
        client.newCall(request).enqueue(callback); //callback：回调，类似按钮作用
    }

}
