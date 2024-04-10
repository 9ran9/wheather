package com.coolweather.coolweather.gson;
import com.google.gson.annotations.SerializedName;

public class Forecast {
    //forecast:预测
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {
        //temperature：温度

        public String max;

        public String min;

    }

    public class More {

        @SerializedName("txt_d")
        public String info;

    }

}
