package com.coolweather.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp") //这里的tmp是格式给的数据？？
    public String temperature;

    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;  //延申到建议
    }

}
