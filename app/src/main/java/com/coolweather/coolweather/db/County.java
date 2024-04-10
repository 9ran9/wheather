package com.coolweather.coolweather.db;
import org.litepal.crud.LitePalSupport;
public class County extends LitePalSupport{
    //Conty ： 县
    private int id;

    private String countyName;

    private String weatherId;  //记录对应县的天气id

    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

}
