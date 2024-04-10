package com.coolweather.coolweather.gson;
import com.google.gson.annotations.SerializedName;
public class Basic {
    //Basic:基本
    @SerializedName("city")
    //这是个注解，建立JSON与Java之间的联系（主要是对应的数据是由JSON得到的，有些数据不适合命名）
    public String cityName;

    @SerializedName("id")//这些应该就是所获取格式的数据
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;

    }
}
