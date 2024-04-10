package com.coolweather.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    //根据当前的状态来决定对应的建议
    @SerializedName("comf")
    public Comfort comfort;//comfort:安慰
    @SerializedName("cw")
    public CarWash carWash; //洗车
    public Sport sport;
    public class Comfort{
        @SerializedName("txt")
        public String info;
    }
    public class CarWash{
        @SerializedName("txt")
        public String info;
    }

    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
