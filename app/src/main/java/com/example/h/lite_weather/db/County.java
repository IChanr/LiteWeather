package com.example.h.lite_weather.db;

import org.litepal.crud.DataSupport;

/**
 * code...
 * Created by H on 2018/2/2.
 */

public class County extends DataSupport {
    int id;
    String countyName;
    String countyCode;
    int cityId;

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

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
